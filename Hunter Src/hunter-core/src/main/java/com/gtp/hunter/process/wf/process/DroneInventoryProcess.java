package com.gtp.hunter.process.wf.process;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.gtp.hunter.common.model.DroneStatus;
import com.gtp.hunter.common.model.RawData;
import com.gtp.hunter.common.payload.LocatePayload;
import com.gtp.hunter.common.payload.SensorPayload;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.activity.BaseProcessActivity;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityPhase;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;
import com.gtp.hunter.ui.json.process.drone.CalculatedStock;
import com.gtp.hunter.ui.json.process.drone.DisplayAddress;
import com.gtp.hunter.ui.json.process.drone.LoadLocation;
import com.gtp.hunter.ui.json.process.drone.MarkerData;
import com.gtp.hunter.ui.json.process.drone.Measurement;
import com.gtp.hunter.ui.json.process.drone.ProcessMessage;
import com.gtp.hunter.ui.json.process.drone.ShowInventory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class DroneInventoryProcess extends BaseProcess {

	private transient static final Logger		logger							= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ScheduledExecutorService			calc							= Executors.newScheduledThreadPool(5);
	private static final Double					MAX_ALLOWED_DISTANCE			= 500d;
	private static final Double					MAX_ALLOWED_DISPLACEMENT		= 60d;
	private static final Double					RACK_HEIGHT						= 187.5d;
	private static final int					MAX_DISPLACEMENT_ERROR_COUNT	= 5;
	private static final DecimalFormat			DF								= new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));

	private List<BaseProcessActivity>			filters							= new ArrayList<BaseProcessActivity>();
	private boolean								verbose							= ConfigUtil.get("hunter-process", "verbose-process", "false").equalsIgnoreCase("true");
	private Process								model;
	private RegisterService						regSvc;
	private RegisterStreamManager				rsm;
	private BaseOrigin							origin;
	private PublishSubject<BaseProcessMessage>	ps;
	private Disposable							disp;
	private Map<String, Object>					params;
	private Location							loc;
	private List<Address>						addrList;
	private Geometry							currPosition;
	private List<Measurement>					measurementList;
	private Thing								drone;
	private Document							documentInventory;
	private Document							documentCount;
	private double								lastHeight;
	private double								lastDist;
	private int									errorDistanceCount;

	@Override
	public void onSubscribe(Disposable d) {
		this.disp = d;
		ps.onSubscribe(d);
	}

	@Override
	public void onNext(ComplexData cd) {
		try {
			verbose = ConfigUtil.get("hunter-process", "verbose-process", "false").equalsIgnoreCase("true");
			if (drone != null && documentInventory != null && documentCount != null && cd.getUnit() != null && drone.getUnits().contains(cd.getUnit().getId())) {
				MarkerData md = new MarkerData();
				DroneStatus status = new DroneStatus(drone.getId());

				drone.setPayload(((RawData) cd).toString());
				switch (cd.getType()) {
					case SENSOR:
						final SensorPayload spl = new Gson().fromJson(cd.getPayload(), SensorPayload.class);
						boolean add = true;
						double val = spl.getValue();
						double height = 0;
						double distF = 0;

						switch (cd.getPort()) {
							case 0://BELOW
								height = val;
								lastHeight = val;
								distF = lastDist;
								status.setAltitude(val);
								break;
							case 1://FRONT
								distF = val;
								lastDist = val;
								height = lastHeight;
								status.setText("Block: " + DF.format(val) + "cm");
								if (val > MAX_ALLOWED_DISTANCE)
									add = false;
								break;
						}
						if (currPosition != null && add) {
							if (verbose) logger.info("Height: " + DF.format(height) + " Distance: " + DF.format(distF) + " DistPos: " + DF.format((double) currPosition.getUserData()) + " Port: " + cd.getPort());
							measurementList.add(new Measurement(currPosition.getCentroid(), height, distF, (double) currPosition.getUserData(), (short) cd.getPort()));
						}
						break;
					case LOCATION://Position
						LocatePayload lpl = new Gson().fromJson(cd.getPayload(), LocatePayload.class);
						Geometry locPosition = getPoint(lpl);
						double dist = currPosition == null ? 0 : locPosition.distance(currPosition);

						if (dist < MAX_ALLOWED_DISPLACEMENT || ++errorDistanceCount > MAX_DISPLACEMENT_ERROR_COUNT) {
							locPosition.setUserData(currPosition == null ? 0d : locPosition.distance(currPosition));
							status.setLatitude(lpl.getY());
							status.setLongitude(lpl.getX());
							errorDistanceCount = 0;
							currPosition = locPosition;
						} else
							return;
						break;
					case IDENT:
						break;
					case STATUS:
						break;
					default:
						break;
				}
				md.setData(drone);
				ps.onNext(md);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			e.printStackTrace();
		}
	}

	@Override
	public void onError(Throwable e) {
		Stream.of(e.getStackTrace()).forEach(s -> logger.error(s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")"));
		ps.onError(e);
	}

	@Override
	public void message(BaseProcessMessage msg) {
		switch (msg.getCommand()) {
			case "FINISH":
				logger.info(this.finish());
				break;
			default:
				logger.info("Message Received: " + msg.toString());
		}
	}

	@Override
	public void onComplete() {
		ProcessMessage pm = new ProcessMessage("COMPLETE");

		ps.onNext(pm);
		ps.onComplete();
		logger.warn("SHUTTING DOWN NOTIFIER");
		calc.shutdown();
		getRegSvc().getDcSvc().persist(this.documentInventory);
		rsm.getPsm().deactivateProcess(this.getModel());
		this.disp.dispose();
	}

	@Override
	public void subscribe(Observer<? super Object> observer) {
		LoadLocation ll = new LoadLocation();
		DisplayAddress da = new DisplayAddress();

		ps.subscribe(observer);
		ll.setData(loc);
		ps.onNext(ll);
		da.setData(addrList);
		ps.onNext(da);
		calc.scheduleAtFixedRate(() -> {
			try {
				List<CalculatedStock> stockList = calculateStock();

				sendInventory(stockList);
				saveSnapshot(stockList);
				stockList.forEach(cs -> logger.info("Calculated: " + cs.getAddress().getMetaname() + " Measurements: " + cs.getMeasurementList().size() + " Average Height: " + cs.getAverageHeight() + " Average Distance: " + cs.getAverageDistance() + " Product: " + (cs.getProduct() == null ? "" : cs.getProduct().getSku() + " - " + cs.getProduct().getName()) + " Pallet Height: " + cs.getProductHeight() + " Margin: " + cs.getMargin() + " Count: " + cs.getCount() + " CountArray: " + Arrays.toString(cs.getCountArr())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 0, 5, TimeUnit.SECONDS);
	}

	private void sendInventory(List<CalculatedStock> stockList) {
		ShowInventory si = new ShowInventory();

		si.setData(stockList);
		ps.onNext(si);
	}

	private void saveSnapshot(List<CalculatedStock> stockList) {
		Map<Address, List<CalculatedStock>> stockMap = stockList.stream().collect(Collectors.groupingBy(cs -> cs.getAddress().getParent()));

		for (Address a : stockMap.keySet()) {
			List<CalculatedStock> csList = stockMap.get(a);
			int count = csList.stream().mapToInt(cs -> cs.getCount()).sum();

			if (csList.get(0).getProduct() != null && count != 0)
				getRegSvc().getWmsSvc().insertStkInventoryCount(this.documentCount.getId().toString(), a.getId().toString(), csList.get(0).getProduct().getId().toString(), count);
		}
	}

	private List<CalculatedStock> calculateStock() {
		List<CalculatedStock> stockList = new CopyOnWriteArrayList<>();

		for (Measurement m : measurementList) {
			List<Address> as = addrList.stream()
							.flatMap(ap -> ap.getSiblings().stream())
							.filter(ad -> {
								if (ad.getParent().getModel().getMetaname().equals("RACK")) {
									return ad.getParent().getRegion().contains(m.getPosition());
								} else
									return ad.getRegion().contains(m.getPosition());
							})
							.sorted((Address o1, Address o2) -> {
								if (o1 == null && o2 == null) return 0;
								if (o2 == null) return 1;
								if (o1 == null) return -1;
								if (o2.getMetaname() == null) return 1;
								if (o1.getMetaname() == null) return -1;
								return o1.getMetaname().compareTo(o2.getMetaname());
							})
							.collect(Collectors.toList());
			if (!as.isEmpty()) {
				int rackPos = as.get(0).getParent().getModel().getMetaname().equals("RACK") ? (int) Math.floor(m.getHeight() / RACK_HEIGHT) : 0;
				Address a = as.get(rackPos);
				//				logger.info("AS: " + a.getMetaname() + " Parent: " + a.getParent().getMetaname() + " ParentModel: " + a.getParent().getModel().getMetaname() + " Height: " + DF.format(m.getHeight()) + " Pos: " + rackPos);
				if ((a.getWkt() == null || a.getWkt().isEmpty()) && a.getRegion() != null)
					a.setWkt(a.getRegion().toText());
				CalculatedStock cs = stockList.stream()
								.filter(s -> s.getAddress().getId().equals(a.getId()))
								.findAny()
								.orElseGet(() -> {
									Product prd = a.getParent().getModel().getMetaname().equals("RACK") ? a.getProduct() : a.getParent().getProduct();

									if (prd == null) {
										Product tmp = null;

										if (a.getParent().getModel().getMetaname().equals("RACK")) {
											tmp = getRegSvc().getPrdSvc().findByAddress(a.getParent().getId());

											a.setProduct(tmp);
										} else {
											tmp = getRegSvc().getPrdSvc().findByAddressParent(a.getParent().getId());

											a.getParent().setProduct(tmp);
										}
										prd = tmp;
									}
									CalculatedStock c = new CalculatedStock(a, prd);

									stockList.add(c);
									return c;
								});
				cs.getMeasurementList().add(m);
				cs.calculate();
				for (int i = 0; i < cs.getCount(); i++)
					as.get(i).setOccupied(true);
			}
		}
		return stockList;
	}

	@Override
	public Process getModel() {
		return this.model;
	}

	@Override
	public void onBaseInit(Process model, RegisterService tRep, BaseOrigin origin, RegisterStreamManager rsm) throws Exception {
		this.model = model;
		this.regSvc = tRep;
		this.origin = origin;
		this.rsm = rsm;
		this.ps = PublishSubject.create();
		this.verbose = false;//TODO: Remove to implement
		this.measurementList = new CopyOnWriteArrayList<>();
	}

	@Override
	public void onInit() {
		if (verbose) logger.info("Init");
		try {
			this.params = JsonUtil.jsonToMap(model.getParam());
		} catch (NullPointerException npe) {
			this.params = new HashMap<>();
		}
		String locId = (String) getParametros().get("location_id");
		String thId = (String) getParametros().get("thing_id");
		String docId = (String) getParametros().get("document_id");

		this.loc = getRegSvc().getLocSvc().findById(UUID.fromString(locId));
		this.drone = thId.equals("%%thingid%%") ? null : getRegSvc().getThSvc().findById(UUID.fromString(thId));
		this.documentInventory = docId.equals("%%docid%%") ? null : getRegSvc().getDcSvc().findById(UUID.fromString(docId));
		if (this.documentInventory != null) {
			createChildDocument();
			createFlightPlan();
		}
	}

	@Override
	protected void success() {
		if (verbose) logger.info("success");
	}

	@Override
	protected void failure() {
		if (verbose) logger.info("failure");
	}

	@Override
	public String finish() {
		if (verbose) logger.info("finish");
		List<CalculatedStock> stockList = calculateStock();

		sendInventory(stockList);
		saveSnapshot(stockList);
		onComplete();
		return "COMPLETED";
	}

	@Override
	public void cancel() {
		if (verbose) logger.info("cancel");
	}

	@Override
	public Observable<ComplexData> getFilterByDocument(UUID document) {
		if (verbose) logger.info("filterByDocument");
		return null;
	}

	@Override
	public Observable<ComplexData> getFilterByTagId(String tagId) {
		if (verbose) logger.info("filterByTagId");
		return null;
	}

	@Override
	public boolean isComplete() {

		return !isFailure();
	}

	@Override
	public Map<String, Object> getParametros() {
		return this.params;
	}

	@Override
	public RegisterService getRegSvc() {
		return this.regSvc;
	}

	@Override
	public RegisterStreamManager getRsm() {
		return this.rsm;
	}

	@Override
	public void initFilters() {
		model.getActivities().stream().forEach(f -> {
			try {
				Constructor<? extends BaseProcessActivity> c = (Constructor<BaseProcessActivity>) Class.forName(f.getClasse()).asSubclass(BaseProcessActivity.class).getConstructor(ProcessActivity.class, BaseOrigin.class);
				BaseProcessActivity bpf = c.newInstance(f, this.origin);
				filters.add(bpf);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		});
		// FILTROS CARREGADOS. RODANDO OS FILTROS DE POSTCONSTRUCT
		for (BaseProcessActivity a : filters) {
			if (a.getModel().getPhase().equals(ProcessActivityPhase.POSTCONSTRUCT)) {
				a.executePostConstruct();
			}
		}
	}

	private Geometry getPoint(LocatePayload lcpl) {
		Geometry g = null;
		WKTReader rdr = new WKTReader();

		try {
			String point = "POINT(";
			if (lcpl.getLatitude() != 0 && lcpl.getLongitude() != 0)
				point += lcpl.getLatitude() + " " + lcpl.getLongitude() + ")";
			else
				point += +lcpl.getX() + " " + lcpl.getY() + ")";
			g = rdr.read(point);
		} catch (ParseException pe) {
			logger.error(pe.getLocalizedMessage());
			logger.trace(pe.getLocalizedMessage(), pe);
		}
		return g;
	}

	private void createChildDocument() {
		DocumentModel dmCount = getRegSvc().getDmSvc().findByMetaname("APOCONTINV");
		String parentCode = this.documentInventory.getCode();
		Prefix pfx = getRegSvc().getPfxSvc().findNext("CNT" + parentCode + "-", 3);

		this.documentCount = new Document(dmCount, dmCount.getName() + " " + pfx.getPrefix() + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "DRONE");
		this.documentCount.setParent(this.documentInventory);
		this.documentInventory.getSiblings().add(getRegSvc().getDcSvc().persist(this.documentCount));
	}

	private void createFlightPlan() {
		this.addrList = this.documentInventory == null ? new ArrayList<>() : this.documentInventory.getSiblings()
						.stream()
						.filter(ds -> ds.getModel().getMetaname().equals("APORUAINV"))
						.flatMap(ds -> ds.getFields().stream())
						.filter(df -> df.getField().getMetaname().equals("INVADDRESS"))
						.map(df -> {
							Address a = getRegSvc().getAddSvc().findById(UUID.fromString(df.getValue()));

							if (a.getModel().getMetaname().equals("RACK"))
								for (Address as : a.getSiblings()) {
									as.setProduct(getRegSvc().getPrdSvc().findByAddress(as.getId()));
									as.setWkt(as.getRegion().toText());
								}
							else
								a.setProduct(getRegSvc().getPrdSvc().findByAddressParent(a.getId()));
							a.setWkt(a.getRegion().toText());
							return a;
						})
						.sorted((Address o1, Address o2) -> {
							if (o1 == null && o2 == null) return 0;
							if (o2 == null) return 1;
							if (o1 == null) return -1;
							return o1.getMetaname().compareTo(o2.getMetaname());
						})
						.collect(Collectors.toList());
	}
}
