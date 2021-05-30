/**
 * 
 */
package com.gtp.hunter.process.wf.process.solar;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.payload.LocatePayload;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.process.location.LocationProcess;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Mateus Tormin
 *
 */
public class TruckTrackProcess extends LocationProcess {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Set<Address>					addresses;

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#checkParams()
	 */
	@Override
	protected void checkParamsImpl() throws Exception {
		if (!getParametros().containsKey("master-document-meta"))
			throw new Exception("Parâmetro 'master-document-meta' não encontrado.");
		if (!getParametros().containsKey("icon-model"))
			throw new Exception("Parâmetro 'iconModel' não encontrado.");
		if (!getParametros().containsKey("icon-meta"))
			throw new Exception("Parâmetro 'iconMeta' não encontrado.");
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#connect()
	 */
	@Override
	protected void connect() {
		logger.debug("Connect");
		Map<String, Object> message = new LinkedHashMap<>();
		Map<String, Object> data = new LinkedHashMap<>();

		message.put("action", "LocationInit");
		data.put("center", new Double[] { location.getCenter().getCentroid().getX(), location.getCenter().getCentroid().getY() });
		data.put("name", location.getName());
		data.put("addresses", location.getAddresses());
		message.put("data", data);
		resend(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization().create().toJson(message));
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#timeout(java.util.Map)
	 */
	@Override
	public void timeout(Map<String, Thing> itens) {
		Profiler prof = new Profiler();
		if (itens != null) {
			prof.step("Timeout: " + itens.size(), false);
			for (String tagId : itens.keySet()) {
				Thing t = itens.get(tagId);
				LocatePayload payload = new GsonBuilder().create().fromJson(t.getPayload(), LocatePayload.class);
				Geometry g = getPoint(payload);

				prof.step(logPrefix() + " Thing " + t.getName() + " with tagId " + tagId, false);
				if (addresses.parallelStream().anyMatch(a -> a.getRegion().contains(g))) {
					Address add = addresses.parallelStream().filter(a -> a.getRegion().contains(g)).findAny().get();
					DocumentThing dt = getRegSvc().getDtSvc().quickFindByThingIdAndDocModelMeta(t.getId(), (String) getParametros().get("master-document-meta"));

					prof.step(logPrefix() + " Quick Find DT ", false);
					if (dt != null && dt.getId() != null && dt.getDocument() != null && dt.getDocument().getId() != null) {
						prof.step(logPrefix() + " Inside address " + add.getMetaname() + " with document " + dt.getDocument().getId().toString(), false);
						getParametros().put("doc", dt.getDocument());
						getParametros().put("thing", t);
						getParametros().put("address", add);
						this.runSucess();
						//TODO: Improve passing parameters to activities
						getParametros().remove("address");
						getParametros().remove("thing");
						getParametros().remove("doc");
					} else if (dt == null) {
						prof.step(logPrefix() + " No Document Attached to Truck", false);
					} else {
						prof.step(logPrefix() + " Dt: " + dt.getId().toString() + " Doc: " + (dt.getDocument() == null ? "NULL" : dt.getDocument().getId().toString()), false);
					}
				}
			}
		}
		this.unlock();
		prof.done(logPrefix() + "Processed " + itens == null ? "NULL" : itens.size() + " messages", false, false).forEach(logger::debug);
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processBefore(com.gtp.hunter.core.model.ComplexData)
	 */
	@Override
	protected void processBefore(ComplexData rd) {
		logger.debug(rd.toString());
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processAfter(com.gtp.hunter.process.model.Thing)
	 */
	@Override
	protected void processAfter(Thing rd) {
		if (rd != null) {
			if (rd.getAddress() != null) {
				logger.debug("Address after: " + rd.getAddress().getMetaname());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processUnknown(com.gtp.hunter.core.model.ComplexData)
	 */
	@Override
	protected void processUnknown(ComplexData rd) {
		logger.debug(rd.toString());
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
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#onInit()
	 */
	@Override
	protected void initImpl() {
		this.addresses = location != null && location.getAddresses() != null ? location.getAddresses() : new HashSet<>();
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#cancel()
	 */
	@Override
	public void cancel() {
		logger.warn("CANCEL");
		resetProcess();
	}

	private void resetProcess() {
		getParametros().remove("positions");
	}
}
