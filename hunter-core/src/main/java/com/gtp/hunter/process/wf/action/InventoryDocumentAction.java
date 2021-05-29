package com.gtp.hunter.process.wf.action;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.util.InventoryComparison;

public class InventoryDocumentAction extends DocumentAction {
	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String				END_STATUS	= "CONTADO";
	private static final String				INIT_STATUS	= "ATIVO";

	public InventoryDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		getAs().onNext(getDoc());
	}

	@Override
	public void onMessage(Object msg) {
		try {
			Document rec = gs.fromJson(msg.toString(), Document.class);
			Document doc = getRegSvc().getDcSvc().findById(rec.getId());

			if (doc != null) {
				getAs().onNext(IntegrationReturn.OK);
				for (Document ds : rec.getSiblings()) {
					if (ds.getModel().getMetaname().equals("APORUAINV")) {
						String val = ds.getFields().stream().filter(df -> df.getField().getMetaname().equals("INVADDRESS")).findAny().get().getValue();

						Document tmpSib = doc.getSiblings().stream()
										.filter(sib -> sib.getModel().getMetaname().equals("APORUAINV"))
										.flatMap(sib -> sib.getFields().stream())
										.filter(df -> df.getValue().equals(val))
										.map(df -> df.getDocument())
										.findAny()
										.get();

						for (DocumentItem recDI : ds.getItems()) {
							Product prd = getRegSvc().getPrdSvc().findById(recDI.getProduct().getId());
							DocumentItem tmpDi = getOrCreateItem(tmpSib, recDI, prd);

							tmpDi.setQty(recDI.getQty());
							tmpDi.setProduct(prd);
							tmpDi.setStatus(END_STATUS);
							getRegSvc().getDiSvc().persist(tmpDi);
						}
						getRegSvc().getDcSvc().persist(tmpSib);
					}
				}

				for (DocumentItem tmpDI : rec.getItems()) {
					if (tmpDI.getId() == null) {
						tmpDI.setDocument(doc);
						doc.getItems().add(tmpDI);
					} else {
						Product prd = getRegSvc().getPrdSvc().findById(tmpDI.getProduct().getId());
						DocumentItem di = getOrCreateItem(doc, tmpDI, prd);

						di.setProduct(prd);
						di.setQty(tmpDI.getQty());
						di.setStatus(END_STATUS);
						doc.getItems().add(di);
					}
				}
				doc.setUser(getUser());
				doc.setStatus(END_STATUS);
				getRegSvc().getDcSvc().persist(doc);
				{//Checa o parent pra ver se todas as contagens estão finalizadas
					Document parent = getRegSvc().getDcSvc().findParent(doc.getId());
					List<Document> siblings = parent.getSiblings().stream()
									.filter(ds -> ds.getModel().getId().equals(doc.getModel().getId()))
									.collect(Collectors.toList());
					String docPref = doc.getCode().substring(0, doc.getCode().indexOf("-"));
					boolean allDone = siblings.size() > 1 && siblings.stream()
									.allMatch(ds -> !ds.getStatus().equals(INIT_STATUS));
					boolean otherDone = siblings.size() > 1 && siblings.stream()
									.filter(ds -> ds.getCode().startsWith(docPref))
									.allMatch(ds -> ds.getStatus().equals(END_STATUS));

					if (otherDone) {
						List<Document> contList = siblings.stream()
										.filter(ds -> ds.getCode().startsWith(doc.getCode().substring(0, doc.getCode().indexOf("-"))))
										.sorted((ds1, ds2) -> {
											return ds2.getCode().compareTo(ds1.getCode());
										})
										.collect(Collectors.toList());
						String maxCode = contList.get(0).getCode();
						Set<UUID> addSet = contList.parallelStream()
										.flatMap(d -> d.getSiblings().parallelStream())
										.flatMap(ds -> ds.getFields().stream())
										.filter(df -> df.getField().getMetaname().equals("INVADDRESS") && df.getValue() != null && !df.getValue().isEmpty())
										.map(df -> UUID.fromString(df.getValue()))
										.collect(Collectors.toSet());//Inicia com todos os endereços divergentes
						Set<UUID> addOkSet = new HashSet<>();

						while (!contList.isEmpty()) {
							Document doc1 = contList.remove(0);

							for (Document doc2 : contList) {
								logger.info("Comparing " + doc1.getCode() + " with " + doc2.getCode());
								InventoryComparison invComp = new InventoryComparison(doc1, doc2).compareAdd();

								addOkSet.addAll(invComp.getSibsOk().parallelStream()
												.flatMap(d -> d.getFields().stream())
												.filter(df -> df.getField().getMetaname().equals("INVADDRESS") && !df.getValue().isEmpty())
												.map(df -> UUID.fromString(df.getValue()))
												.collect(Collectors.toSet()));//Remove todos os endereços OK
							}
							addSet.removeIf(a -> addOkSet.contains(a));
							logger.info("Div: " + addSet.size());
						}

						//						getRegSvc().getDiSvc().multiPersist(parent.getItems());
						if (!addSet.isEmpty()) {
							DocumentModel dmCount = doc.getModel();
							String code = maxCode.substring(0, maxCode.indexOf("-")).replace("CNT", "");
							int seq = Integer.parseInt(maxCode.substring(maxCode.indexOf("-") + 1)) + 1;
							Document dCount = new Document(dmCount, dmCount.getName() + " " + (code.substring(0, 5) + ' ' + code.substring(5)) + "-" + seq, "CNT" + code + "-" + seq, "ATIVO");
							DocumentModelField dmfWarehouse = dmCount.getFields().stream().filter(dmf -> dmf.getMetaname().equals("WAREHOUSE")).findAny().get();
							DocumentModelField dmfType = dmCount.getFields().stream().filter(dmf -> dmf.getMetaname().equals("CONTTYPE")).findAny().get();
							DocumentField dfOldWarehouse = doc.getFields().stream().filter(df -> df.getField().getId().equals(dmfWarehouse.getId())).findAny().get();
							DocumentField dfOldType = doc.getFields().stream().filter(df -> df.getField().getId().equals(dmfType.getId())).findAny().get();
							DocumentField dfWarehouse = new DocumentField(dCount, dmfWarehouse, "NOVO", dfOldWarehouse.getValue());
							DocumentField dfType = new DocumentField(dCount, dmfType, "NOVO", dfOldType.getValue());
							DocumentModel dmAri = getRegSvc().getDmSvc().findByMetaname("APORUAINV");
							DocumentModelField dmfAddr = dmAri.getFields().stream().filter(dmf -> dmf.getMetaname().equals("INVADDRESS")).findAny().get();
							for (UUID aId : addSet) {//todos os enderecos nok
								Address a = getRegSvc().getAddSvc().findById(aId);
								Prefix pfxAri = getRegSvc().getPfxSvc().findNext("ARI", 10);
								Document dAri = new Document(dmAri, dmAri.getName() + " - " + pfxAri.getCode(), pfxAri.getPrefix() + pfxAri.getCode(), "NOVO");
								DocumentField dfAddr = new DocumentField(dAri, dmfAddr, "NOVO", a.getId().toString());

								dAri.getFields().add(dfAddr);
								dAri.setParent(dCount);
								dCount.getSiblings().add(dAri);
							}
							dCount.getFields().add(dfWarehouse);
							dCount.getFields().add(dfType);
							dCount.setParent(parent);
							getRegSvc().getDcSvc().persist(dCount);
						} else if (allDone) {
							getRegSvc().getDcSvc().completeInventory(parent.getId());
						}
					}
				}
			} else
				getAs().onNext(new IntegrationReturn(false, "Documento Inválido - " + rec.getName()));
		} catch (Exception e) {
			getAs().onNext(new IntegrationReturn(false, "JSON MAL FORMADO - " + msg));
			e.printStackTrace();
		}
	}

	private DocumentItem getOrCreateItem(Document tmpSib, DocumentItem recDI, Product prd) {
		return tmpSib.getItems().stream()
						.filter(di -> di == null || di.getId() == null ? false : di.getId().equals(recDI.getId()))
						.findAny()
						.orElseGet(() -> {
							DocumentItem r = new DocumentItem(tmpSib, prd, recDI.getQty(), recDI.getMeasureUnit());

							for (String k : recDI.getProperties().keySet()) {
								r.getProperties().put(k, recDI.getProperties().get(k));
							}
							r.setStatus(END_STATUS);
							tmpSib.getItems().add(r);
							return r;
						});
	}

	@OnClose
	@Override
	protected void close(Session ss, CloseReason cr) {
		logger.info("Closed: " + cr.getReasonPhrase());
	}
}
