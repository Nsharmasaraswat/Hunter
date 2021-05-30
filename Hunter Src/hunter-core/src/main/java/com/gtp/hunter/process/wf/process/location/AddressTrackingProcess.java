/**
 * 
 */
package com.gtp.hunter.process.wf.process.location;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.payload.LocatePayload;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.Thing;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author Mateus Tormin
 *
 */
public class AddressTrackingProcess extends LocationProcess {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Set<Address>					addresses;

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#checkParams()
	 */
	@Override
	protected void checkParamsImpl() throws Exception {
		if (!getParametros().containsKey("addressModel"))
			throw new Exception("Parâmetro 'addressModel' não encontrado.");
		if (!getParametros().containsKey("updateUnitType"))
			throw new Exception("Parâmetro 'updateUnitType' não encontrado.");
		if (!getParametros().containsKey("masterDocumentMeta"))
			throw new Exception("Parâmetro 'masterDocumentMeta' não encontrado.");
		if (!getParametros().containsKey("newAddressDocumentMeta"))
			throw new Exception("Parâmetro 'newAddressDocumentMeta' não encontrado.");
		if (!getParametros().containsKey("iconModel"))
			throw new Exception("Parâmetro 'iconModel' não encontrado.");
		if (!getParametros().containsKey("iconMeta"))
			throw new Exception("Parâmetro 'iconMeta' não encontrado.");
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#connect()
	 */
	@Override
	protected void connect() {
		logger.info("Connect");
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
		logger.info("Timeout: " + itens.size());
		this.unlock();
		for (String tagId : itens.keySet()) {
			Thing t = itens.get(tagId);
			LocatePayload payload = new GsonBuilder().create().fromJson(t.getPayload(), LocatePayload.class);
			Map<String, Address> lastPosition = getParametros().containsKey("positions") ? (HashMap<String, Address>) getParametros().get("positions") : new HashMap<>();
			Address lastAddress = lastPosition.get(tagId);
			DocumentThing dt = getRegSvc().getDtSvc().quickFindByThingIdAndDocModelMeta(t.getId(), (String) getParametros().get("masterDocumentMeta"));
			Document dMaster;
			boolean insideAny = false;

			for (Address a : addresses) {
				try {
					WKTReader rdr = new WKTReader();
					Geometry g = rdr.read("POINT(" + payload.getLongitude() + " " + payload.getLatitude() + ")");

					if (a.getRegion().contains(g)) {
						DocumentModel dmMaster = getRegSvc().getDmSvc().findByMetaname((String) getParametros().get("masterDocumentMeta"));
						DocumentModel dmTrack = getRegSvc().getDmSvc().findByMetaname((String) getParametros().get("newAddressDocumentMeta"));
						DocumentModelField dmfTrack = getRegSvc().getDmfSvc().findByMetaname((String) getParametros().get("addressDocumentModelField"));
						String prodMeta = (String) getParametros().get("productMeta");

						try {
							t.setProperties(new HashSet<Property>(getRegSvc().getPrSvc().listByThing(t)));
							//TODO: FIX
							Property prProduct = t.getProperties().stream().filter(pr -> pr.getField().getMetaname().equals(prodMeta)).findFirst().get();
							Product pd = getRegSvc().getPrdSvc().findByMetaname(prProduct.getValue());
							logger.info(tagId + " INSIDE " + a.getMetaname());
							if (dt == null) {
								DocumentItem diMaster = new DocumentItem();

								dMaster = new Document();
								dMaster.setCode("1");
								dMaster.setModel(dmMaster);
								getRegSvc().getDcSvc().persist(dMaster);

								dt = new DocumentThing();
								dt.setDocument(dMaster);
								dt.setThing(t);
								getRegSvc().getDtSvc().persist(dt);

								diMaster.setDocument(dMaster);
								diMaster.setProduct(pd);
								diMaster.setQty(1);
								diMaster.setMeasureUnit("TR");
								getRegSvc().getDiSvc().persist(diMaster);
							} else {
								dMaster = getRegSvc().getDcSvc().quickFindById(dt.getDocument().getId());
							}

							if (lastAddress == null || !lastAddress.getMetaname().equals(a.getMetaname())) {
								Document dTrack = new Document();
								DocumentThing dtTrack = new DocumentThing();
								DocumentItem diTrack = new DocumentItem();
								DocumentField dfTrack = new DocumentField();

								dTrack.setCode(dMaster.getCode());
								dTrack.setMetaname(dmTrack.getMetaname());
								dTrack.setName(dmTrack.getName());
								dTrack.setParent(dMaster);
								dTrack.setStatus(dMaster.getStatus());
								dTrack.setModel(dmTrack);
								getRegSvc().getDcSvc().persist(dTrack);

								diTrack.setDocument(dTrack);
								diTrack.setProduct(pd);
								diTrack.setQty(1);
								diTrack.setMeasureUnit("TR");
								getRegSvc().getDiSvc().persist(diTrack);

								dtTrack.setDocument(dTrack);
								dtTrack.setThing(t);
								getRegSvc().getDtSvc().persist(dtTrack);

								dfTrack.setField(dmfTrack);
								dfTrack.setDocument(dTrack);
								dfTrack.setValue(a.getMetaname());
								getRegSvc().getDfSvc().persist(dfTrack);
							}
							t.setAddress(a);
							getRegSvc().getThSvc().persist(t);
							//update interface
							{
								Map<String, Object> message = new LinkedHashMap<>();
								Map<String, Object> data = new LinkedHashMap<>();
								Map<String, String> props = new LinkedHashMap<>();
								String icon = "question-mark.svg";
								String iconModel = (String) getParametros().get("iconModel");
								String iconMeta = (String) getParametros().get("iconMeta");

								for (Property pr : t.getProperties()) {
									props.put(pr.getField().getMetaname(), pr.getValue());
								}

								if (iconModel.equals("Product"))
									icon = pd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals(iconMeta)).findFirst().get().getValue();
								message.put("action", "LocationData");
								data.put("latitude", payload.getLatitude());
								data.put("longitude", payload.getLongitude());
								data.put("altitude", payload.getAltitude());
								data.put("speed", 0.0d);
								data.put("tagId", t.getUnitModel().stream().filter(u -> u.getType() == UnitType.valueOf((String) getParametros().get("updateUnitType"))).findFirst().get());
								data.put("name", t.getName());
								data.put("icon", icon);
								data.put("address", a.getName());
								data.put("otherProperties", props);
								message.put("data", data);
								resend(new GsonBuilder().enableComplexMapKeySerialization().create().toJson(message));
							}
						} catch (NoSuchElementException nsee) {
							logger.error("Property PRMFPRODMETA not found on thing " + t.getId().toString());
							logger.trace(nsee.getLocalizedMessage(), nsee);
						}
						insideAny = true;
						lastPosition.put(tagId, a);
					} else
						logger.info(tagId + " OUTSIDE " + a.getMetaname());
				} catch (ParseException pe) {
					logger.error(pe.getLocalizedMessage());
					logger.trace(pe.getLocalizedMessage(), pe);
				}
			}

			if (!insideAny) {
				logger.info("Not inside any address");
			}
			getParametros().put("positions", lastPosition);
		}
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processBefore(com.gtp.hunter.core.model.ComplexData)
	 */
	@Override
	protected void processBefore(ComplexData rd) {
		logger.info(rd.toString());
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processAfter(com.gtp.hunter.process.model.Thing)
	 */
	@Override
	protected void processAfter(Thing rd) {
		if (rd != null) {
			if (rd.getAddress() != null) {
				logger.info("Address after: " + rd.getAddress().getMetaname());
			}
		}
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
