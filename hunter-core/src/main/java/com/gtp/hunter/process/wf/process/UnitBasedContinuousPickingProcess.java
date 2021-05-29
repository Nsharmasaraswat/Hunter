package com.gtp.hunter.process.wf.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.Thing;

public class UnitBasedContinuousPickingProcess extends ContinuousProcess {

	@Inject
	private static transient Logger	logger;

	private String					statusFrom;
	private String					statusTo;
	private String					statusDocThingTo;
	private String					statusDocTo;
	private String					metaProdModel;
	private String					unitDocProperty;
	private String					userDocProperty;
	private Set<Thing>				changes	= new HashSet<Thing>();

	@Override
	public void onInit() {
		this.statusDocThingTo = getParametros().get("statusDocThingTo").toString().replaceAll("\"", "");
		this.statusDocTo = getParametros().get("statusDocTo").toString().replaceAll("\"", "");
		this.metaProdModel = getParametros().get("metaProdModel").toString().replaceAll("\"", "");
		this.unitDocProperty = getParametros().get("unitDocProperty").toString().replaceAll("\"", "");
		this.userDocProperty = getParametros().get("userDocProperty").toString().replaceAll("\"", "");
		this.statusFrom = getModel().getEstadoDe();
		this.statusTo = getModel().getEstadoPara();
	}

	@Override
	public void cancel() {

	}

	@Override
	protected void connect() {

	}

	@Override
	protected void processBefore(ComplexData rd) {

	}

	@Override
	protected void processAfter(Thing rd) {

	}

