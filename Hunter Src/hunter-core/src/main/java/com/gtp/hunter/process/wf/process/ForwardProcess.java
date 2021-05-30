package com.gtp.hunter.process.wf.process;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.json.JsonNumber;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.common.util.RestUtil;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.AGLRawData;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.activity.BaseProcessActivity;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityPhase;
import com.gtp.hunter.process.wf.process.interfaces.ForwardProcessEncoder;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;
import com.gtp.hunter.ui.json.process.ProcessAlert;
import com.gtp.hunter.ui.json.process.ProcessNotification;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

//TODO: add activities from other modules
public class ForwardProcess extends BaseProcess {

	private transient static final Logger							logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final boolean									EUROFARMA	= ConfigUtil.get("hunter-process", "customer", "EUROFARMA").equalsIgnoreCase("EUROFARMA");
	private RestUtil												rest;
	private RegisterService											regSvc;
	private RegisterStreamManager									rsm;
	private PublishSubject											ps;
	private Process													model;
	private BaseOrigin												origin;
	private Disposable												disp;
	private Map<String, Object>										params;
	private boolean													complete;
	protected List<BaseProcessActivity>								filters		= new ArrayList<BaseProcessActivity>();
	private ConcurrentHashMap<String, Future<IntegrationReturn>>	futureMap	= new ConcurrentHashMap<>();
	private List<ForwardProcessEncoder>								forwarders	= new CopyOnWriteArrayList<>();
	private ScheduledExecutorService								execSvc		= Executors.newScheduledThreadPool(5);

	public void onBaseInit(Process model, RegisterService tRep, BaseOrigin origin, RegisterStreamManager rsm) throws Exception {
		this.model = model;
		this.regSvc = tRep;
		this.rsm = rsm;
		this.ps = PublishSubject.create();
		this.origin = origin;
		// this.origin.getOrigin().subscribe(this);
		this.params = JsonUtil.jsonToMap(model.getParam());
		baseCheckParams();
		this.rest = new RestUtil((String) this.params.get("base-url"));
	}

	private void baseCheckParams() throws Exception {
		if (!this.params.containsKey("base-url"))
			throw new Exception("Parâmetro 'base-url' não encontrado.");
	}

