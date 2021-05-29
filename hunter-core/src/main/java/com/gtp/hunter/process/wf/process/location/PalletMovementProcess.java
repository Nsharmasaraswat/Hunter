/**
 * 
 */
package com.gtp.hunter.process.wf.process.location;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.json.JsonNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.payload.IdentPayload;
import com.gtp.hunter.common.payload.LocatePayload;
import com.gtp.hunter.common.payload.SensorPayload;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.AGLRawData;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.process.interfaces.UserTransporterInterface;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Mateus Tormin
 *
 */
public class PalletMovementProcess extends LocationProcess implements UserTransporterInterface {

	private static final Gson					gson				= new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	private static final DecimalFormat			DF					= new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));

	private transient static final Logger		logger				= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ExecutorService						exec				= Executors.newCachedThreadPool();

	private Set<Address>						addresses;
	private Set<Address>						addrBlRd;

	private static final Map<String, String>	userTransporterMap	= new ConcurrentHashMap<>();
	private static final Set<String>			connectedForklifts	= new ConcurrentSkipListSet<>();

	public PalletMovementProcess() {
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#checkParams()
	 */
	@Override
	protected void checkParamsImpl() throws Exception {
		if (!getParametros().containsKey("min-distance")) throw new Exception("Parâmetro 'min-distance' não encontrado.");
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#onInit()
	 */
	@Override
	protected void initImpl() {
		this.addresses = location != null && location.getAddresses() != null ? location.getAddresses() : new HashSet<>();
		this.addrBlRd = this.addresses.stream()
						.filter(a -> a.getModel().getMetaname().equals("ROAD") || a.getModel().getMetaname().equals("BLOCK") || a.getModel().getMetaname().equals("RACK") || a.getModel().getMetaname().equals("DRIVE-IN") || a.getModel().getMetaname().equals("DOCK") || a.getModel().getMetaname().equals("TRUCK_ADDRESS"))
						.collect(Collectors.toSet());
		logger.info("Process Initialized: " + this.addrBlRd.size() + " addresses on location " + location.getName());
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#connect()
	 */
	@Override
	protected void connect() {
		//		if (mockTimer == null) {
		//			mockTimer = new Timer("MockRDTimer", true);
		//			mockTimerTask = new RandomEventSender();
		//			mockTimer.scheduleAtFixedRate(mockTimerTask, 500, 400);
		//			logger.info("Initializing mock timer");
		//		}
		logger.info("Connect");
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#timeout(java.util.Map)
	 */
	@Override
	public void timeout(Map<String, Thing> itens) {
		this.runSucess();
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processBefore(com.gtp.hunter.core.model.ComplexData)
	 */
	@Override
	protected void processBefore(ComplexData rd) {
		final Profiler prof = new Profiler("ProcessBefore");
		final boolean verbose = ConfigUtil.get("hunter-process", "verbose-process", "false").equalsIgnoreCase("true");

		prof.step(logPrefix() + "Load Config", verbose);
		exec.submit(() -> {
			switch (rd.getType()) {
				case LOCATION:
					LocatePayload lcpl = gson.fromJson(rd.getPayload(), LocatePayload.class);
					prof.step(logPrefix() + "Payload Serialization", verbose);
					Geometry g = getPoint(lcpl);
					prof.step(logPrefix() + "Extract Point From Geometry", verbose);
					double minDistance = ((JsonNumber) getParametros().get("min-distance")).doubleValue();
					prof.step(logPrefix() + "Load Parameter", verbose);
					boolean found = false;

					for (Address a : this.addrBlRd) {
						double dist = a.getRegion().distance(g);

						prof.step(logPrefix() + "Get Point Distance To " + a.getName() + "(" + dist + ")", verbose);
						if (dist < minDistance) {
							if (verbose) logger.info(rd.getTagId() + " Distance: " + dist + " Address: " + a.getName());
							AGLRawData<LocatePayload> aglRd = new AGLRawData<>(rd);

							lcpl.setNearbyAddress(a.getId().toString());
							lcpl.setNearbyAddressName(a.getName());
							rd.setPayload(lcpl.toString());
							aglRd.setPayload(lcpl);
							prof.step(logPrefix() + "Build Device Payload", verbose);
							resend(aglRd);
							prof.step(logPrefix() + "On Next", verbose);
							found = true;
						}
					}
					if (!found) {
						AGLRawData<LocatePayload> aglRd = new AGLRawData<>(rd);

						lcpl.setNearbyAddress("");
						lcpl.setNearbyAddressName("");
						rd.setPayload(lcpl.toString());
						aglRd.setPayload(lcpl);
						prof.step(logPrefix() + "Build Device Payload", verbose);
						resend(aglRd);
						prof.step(logPrefix() + "On Next", verbose);
					}
					prof.done(logPrefix() + "Process End", verbose, verbose);
					break;
				case IDENT:
					AGLRawData<IdentPayload> aglIRd = new AGLRawData<>(rd);
					IdentPayload idpl = gson.fromJson(rd.getPayload(), IdentPayload.class);

					aglIRd.setPayload(idpl);
					resend(aglIRd);
					break;
				case SENSOR:
					AGLRawData<SensorPayload> aglSRd = new AGLRawData<>(rd);
					SensorPayload snpl = gson.fromJson(rd.getPayload(), SensorPayload.class);

					logger.info(rd.getTagId() + " on process " + hashCode());
					if (userTransporterMap.containsKey(rd.getTagId()))
						aglSRd.setTagId(userTransporterMap.get(rd.getTagId()));
					else if (!connectedForklifts.contains(rd.getTagId())) {//TODO: Buscar se Paleteira ou Empilhadeira
						getRegSvc().getAlertSvc().persist(new Alert(AlertType.PROCESS, AlertSeverity.WARNING, rd.getTagId(), "Pesagem Incorreta. Sem Tarefas Em Execução.", "Pesagem " + DF.format(snpl.getValue())));
					}
					aglSRd.setPayload(snpl);
					resend(aglSRd);
					break;
				case STATUS:
					logger.info("Status CD: " + rd.toString());
					break;
				default:
					break;

			}
		});
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processAfter(com.gtp.hunter.process.model.Thing)
	 */
	@Override
	protected void processAfter(Thing rd) {
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processUnknown(com.gtp.hunter.core.model.ComplexData)
	 */
	@Override
	protected void processUnknown(ComplexData rd) {
		logger.info(rd.toString());
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#success()
	 */
	@Override
	protected void success() {
		logger.info("SUCCESS");
		resetProcess();
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#failure()
	 */
	@Override
	protected void failure() {
		logger.warn("FAILURE");
		resetProcess();
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#cancel()
	 */
	@Override
	public void cancel() {
		logger.warn("CANCEL");
		resetProcess();
	}

	@Override
	public void setUserTransporter(User us) {
		String transporterId = us.getProperties().get("transporter-tag");
		String tagId = us.getProperties().get("rtls-tag");
		String forkliftTag = us.getProperties().get("forklift-tag");

		if (transporterId != null && tagId != null)
			userTransporterMap.put(transporterId, tagId);
		if (forkliftTag != null)
			connectedForklifts.add(forkliftTag);
	}

	@Override
	public void removeUserTransporter(User us) {
		String transporterId = us.getProperties().get("transporter-tag");
		String tagId = us.getProperties().get("rtls-tag");
		String forkliftTag = us.getProperties().get("forklift-tag");

		if (transporterId != null && tagId != null && userTransporterMap.containsKey(transporterId) && userTransporterMap.get(transporterId).equals(tagId))
			userTransporterMap.remove(transporterId);
		if (forkliftTag != null)
			connectedForklifts.remove(forkliftTag);
	}

	private void resetProcess() {
		if (getParametros().containsKey("positions")) getParametros().remove("positions");
	}
}