	@Override
	public void timeout(Map<String, Thing> itens) {
		Profiler p = new Profiler();
		logger.debug(logPrefix() + "timeout " + (itens == null ? 0 : itens.size()));
		Document d = null;
		p.step(logPrefix() + "Inicio do Timeout", false);
		List<DocumentItem> lst = new ArrayList<DocumentItem>();
		if (itens != null && itens.size() > 0) {
			for (Thing rd : itens.values()) {
				if (rd != null) {
					if (rd.getProduct() != null) {
						if (rd.getProduct().getModel() != null) {
							if (this.metaProdModel.equals(rd.getProduct().getModel().getMetaname())) {
								// logger.debug(logPrefix() + "UNIT ENCONTRADO");
								Unit u = null;
								for (Unit tmpu : rd.getUnitModel()) {
									u = tmpu;
									break;
								}
								d = getRegSvc().getDcSvc().quickFindByPropertyValue(this.unitDocProperty, u.getTagId());
								if (d != null) {
									logger.debug(logPrefix() + "DOCUMENT ENCONTRADO");
									lst = getRegSvc().getDiSvc().quickListByDocumentId(d.getId());
								} else {
									logger.debug(logPrefix() + "DOCUMENT NÃO ENCONTRADO");
								}
								break;
							} else {
								// logger.debug(logPrefix() + "NAO É O UNIT CORRETO");
							}
						} else {
							logger.debug(logPrefix() + "RD COM PRODUCT MODEL NULL");
						}
					} else {
						logger.debug(logPrefix() + "RD COM PRODUCT NULL");
					}
				} else {
					logger.debug(logPrefix() + "RD NULL");
				}
			}
			p.step(logPrefix() + "Fim da Carga do Documento", false);
			if (lst.size() > 0) {
				p.step(logPrefix() + "Inicio da validacao", false);
				changes = new HashSet<Thing>();
				// HashSet<DocumentThing> dtChg = new HashSet<DocumentThing>();
				HashSet<Thing> tChg = new HashSet<Thing>();
				// logger.debug(logPrefix() + "PROCESSANDO OS ITENS A SEREM SEPARADOS");
				boolean cancel = false;
				List<String> toRemove = new ArrayList<String>();
				for (String cntRd : itens.keySet()) {
					Thing rd = itens.get(cntRd);
					if (rd != null && !cancel) {
						if (rd.getProduct() != null) {
							if (rd.getProduct().getModel() != null) {
								if (!this.metaProdModel.equals(rd.getProduct().getModel().getMetaname())) {
									if (this.statusFrom.equals(rd.getStatus())) {
										for (DocumentItem di : lst) {
											if (di.getProduct().getId().equals(rd.getProduct().getId())) {
												if (di.getQtdThings() >= di.getQty()) {
													cancel = true;
													rd.getErrors().add(logPrefix() + "PRODUTO A MAIS");
													rd.setCancelProcess(true);
													rd.setDocument(d.getId());
													resend(rd);
												} else {
													logger.debug(logPrefix() + "Thing Adicionado");
													rd.setDocument(d.getId());
													di.setQtdThings(di.getQtdThings() + 1);
													tChg.add(rd);
													toRemove.add(cntRd);
													changes.add(rd);
													resend(rd);
												}
											}
										}
									} else if (this.statusTo.equals(rd.getStatus())) {
										toRemove.add(cntRd);
									} else {
										cancel = true;
										rd.getErrors().add(logPrefix() + "PRODUTO EM ESTADO INCORRETO - " + rd.getStatus());
										rd.setCancelProcess(true);
										rd.setDocument(d.getId());
										resend(rd);
									}
								} else {
									toRemove.add(cntRd);
								}
							} else {
								// cancel = true;
								// rd.getErrors().add(logPrefix() + "ERRO DE INTEGRIDADE DE CADASTRO");
								// rd.setCancelProcess(true);
								// rd.setDocument(d.getId());
								// resend(rd);
							}
						} else {
							// cancel = true;
							// rd.getErrors().add(logPrefix() + "ERRO DE INTEGRIDADE DE CADASTRO");
							// rd.setCancelProcess(true);
							// rd.setDocument(d.getId());
							// resend(rd);
						}
					}
				}
				for (String s : toRemove) {
					itens.remove(s);
				}
				p.step(logPrefix() + "fim da validacao", false);
				if (!cancel && itens.isEmpty()) {
					getParametros().put("doc", d);
					getParametros().put("itens", changes);
					// for (DocumentThing dt : dtChg) {
					// getrSvc().getDtRep().persist(dt);
					// }
					p.step(logPrefix() + "Inicio da persistencia", false);
					for (Thing t : tChg) {
						getRegSvc().getThSvc().getThRep().quickUpdateThingStatus(t.getId(), this.statusTo);
						getRegSvc().getDtSvc().quickInsert(d.getId(), t.getId(), this.statusDocThingTo);
					}
					boolean alterarDoc = true;
					for (DocumentItem di : lst) {
						if (new Double(di.getQty()).intValue() > di.getQtdThings()) {
							alterarDoc = false;
							break;
						}
					}
					if (alterarDoc) {
						DocumentModelField docModelFrom = getRegSvc().getDmfSvc().findByMetaname(this.unitDocProperty);
						DocumentModelField docModelTo = getRegSvc().getDmfSvc().findByMetaname(this.userDocProperty);

						getRegSvc().getDcSvc().quickUpdateStatus(d.getId(), this.statusDocTo);
						getRegSvc().getDfSvc().quickChangeModel(d.getId(), docModelFrom.getId(), docModelTo.getId());
					}
					p.step(logPrefix() + "fim da persistencia", false);
					this.runSucess();
				} else {
					if (!itens.isEmpty()) {
						for (String s : itens.keySet()) {
							Thing t = itens.get(s);
							if (t == null)
								logger.debug(logPrefix() + "WUUUUUT " + s);
							t.getErrors().add("Item fora do escopo da nota");
							t.setDocument(d.getId());
							resend(t);
						}
						this.lockdown("Thing com produto ou status inválido.");
					} else {
						this.lockdown("Produto lido a mais.");
					}

				}

			} else {
				System.out.println(logPrefix() + "NAO ACHEI O DOC FINAL");
				this.lockdown("Documento não encontrado.");
			}
		} else {
			this.unlock();
		}
		p.done(logPrefix() + "Fim do timeout", false, true);
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
	protected void checkParams() throws Exception {
		if (!getParametros().containsKey("statusDocThingTo"))
			throw new Exception("Parâmetro 'statusDocThingTo' não encontrado.");
		if (!getParametros().containsKey("metaProdModel"))
			throw new Exception("Parâmetro 'metaProdModel' não encontrado.");
		if (!getParametros().containsKey("unitDocProperty"))
			throw new Exception("Parâmetro 'unitDocProperty' não encontrado.");
		if (!getParametros().containsKey("userDocProperty"))
			throw new Exception("Parâmetro 'userDocProperty' não encontrado.");
		if (!getParametros().containsKey("statusDocTo"))
			throw new Exception("Parâmetro 'statusDocTo' não encontrado.");
	}

	@Override
	protected void processUnknown(ComplexData rd) {
		// TODO Auto-generated method stub

	}

}
