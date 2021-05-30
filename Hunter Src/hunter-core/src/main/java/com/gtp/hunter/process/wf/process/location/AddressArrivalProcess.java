/**
 * 
 */
package com.gtp.hunter.process.wf.process.location;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.payload.LocatePayload;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Thing;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author Mateus Tormin
 *
 */
public class AddressArrivalProcess extends LocationProcess {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Set<Address>					addresses;

	private DocumentModel					childModel;
	private DocumentModelField				childAddresModelField;

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#checkParams()
	 */
	@Override
	protected void checkParamsImpl() throws Exception {
		if (!getParametros().containsKey("master-document-meta"))
			throw new Exception("Parâmetro 'master-document-meta' não encontrado.");
		if (!getParametros().containsKey("child-document-meta"))
			throw new Exception("Parâmetro 'child-document-meta' não encontrado.");
		if (!getParametros().containsKey("child-document-name-prefx"))
			throw new Exception("Parâmetro 'child-document-name-prefx' não encontrado.");
		if (!getParametros().containsKey("child-document-code-prefx"))
			throw new Exception("Parâmetro 'child-document-code-prefx' não encontrado.");
		if (!getParametros().containsKey("target-master-address-field-meta"))
			throw new Exception("Parâmetro 'target-master-address-field-meta' não encontrado.");
		if (!getParametros().containsKey("child-address-field-meta"))
			throw new Exception("Parâmetro 'child-address-field-meta' não encontrado.");

	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#connect()
	 */
	@Override
	protected void connect() {
		logger.info("Connect");
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#timeout(java.util.Map)
	 */
	@Override
	public void timeout(Map<String, Thing> itens) {
		final boolean verbose = ConfigUtil.get("hunter-process", "verbose-process", "false").equalsIgnoreCase("true");

		if (itens != null) {
			WKTReader rdr = new WKTReader();

			for (String tagId : itens.keySet()) {
				try {
					Thing t = itens.get(tagId);
					LocatePayload payload = new GsonBuilder().create().fromJson(t.getPayload(), LocatePayload.class);
					DocumentThing dt = getRegSvc().getDtSvc().quickFindByThingIdAndDocModelMeta(t.getId(), (String) getParametros().get("master-document-meta"));

					if (dt != null) {
						try {
							Document dMaster = getRegSvc().getDcSvc().findById(dt.getDocument().getId());
							String point = "POINT(" + payload.getLongitude() + " " + payload.getLatitude() + ")";
							Geometry g = rdr.read(point);

							if (dMaster.getStatus().equalsIgnoreCase(getModel().getEstadoDe())) {
								Optional<DocumentField> optDF = dMaster.getFields().stream().filter(df -> df.getField().getMetaname().equals((String) getParametros().get("target-master-address-field-meta"))).findFirst();

								if (optDF.isPresent()) {
									Address target = getRegSvc().getAddSvc().findById(UUID.fromString(optDF.get().getValue()));

									if (target.getRegion().contains(g)) {
										logger.info("Transporte " + dMaster.getCode() + " chegou em " + target.getName());
										createChild(dMaster, t, target);
										getParametros().put("doc", dMaster);
									} else if (verbose) {
										logger.info("Point " + point + " NOT INSIDE " + target.getWkt());
									}
								} else if (verbose)
									logger.info((String) getParametros().get("target-master-address-field-meta") + " not present on " + dMaster.getName() + " fields");
							} else if (verbose)
								logger.info("DocumentStatus: " + dMaster.getStatus() + " Process Requires: " + getModel().getEstadoDe());
						} catch (ParseException pe) {
							logger.error(pe.getLocalizedMessage());
							logger.trace(pe.getLocalizedMessage(), pe);
						}
					} else if (verbose)
						logger.info("Tag " + tagId + " is not associated with any document (NO DOCUMENTTHING)");
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		} else if (verbose)
			logger.info("Timeout without items");
		this.runSucess();
	}

	private Document createChild(Document dMaster, Thing t, Address a) {
		Document dChild = new Document(childModel, (String) getParametros().get("child-document-name-prefx") + " " + dMaster.getCode(), (String) getParametros().get("child-document-code-prefx") + dMaster.getCode(), "NOVO");
		DocumentField dfChildAddr = new DocumentField();

		dfChildAddr.setDocument(dChild);
		dfChildAddr.setField(childAddresModelField);
		dfChildAddr.setValue(a.getId().toString());
		dChild.setParent(dMaster);
		dChild.getFields().add(dfChildAddr);
		dMaster.setStatus(getModel().getEstadoPara());
		dMaster.getSiblings().add(dChild);
		t.setStatus(getModel().getEstadoPara());
		getRegSvc().getThSvc().persist(t);
		getRegSvc().getDcSvc().persist(dChild);
		getRegSvc().getDcSvc().persist(dMaster);
		return dChild;
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processBefore(com.gtp.hunter.core.model.ComplexData)
	 */
	@Override
	protected void processBefore(ComplexData rd) {
		//		logger.info(rd.toString());
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
		this.childModel = getRegSvc().getDmSvc().findByMetaname((String) getParametros().get("child-document-meta"));
		this.childAddresModelField = getRegSvc().getDmfSvc().findByMetaname((String) getParametros().get("child-address-field-meta"));
		if (this.childModel == null)
			throw new RuntimeException("Parameter '" + (String) getParametros().get("child-document-meta") + "' invalid, couldn't findDocumentModelField.");
		if (this.childAddresModelField == null)
			throw new RuntimeException("Parameter '" + (String) getParametros().get("child-address-field-meta") + "' invalid, couldn't findDocumentModelField.");
		logger.info("Process Initialized!");
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