	@Override
	public void initFilters() {
		final Profiler p = new Profiler();
		model.getActivities().stream().forEach(f -> {
			try {
				p.step(logPrefix() + "Adicionando Activity " + f.getMetaname() + " - " + f.getClasse(), true);
				Constructor<BaseProcessActivity> c = (Constructor<BaseProcessActivity>) Class.forName(f.getClasse()).getConstructor(ProcessActivity.class, BaseOrigin.class);
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
		filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.POSTCONSTRUCT)).forEach(a -> a.executePostConstruct());
		p.done(logPrefix() + "Process Complete!", true, false);
	}

	@Override
	public void onSubscribe(Disposable arg0) {
		this.disp = arg0;
		ps.onSubscribe(arg0);
	}

	@Override
	public void onNext(ComplexData cd) {
		String method = (String) this.params.get("method");
		String verb = (String) this.params.get("verb");

		for (ForwardProcessEncoder fm : forwarders) {
			JsonObject ret = fm.encodeMessage(this, cd);

			logger.info("JSON: " + ret.toString());
			logger.warn("Add to Map: " + cd.getTagId());
			futureMap.put(cd.getTagId(), rest.sendAsync(ret, method, verb, null, (String) params.get("username"), (String) this.params.get("password")));
		}
		if (EUROFARMA)
			execSvc.execute(() -> {
				while (!futureMap.isEmpty()) {
					//				logger.info("Map size: " + futureMap.size());
					try {
						for (Entry<String, Future<IntegrationReturn>> en : futureMap.entrySet()) {
							//logger.info("Tag " + en.getKey());
							if (en.getValue().isDone()) {
								IntegrationReturn ret = en.getValue().get();
								AlertSeverity sev = ret.isResult() ? AlertSeverity.INFO : AlertSeverity.ERROR;
								String desc = ret.isResult() ? "" : ret.getMessage();
								String data = "{\"tag\":\"" + en.getKey() + "\",\"message\":\"" + desc + "\"}";

								getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, en.getKey(), model.getOrigin().getName(), desc));
								if (ret.isResult()) {
									ProcessNotification not = new ProcessNotification();
									Thing t = getRegSvc().getThSvc().transform(cd);

									if (t == null) {
										Unit u = new Unit("Barcode", en.getKey(), UnitType.CODE128);

										t = new Thing("Não Identificado", null, null, "RECEBIDO");
										t.getUnitModel().add(getRegSvc().getUnSvc().persist(u));
										t.getUnits().add(u.getId());
										getRegSvc().getThSvc().persist(t);
										logger.info("Thing persisted: " + t.getId().toString());
									}
									not.setData(data);
									logger.info("Notify Process: " + not.toString());
									ps.onNext(not);
								} else {
									ProcessAlert al = new ProcessAlert();

									al.setData(data);
									logger.info("Alert Process: " + al.toString());
									ps.onNext(al);
									execSvc.execute(() -> {
										filters.stream()
														.filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.FAILURE))
														.forEach(a -> {
															logger.info("Executing Activity " + a.getModel().getMetaname());
															a.execute();
														});
									});
									execSvc.schedule(() -> {
										try {
											filters.stream()
															.filter(b -> b.getModel().getPhase().equals(ProcessActivityPhase.REBOOT))
															.forEach(b -> {
																logger.info("Executing Activity " + b.getModel().getMetaname());
																b.execute();
															});
										} catch (Exception e) {
											logger.error(e.getLocalizedMessage(), e);
										}
									}, ((JsonNumber) params.get("stacklight-timeout")).longValue(), TimeUnit.SECONDS);
								}
								logger.info("Remove Key " + en.getKey());
								futureMap.remove(en.getKey());
							}
							Thread.yield();
						}
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				logger.warn("Map Size: " + futureMap.size());
			});
	}

	@Override
	public void message(BaseProcessMessage msg) {
		logger.info("Message Received: " + msg.toString());
		ps.onNext(msg);
	}

	public BaseOrigin getBaseOrigin() {
		return this.origin;
	}

	@Override
	public void onError(Throwable arg0) {
		logger.error(this.logPrefix() + arg0.getLocalizedMessage());
		Stream.of(arg0.getStackTrace()).forEach(s -> logger.error(s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")"));
		ps.onError(arg0);
	}

	@Override
	public void onComplete() {
		ps.onComplete();
		this.disp.dispose();
	}

	@Override
	public void subscribe(Observer arg0) {
		ps.subscribe(arg0);
		connect();
	}

	@Override
	public Process getModel() {
		return this.model;
	}

	@Override
	public boolean isComplete() {
		return this.complete;
	}

	@Override
	public RegisterService getRegSvc() {
		return regSvc;
	}

	@Override
	public RegisterStreamManager getRsm() {
		return rsm;
	}

	@Override
	public Map<String, Object> getParametros() {
		return params;
	}

	@Override
	public String finish() {
		final Profiler p = new Profiler();
		if (!isFailure()) {
			success();
		} else {
			failure();
		}

		this.complete = true;
		p.step(logPrefix() + " RETORNO DO FIM DO PROCESSO: " + this.getModel().getUrlRetorno(), true);

		this.disp.dispose();

		rsm.getPsm().deactivateProcess(this.getModel());
		p.done(logPrefix() + " complete!", true, false);
		return this.getModel().getUrlRetorno();
	}

	private void connect() {

	}

	public void addEncoder(ForwardProcessEncoder fpe) {
		this.forwarders.add(fpe);
	}

	public void removeEncoder(ForwardProcessEncoder fpe) {
		this.forwarders.remove(fpe);
	}

	@Override
	public Observable getFilterByDocument(UUID document) {
		return ps.filter(new Predicate<Object>() {
			@Override
			public boolean test(Object o) {
				if (o instanceof Thing) {
					Thing v = (Thing) o;

					if (v == null || v.getDocument() == null || document == null)
						return false;
					return (v.getDocument().equals(document));
				} else if (o instanceof BaseProcessMessage) {
					return true;
				}
				return false;
			}
		}).observeOn(Schedulers.computation()).doOnSubscribe((a) -> connect());
	}

	@Override
	public Observable getFilterByTagId(String tagId) {
		return ps.filter(new Predicate<Object>() {
			@Override
			public boolean test(Object cd) {
				if (cd instanceof ComplexData)
					return ((ComplexData) cd).getTagId().equals(tagId);
				if (cd instanceof AGLRawData<?>) {
					return ((AGLRawData) cd).getTagId().equals(tagId);
				} else
					return true;
			}
		}).observeOn(Schedulers.computation());
	}

	@Override
	public void onInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void success() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void failure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}
}
