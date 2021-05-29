package com.gtp.hunter.custom.solar.rest;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.realpicking.RPDeliveryDTO;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPConfCegaMsgDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPRecusaDocDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReturnDTO;
import com.gtp.hunter.custom.solar.sap.worker.BaseWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWConferenciaCegaTranspWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWConferenciaCegaWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWInformacaoChegadaWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWInformacaoInventarioWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWInformacaoVeiculoWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWOrdemProducaoWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWProdutosWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWProntaEntregaWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWTransferenciaWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWVasilhameGeralWorker;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.RealpickingService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.PDFUtil;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.util.DocumentModels;
import com.gtp.hunter.process.model.util.Products;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;

@RequestScoped
@Path("/testSAP")
public class SolarRest {

	@Inject
	private SAPSolar			solar;

	@Inject
	private IntegrationService	iSvc;

	@Inject
	private SAPService			svc;

	@Inject
	private RealpickingService	rpSvc;

	@Inject
	private Logger				logger;

	@GET
	public void teste() {
		System.out.println("@GET Basic");
	}

	@GET
	@Path("/{rfc}")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public String testSAP(@PathParam("rfc") String rfc) {

		return execute(rfc, new HashMap<String, String>());
	}

	@GET
	@Path("/RfcAndCode/{rfc}/{code}")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public String testWithRfcAndCodeSAP(@PathParam("rfc") String rfc, @PathParam("code") String code) {
		return execute(rfc, new HashMap<String, String>() {
			private static final long serialVersionUID = -2889376381650737751L;

			{
				put(Constants.I_CODE, code);
			}
		});
	}

	@GET
	@Path("/RfcAndControle/{rfc}/{controle}")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public String testWithRfcAndControleSAP(@PathParam("rfc") String rfc, @PathParam("controle") String controle) {
		return execute(rfc, new HashMap<String, String>() {
			private static final long serialVersionUID = 1556536539780673854L;

			{
				put(Constants.I_CONTROLE, controle);
			}
		});
	}

	@GET
	@Path("RfcAndCodeAndControle/{rfc}/{code}/{controle}")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public String testWithCompleteParamsSAP(@PathParam("rfc") String rfc, @PathParam("code") String code, @PathParam("controle") String controle) {
		return execute(rfc, new HashMap<String, String>() {
			private static final long serialVersionUID = 7538195986522688700L;

			{
				put(Constants.I_CODE, code);
				put(Constants.I_CONTROLE, controle);
			}
		});
	}

	@GET
	@Path("/execute")
	@Produces(MediaType.TEXT_HTML)
	public String execWithCompleteParamsSAP(@QueryParam("rfc") String rfc, @QueryParam("code") String code, @QueryParam("controle") String controle) {
		return execute(rfc, new HashMap<String, String>() {
			private static final long serialVersionUID = 1949985182267418101L;

			{
				if (code != null) {
					put(Constants.I_CODE, code);
				}

				if (controle != null) {
					put(Constants.I_CONTROLE, controle);
				}
			}
		});
	}

	@GET
	@Path("/produtos")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public String testProdsMassiva() {
		return execute("Z_HW_PRODUTOS", new HashMap<String, String>() {
			private static final long serialVersionUID = -6475330764274249899L;

			{
				put(Constants.I_MASSIVA, "X");
			}
		});
	}

	@GET
	@Path("/produtosCargaProdutos")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public void testProdsExecutecargaMassiva() throws JCoException {
		final Gson gson = new GsonBuilder().create();
		ToJsonSAP jcoSonReadStart = new ToJsonSAP(solar.getFunc(Constants.RFC_PRODUTOS));
		jcoSonReadStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = -1691020520640296506L;

			{
				put(Constants.I_MASSIVA, "X");
			}
		});
		String resultJsonReadStart = jcoSonReadStart.execute(solar.getDestination());
		ReadFieldsSap readFieldsSap = gson.fromJson(resultJsonReadStart, ReadFieldsSap.class);
		ZHWProdutosWorker worker = new ZHWProdutosWorker(svc, solar, iSvc);

		worker.registerProducts(readFieldsSap);
	}

	@GET
	@Path("/produtosCargaDocument/{code}")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public void testProdsExecuteCargaDocument(@PathParam("code") String code) throws JCoException {
		final Gson gson = new GsonBuilder().create();
		ToJsonSAP jcoSonReadStart = new ToJsonSAP(solar.getFunc(Constants.RFC_START));
		jcoSonReadStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = 9219456901486082873L;

			{
				put(Constants.I_CODE, code);
			}
		});
		ReadFieldsSap readFieldsSap = gson.fromJson(jcoSonReadStart.execute(solar.getDestination()), ReadFieldsSap.class);
		ZHWInformacaoChegadaWorker worker = new ZHWInformacaoChegadaWorker(svc, solar, iSvc);
		for (SAPReadStartDTO rstart : readFieldsSap.getReadStartDTOs()) {
			worker.work(rstart);
		}
	}

	@GET
	@Path("/cargaDocument/{code}/{control}")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public void testExecuteCargaDocument(@PathParam("code") int code, @PathParam("control") String control) throws JCoException {
		final Gson gson = new GsonBuilder().create();
		ToJsonSAP jcoSonReadStart = new ToJsonSAP(solar.getFunc(Constants.RFC_START));
		jcoSonReadStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = -5052014764973863472L;

			{
				put(Constants.I_CODE, code);
				put(Constants.I_CONTROLE, control);
			}
		});
		try {
			ReadFieldsSap readFieldsSap = gson.fromJson(jcoSonReadStart.execute(solar.getDestination()), ReadFieldsSap.class);
			BaseWorker worker = null;
			switch (code) {
				case Constants.CODE_NF:
					worker = new ZHWInformacaoChegadaWorker(svc, solar, iSvc);
					break;
				case Constants.CODE_INVENTARIO:
					worker = new ZHWInformacaoInventarioWorker(svc, solar, iSvc);
					break;
				case Constants.CODE_PLANPROD:
					worker = new ZHWOrdemProducaoWorker(svc, solar, iSvc);
					break;
				case Constants.CODE_TRANSFERENCIA:
					worker = new ZHWTransferenciaWorker(svc, solar, iSvc);
					break;
				case Constants.CODE_VEICULO:
					worker = new ZHWInformacaoVeiculoWorker(svc, solar, iSvc);
					break;
				case Constants.CODE_SOLICRESERVA:
					//SOLICITACAO_RESERVA_MATERIAL
					break;
			}
			for (SAPReadStartDTO rstart : readFieldsSap.getReadStartDTOs()) {
				if (worker != null) {
					worker.work(rstart);
				}
			}
		} catch (JCoException je) {
			logger.error(je.getLocalizedMessage());
			logger.trace(je.getLocalizedMessage(), je);
		}
	}

	@GET
	@Path("/cargaInventario/{control}")
	@PermitAll
	@Produces(MediaType.TEXT_HTML)
	public void testExecuteCargaInventario(@PathParam("control") String control) throws JCoException {
		final Gson gson = new GsonBuilder().create();
		ToJsonSAP jcoSonReadStart = new ToJsonSAP(solar.getFunc(Constants.RFC_START));
		jcoSonReadStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = -5052014764973863472L;

			{
				put(Constants.I_CODE, Constants.CODE_INVENTARIO);
				put(Constants.I_CONTROLE, control);
			}
		});
		try {
			ReadFieldsSap readFieldsSap = gson.fromJson(jcoSonReadStart.execute(solar.getDestination()), ReadFieldsSap.class);
			BaseWorker worker = new ZHWInformacaoInventarioWorker(svc, solar, iSvc);

			for (SAPReadStartDTO rstart : readFieldsSap.getReadStartDTOs()) {
				if (worker != null) {
					worker.work(rstart);
				}
			}
		} catch (JCoException je) {
			logger.error(je.getLocalizedMessage());
			logger.trace(je.getLocalizedMessage(), je);
		}
	}

	@GET
	@Path("/testeInventario/{code}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn testInventario(@PathParam("code") String docCode) {
		IntegrationReturn iRet = IntegrationReturn.OK;
		DocumentModel dm = iSvc.getRegSvc().getDmSvc().findByMetaname("SAPINVENTORY");
		Document d = iSvc.getRegSvc().getDcSvc().findByModelAndCode(dm, docCode);
		ZHWInformacaoInventarioWorker worker = new ZHWInformacaoInventarioWorker(svc, solar, iSvc);

		if (d != null)
			worker.external(d);
		else
			iRet = new IntegrationReturn(false, "Documento não encontrado");
		return iRet;
	}

	@GET
	@Path("/conferencia/{id}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn testConferencia(@PathParam("id") String id) throws JCoException {
		Document d = iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(id));
		if (d != null) {
			ZHWConferenciaCegaWorker worker = new ZHWConferenciaCegaWorker(svc, solar, iSvc);

			worker.external(d);
		}
		return IntegrationReturn.OK;
	}

	@GET
	@Path("/conferencia-transp/{id}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn testConferenciaTransp(@PathParam("id") String id) throws JCoException {
		Document d = iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(id));
		if (d != null) {
			ZHWConferenciaCegaTranspWorker worker = new ZHWConferenciaCegaTranspWorker(svc, solar, iSvc);

			worker.external(d);
		}
		return IntegrationReturn.OK;
	}

	@GET
	@Path("/chegada/{code}/{controle}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn testchegada(@PathParam("code") int code, @PathParam("controle") String controle) throws JCoException {
		svc.call(code, controle);
		return IntegrationReturn.OK;
	}

	@GET
	@Path("/vasilhame/{sku}")
	@PermitAll
	public Response testVasilhame(@PathParam("sku") String sku) {
		return Response.ok(new ZHWVasilhameGeralWorker(svc, solar, iSvc).getVasilhameGeral(sku)).build();
	}

	private String execute(String rfc, HashMap<String, String> params) {
		StringBuilder sb = new StringBuilder("<html><head><title>TESTESAP");
		sb.append("<style>table{display: flex;}</style>");
		sb.append("</title><body>");
		Map<String, JCoTable> tbls = solar.callSAP(rfc, params, null);
		int i = 0;

		for (String s : tbls.keySet()) {
			JCoTable tbl = tbls.get(s);
			sb.append(s);
			sb.append("<table>");
			sb.append("<tr>");
			JCoFieldIterator campos = tbl.getFieldIterator();
			do {
				JCoField campo = campos.nextField();
				sb.append("<th>" + campo.getName() + "(" + campo.getDescription() + ")</th>");
			} while (campos.hasNextField());
			sb.append("</tr>");
			if (tbl.getNumRows() > 0) {
				tbl.firstRow();
				do {
					sb.append("<tr>");
					sb.append("<td>" + Integer.toString(++i) + "</td>");
					for (int col = 0; col < tbl.getFieldCount(); col++) {
						sb.append("<td>" + tbl.getString(col) + "</td>");
					}
					sb.append("</tr>");
				} while (tbl.nextRow());
			}
			sb.append("</table>");
			sb.append("<hr>");
		}
		sb.append("</body></html>");
		return sb.toString();
	}

	@GET
	@Path("/loadSAP/{code}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn loadSAPCode(@PathParam("code") int code) {
		final Gson gson = new GsonBuilder().create();
		ToJsonSAP jcoSonReadStart = new ToJsonSAP(solar.getFunc("Z_HW_READ_START"));
		jcoSonReadStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = -5052014764973863472L;

			{
				put(Constants.I_CODE, code);
			}
		});
		try {
			ReadFieldsSap readFieldsSap = gson.fromJson(jcoSonReadStart.execute(solar.getDestination()), ReadFieldsSap.class);
			BaseWorker worker = null;
			switch (code) {
				case 1:
					worker = new ZHWInformacaoChegadaWorker(svc, solar, iSvc);
					break;
				case 8:
					worker = new ZHWOrdemProducaoWorker(svc, solar, iSvc);
					break;
				case 13:
					//SOLICITACAO_RESERVA_MATERIAL
					break;
			}
			for (SAPReadStartDTO rstart : readFieldsSap.getReadStartDTOs()) {
				if (worker != null) {
					worker.work(rstart);
				}
			}
		} catch (JCoException je) {
			logger.error(je.getLocalizedMessage());
			logger.trace(je.getLocalizedMessage(), je);
			return new IntegrationReturn(false, je.getLocalizedMessage());
		}
		return IntegrationReturn.OK;
	}

	@POST
	@Path("/transfMP")
	@Produces(MediaType.TEXT_PLAIN)
	public String testTransfer(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		JCoFunction func = solar.getFunc(Constants.RFC_TRANSFERENCIA);
		ToJsonSAP jcoSonStart = new ToJsonSAP(func);

		item.put("MANDT", jsonObj.getString("MANDT"));
		item.put("CODE", jsonObj.getString("CODE"));
		item.put("IDENT", jsonObj.getString("IDENT"));
		item.put("CENTRO", jsonObj.getString("CENTRO"));
		item.put("MATERIAL", jsonObj.getString("MATERIAL"));
		item.put("DOCUMENTO", jsonObj.getString("DOCUMENTO"));
		item.put("ANO", jsonObj.getString("ANO"));
		item.put("TIPOMOV", jsonObj.getString("TIPOMOV"));
		item.put("DEPORIGEM", jsonObj.getString("DEPORIGEM"));
		item.put("DEPDESTINO", jsonObj.getString("DEPDESTINO"));
		item.put("QUANTIDADE", Double.parseDouble(jsonObj.getString("QUANTIDADE")));
		item.put("UNID_MED", jsonObj.getString("UNID_MED"));
		item.put("DEBCRED", jsonObj.getString("DEBCRED"));
		item.put("TIPO_NRHUNTER", jsonObj.getString("TIPO_NRHUNTER"));
		ret.add(item);
		jcoSonStart.setTableParameter(Constants.TBL_TRANSFERENCIA, ret);

		ReadFieldsSap readFieldsSap = null;

		try {
			logger.info(func.toString());
			retHTML = jcoSonStart.execute(solar.getDestination());

			readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retHTML = e.getLocalizedMessage();
		} catch (JCoException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retHTML = e.getLocalizedMessage();
		}

		for (SAPReturnDTO msg : readFieldsSap.getReturnDTOs()) {
			AlertSeverity sev = AlertSeverity.INFO;

			if (msg.getTipo().equals("E")) {
				sev = AlertSeverity.ERROR;
			} else if (msg.getTipo().equals("W")) sev = AlertSeverity.WARNING;
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.DOCUMENT, sev, "TESTE-INTERFACE", msg.getMensagem(), msg.getSeq()));
		}
		return retHTML;
	}

	@POST
	@Path("/testConf")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String tstConf(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		ToJsonSAP jcoSonStart = new ToJsonSAP(solar.getFunc(Constants.RFC_CONFERENCIA_CEGA));

		item.put("MANDT", "120");//FIXED
		item.put("NUMERO_NF", jsonObj.getString("NUMERO_NF"));//NUMERO NF
		item.put("SERIE_NF", jsonObj.getString("SERIE_NF"));//SERIE NF
		item.put("DATA_NF", jsonObj.getString("DATA_NF"));//DATA NF
		item.put("EBELN", jsonObj.getString("EBELN"));//DF da NFENTRADA
		item.put("MATNR", jsonObj.getString("MATNR"));//SKU
		item.put("QTDE_CONTADA", jsonObj.getString("QTDE_CONTADA"));//QTD CONTADA
		item.put("LGORT", jsonObj.getString("LGORT"));//destino
		item.put("CENTRO", jsonObj.getString("CENTRO"));//destino
		ret.add(item);
		jcoSonStart.setTableParameter("T_ZWH_CONFCEGA", ret);

		ReadFieldsSap readFieldsSap = null;
		try {
			retHTML = jcoSonStart.execute(solar.getDestination());

			readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retHTML = e.getLocalizedMessage();
		} catch (JCoException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retHTML = e.getLocalizedMessage();
		}

		for (int i = 0; i < readFieldsSap.getConfCegaDTOs().size(); i++) {
			SAPConfCegaMsgDTO msg = readFieldsSap.getConfCegaDTOs().get(i);
			AlertSeverity sev = AlertSeverity.INFO;

			if (msg.getTipo().equals("E")) {
				sev = AlertSeverity.ERROR;
			} else if (msg.getTipo().equals("W")) sev = AlertSeverity.WARNING;
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, "TESTE-INTERFACE", msg.getMensagem(), String.valueOf(i)));
		}
		return retHTML;
	}

	@POST
	@Path("/testAPOPrd")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String tstAPOPrd(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		JCoFunction func = solar.getFunc(Constants.RFC_PSC);

		if (func != null) {
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);

			item.put(Constants.I_PLANT, jsonObj.getString(Constants.I_PLANT));
			item.put(Constants.I_ORDER_NUMBER, jsonObj.getString(Constants.I_ORDER_NUMBER));
			item.put(Constants.I_QTD_PRODUCED, jsonObj.getString(Constants.I_QTD_PRODUCED));
			jcoSonStart.setParameters(item);

			ReadFieldsSap readFieldsSap = null;
			try {
				retHTML = jcoSonStart.execute(solar.getDestination());

				readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
				logger.info("Function Return {}", readFieldsSap.geteReturn());
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}

			for (int i = 0; i < readFieldsSap.getReturnDTOs().size(); i++) {
				SAPReturnDTO msg = readFieldsSap.getReturnDTOs().get(i);
				AlertSeverity sev = AlertSeverity.INFO;

				if (msg.getType().equals("E")) {
					sev = AlertSeverity.ERROR;
				} else if (msg.getType().equals("W")) sev = AlertSeverity.WARNING;
				iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, "TESTE-INTERFACE", msg.getMessage(), String.valueOf(i)));
			}
		} else
			retHTML = "RFC " + Constants.RFC_PSC + " Inexistente no SAP";
		return retHTML;
	}

	@POST
	@Path("/restartNF")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String restartNF(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		JCoFunction func = solar.getFunc(Constants.RFC_REPROCESSO_NF);
		if (func != null) {
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);
			item.put(Constants.I_CHAVE, jsonObj.getString(Constants.I_CHAVE));
			jcoSonStart.setParameters(item);

			ReadFieldsSap readFieldsSap = null;
			try {
				retHTML = jcoSonStart.execute(solar.getDestination());

				readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
				logger.info("Function Return {}", readFieldsSap.getReturnDTOs());
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}

			for (int i = 0; i < readFieldsSap.getReturnDTOs().size(); i++) {
				SAPReturnDTO msg = readFieldsSap.getReturnDTOs().get(i);
				AlertSeverity sev = AlertSeverity.INFO;

				if (msg.getType().equals("E")) {
					sev = AlertSeverity.ERROR;
				} else if (msg.getType().equals("W")) sev = AlertSeverity.WARNING;
				iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, jsonObj.getString(Constants.I_CHAVE), msg.getMessage(), "REPROCESSAR NOTA FISCAL NO SAP"));
			}
		} else
			retHTML = "RFC " + Constants.RFC_REPROCESSO_NF + " Inexistente no SAP";
		return retHTML;
	}

	@POST
	@Path("/deleteConf")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String deleteConf(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		JCoFunction func = solar.getFunc(Constants.RFC_DELETE_CONF_CEGA);
		if (func != null) {
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);

			item.put(Constants.I_CHAVE, jsonObj.getString(Constants.I_CHAVE));
			jcoSonStart.setParameters(item);

			ReadFieldsSap readFieldsSap = null;
			try {
				retHTML = jcoSonStart.execute(solar.getDestination());

				readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
				logger.info("Function Return {}", readFieldsSap.getReturnDTOs());
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}

			for (int i = 0; i < readFieldsSap.getReturnDTOs().size(); i++) {
				SAPReturnDTO msg = readFieldsSap.getReturnDTOs().get(i);
				AlertSeverity sev = AlertSeverity.INFO;

				if (msg.getType().equals("E")) {
					sev = AlertSeverity.ERROR;
				} else if (msg.getType().equals("W")) sev = AlertSeverity.WARNING;
				iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, "REPROCESSAR NOTA FISCAL NO SAP", msg.getMessage(), String.valueOf(i)));
			}
		} else
			retHTML = "RFC " + Constants.RFC_DELETE_CONF_CEGA + " Inexistente no SAP";
		return retHTML;
	}

	@POST
	@Path("/checkinout")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String checkInOutRota(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		JCoFunction func = solar.getFunc(Constants.RFC_CHECKINOUT);

		if (func != null) {
			logger.info(func.toString());
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);

			item.put("MANDT", jsonObj.getString("MANDT"));
			item.put("TKNUM", jsonObj.getString("TKNUM"));
			item.put("FUNCAO", jsonObj.getString("FUNCAO"));
			item.put("ID_CONF", jsonObj.getString("ID_CONF"));
			item.put("MATNR", jsonObj.getString("MATNR"));
			item.put("LFIMG", jsonObj.getString("LFIMG"));
			item.put("FINAL", jsonObj.getString("FINAL"));
			ret.add(item);
			jcoSonStart.setSimpleParameter(Constants.I_EUCATEX, Integer.parseInt(jsonObj.getString("I_EUCATEX")));
			jcoSonStart.setSimpleParameter(Constants.I_PALLET, Integer.parseInt(jsonObj.getString("I_PALLET")));
			jcoSonStart.setSimpleParameter(Constants.I_LACRE, jsonObj.getString("I_LACRE"));
			jcoSonStart.setSimpleParameter(Constants.I_CONTROLE, jsonObj.getString("TKNUM"));
			jcoSonStart.setTableParameter(Constants.TBL_CHECKINOUT, ret);

			ReadFieldsSap readFieldsSap = null;
			try {
				retHTML = jcoSonStart.execute(solar.getDestination());

				readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}

			for (int i = 0; i < readFieldsSap.getConfCegaDTOs().size(); i++) {
				SAPConfCegaMsgDTO msg = readFieldsSap.getConfCegaDTOs().get(i);
				AlertSeverity sev = AlertSeverity.INFO;

				if (msg.getTipo().equals("E"))
					sev = AlertSeverity.ERROR;
				else if (msg.getTipo().equals("W"))
					sev = AlertSeverity.WARNING;
				iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, jsonObj.getString("MATNR"), msg.getMensagem(), msg.getSeq()));
			}
		} else
			retHTML = "RFC " + Constants.RFC_CHECKINOUT + " Inexistente no SAP";
		return retHTML;
	}

	@POST
	@Path("/checkinoutportaria")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String checkInOutRotaPortaria(JsonObject jsonObj) {
		String retHTML = "";
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		JCoFunction func = solar.getFunc(Constants.RFC_CHECKINOUT_PORTARIA);

		if (func != null) {
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);
			item.put("MANDT", jsonObj.getString("MANDT"));
			item.put("TKNUM", jsonObj.getString("TKNUM"));
			item.put("SAIENT", jsonObj.getString("SAIENT"));
			item.put("CARRINHOS", jsonObj.getString("CARRINHOS"));
			item.put("CONES", jsonObj.getString("CONES"));
			item.put("KMSAIENT", Integer.valueOf(jsonObj.getString("KMSAIENT")));
			item.put("OBSERVACAO", jsonObj.getString("OBSERVACAO"));
			item.put("ITENSEGUR", jsonObj.getString("ITENSEGUR"));
			item.put("EXTINTOR", jsonObj.getString("EXTINTOR"));
			ret.add(item);
			jcoSonStart.setTableParameter(Constants.TBL_CHECKINOUT_PORTARIA, ret);

			try {
				retHTML = jcoSonStart.execute(solar.getDestination());
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}
		} else
			retHTML = "RFC " + Constants.RFC_CHECKINOUT_PORTARIA + " Inexistente no SAP";
		return retHTML;
	}

	@POST
	@Path("/recusaNF")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String recusaNF(JsonObject jsonObj) throws ParseException {
		String retHTML = "";
		JCoFunction func = solar.getFunc(Constants.RFC_RECUSA_NF);

		if (func != null) {
			logger.info(func.toString());
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);
			jcoSonStart.setParameter("I_DATA", jsonObj.getString("I_DATA"));
			jcoSonStart.setParameter("I_WERKS", jsonObj.getString("I_WERKS"));

			try {
				retHTML = jcoSonStart.execute(solar.getDestination());
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}
		} else
			retHTML = "RFC " + Constants.RFC_RECUSA_NF + " Inexistente no SAP";
		return retHTML;
	}

	@GET
	@Path("/recusa//{datrecusa}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response recusaDocSpecData(@PathParam("datrecusa") String datRecusa) {
		boolean logimmediately = false;
		Gson gson = new GsonBuilder().create();
		IntegrationReturn ret = IntegrationReturn.OK;
		Profiler prof = new Profiler();
		JCoFunction func = solar.getFunc(Constants.RFC_RECUSA_NF);
		ToJsonSAP jcoSonStart = new ToJsonSAP(func);
		jcoSonStart.setParameter("I_DATA", datRecusa);
		jcoSonStart.setParameter("I_WERKS", ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
		logger.debug(prof.step("JcoSetParameters", logimmediately));
		ReadFieldsSap readFieldsSap = null;

		try {
			readFieldsSap = gson.fromJson(jcoSonStart.execute(solar.getDestination()), ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, datRecusa, Constants.RFC_RECUSA_NF, e.getLocalizedMessage()));
		} catch (JCoException e) {
			e.printStackTrace();
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, datRecusa, Constants.RFC_RECUSA_NF, e.getLocalizedMessage()));
		}
		logger.debug(prof.step("ReadFieldsSAP", logimmediately));
		try {
			if (!readFieldsSap.geteRetorno().equals("E")) {
				DocumentModel recusaNF = iSvc.getRegSvc().getDmSvc().findByMetaname("RECUSANF");
				DocumentModel nfsModel = iSvc.getRegSvc().getDmSvc().findByMetaname("NFSAIDA");
				DocumentModelField dmfReason = DocumentModels.findField(recusaNF, "REASON");
				DocumentModelField dmfDesc = DocumentModels.findField(recusaNF, "DESCRIPTION");
				List<Document> pRecusas = new ArrayList<>();
				Set<String> skip = readFieldsSap.getRecusaNFDTOs()
								.parallelStream()
								.map(dto -> dto.getDocNum())
								.distinct()
								.collect(Collectors.toSet());

				skip.removeIf(c -> iSvc.getRegSvc().getDcSvc().findByModelAndCode(recusaNF, c) == null);
				for (SAPRecusaDocDTO dto : readFieldsSap.getRecusaNFDTOs()) {
					String docCode = dto.getDocNum();
					if (skip.contains(docCode)) continue;
					Product p = iSvc.getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(dto.getMatnr())));
					String mu = Products.getStringField(p, "GROUP_UM", "CX");
					Document dRecusa = pRecusas.parallelStream()
									.filter(d -> d.getCode().equals(docCode))
									.findAny()
									.orElse(null);

					if (dRecusa == null) {
						String nfeCode = String.join("", Collections.nCopies(10 - dto.getNfeNum().length(), "0")) + dto.getNfeNum();
						Document nfs = iSvc.getRegSvc().getDcSvc().findByModelAndCodeAndPersonCode(nfsModel, nfeCode, dto.getKunnr());

						dRecusa = new Document(recusaNF, recusaNF.getName() + docCode, docCode, "INTEGRADO");
						if (nfs != null) {
							nfs.setStatus("RECUSADO");
							dRecusa.setParent(nfs);
						}
						dRecusa.getItems().add(new DocumentItem(dRecusa, p, dto.getMenge(), "NOVO", mu));
						dRecusa.getFields().add(new DocumentField(dRecusa, dmfReason, "NOVO", String.valueOf(dto.getReason())));
						dRecusa.getFields().add(new DocumentField(dRecusa, dmfDesc, "NOVO", dto.getDescription()));
						pRecusas.add(dRecusa);
					} else if (!dRecusa.getItems().parallelStream().anyMatch(di -> di.getProduct().getId().equals(p.getId()))) {
						dRecusa.getItems().add(new DocumentItem(dRecusa, p, dto.getMenge(), "NOVO", mu));
					}

				}
				iSvc.getRegSvc().getDcSvc().multiPersist(pRecusas);
				iSvc.getRegSvc().getDcSvc().multiPersist(pRecusas.parallelStream()
								.filter(dc -> dc.getParent() != null)
								.map(dc -> dc.getParent())
								.collect(Collectors.toList()));
				iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, datRecusa, Constants.RFC_RECUSA_NF, readFieldsSap.geteMensagem()));
			} else {
				ret = new IntegrationReturn(false, readFieldsSap.geteMensagem());
				iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, datRecusa, Constants.RFC_RECUSA_NF, readFieldsSap.geteMensagem()));
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			ret = new IntegrationReturn(false, e.getLocalizedMessage());
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, datRecusa, Constants.RFC_RECUSA_NF, e.getLocalizedMessage()));
			prof.done(e.getLocalizedMessage(), logimmediately, false).forEach(logger::error);
		}
		return Response.ok(ret).build();
	}

	@POST
	@Path("/prontaEntrega")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String prontaEntrega(JsonObject jsonObj) throws ParseException {
		String retHTML = "";
		JCoFunction func = solar.getFunc(Constants.RFC_PRONTA_ENTREGA);

		if (func != null) {
			logger.info(func.toString());
			logger.info(func.getTableParameterList().getTable(Constants.TBL_PRONTA_ENTREGA).toString());
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);
			jcoSonStart.setParameter(Constants.I_TKNUM, jsonObj.getString(Constants.I_TKNUM));

			try {
				retHTML = jcoSonStart.execute(solar.getDestination());
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}
		} else
			retHTML = "RFC " + Constants.RFC_PRONTA_ENTREGA + " Inexistente no SAP";
		return retHTML;
	}

	@GET
	@Path("/prontaEntrega/{transp}")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public IntegrationReturn prontaEntregaWorker(@PathParam("transp") UUID id) throws ParseException {
		IntegrationReturn iRet = IntegrationReturn.OK;
		try {
			Document d = iSvc.getRegSvc().getDcSvc().findById(id);
			ZHWProntaEntregaWorker worker = new ZHWProntaEntregaWorker(svc, solar, iSvc);
			boolean ok = worker.external(d);

			if (!ok) iRet = new IntegrationReturn(ok, "Erro no Worker");
		} catch (Exception e) {
			iRet = new IntegrationReturn(false, e.getLocalizedMessage());
		}
		return iRet;
	}

	@POST
	@Path("/checkoutFaturado")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String checkoutFaturado(JsonObject jsonObj) throws ParseException {
		String retHTML = "";
		JCoFunction func = solar.getFunc(Constants.RFC_CHECKOUT_FATURADO);

		if (func != null) {
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);
			jcoSonStart.setParameter(Constants.I_TKNUM, jsonObj.getString(Constants.I_TKNUM));

			try {
				retHTML = jcoSonStart.execute(solar.getDestination());
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}
		} else
			retHTML = "RFC " + Constants.RFC_CHECKOUT_FATURADO + " Inexistente no SAP";
		return retHTML;
	}

	@POST
	@Path("/veiculo")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String loadVehicle(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		JCoFunction func = solar.getFunc(Constants.RFC_VEICULOS);

		if (func != null) {
			logger.info(func.toString());
			logger.info(func.getTableParameterList().getTable("I_TKNUM").toString());
			ReadFieldsSap readFieldsSap = null;
			ToJsonSAP jcoSonStart = new ToJsonSAP(func);
			String typed = jsonObj.getString("I_TKNUM").endsWith(",") ? jsonObj.getString("I_TKNUM").substring(0, jsonObj.getString("I_TKNUM").length() - 1) : jsonObj.getString("I_TKNUM");
			String[] transps = typed.split(",");

			try {
				LinkedList<LinkedHashMap<String, Object>> tblValues = new LinkedList<>();
				for (int i = 0; i < transps.length; i++) {
					LinkedHashMap<String, Object> val = new LinkedHashMap<>();

					val.put("TKNUM_NUM", transps[i]);
					tblValues.add(val);
				}
				jcoSonStart.setParameter(Constants.I_FLUXO, "L");
				jcoSonStart.setParameter(Constants.I_TKNUM, tblValues);
				//				jcoSonStart.setTableParameter(Constants.I_TKNUM, tblValues);
				retHTML = jcoSonStart.execute(solar.getDestination());
				readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
			} catch (JsonSyntaxException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			} catch (JCoException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				retHTML = e.getLocalizedMessage();
			}

			for (SAPReturnDTO msg : readFieldsSap.getReturnDTOs().stream().sorted((ret1, ret2) -> {
				return Integer.valueOf(ret1.getSeq()) - Integer.valueOf(ret2.getSeq());
			}).collect(Collectors.toList())) {
				AlertSeverity sev = AlertSeverity.INFO;

				if (msg.getTipo().equals("E"))
					sev = AlertSeverity.ERROR;
				else if (msg.getTipo().equals("W"))
					sev = AlertSeverity.WARNING;
				iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, jsonObj.getString("I_TKNUM"), msg.getMensagem(), msg.getSeq()));
			}
		} else
			retHTML = "RFC " + Constants.RFC_VEICULOS + " Inexistente no SAP";
		return retHTML;
	}

	@POST
	@PermitAll
	@Path("/loadvehicles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadVehicles() {
		try {
			List<RPDeliveryDTO> deliveryList = rpSvc.getDataRepository().getDeliveryList(new Date(), "CNAT");
			ZHWInformacaoVeiculoWorker worker = new ZHWInformacaoVeiculoWorker(svc, solar, iSvc);

			worker.registerVehicles(deliveryList.stream().map(dto -> dto.getTrip_number()).collect(Collectors.toList()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Response.ok("OK").build();
	}

	@POST
	@Path("/testCQ")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String tstCQ(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		JCoFunction func = solar.getFunc(Constants.RFC_QUALIDADE);
		ToJsonSAP jcoSonStart = new ToJsonSAP(func);

		logger.info(func.toString());
		logger.info(func.getTableParameterList().getTable(Constants.TBL_QUALIDADE).toString());
		item.put("MANDT", jsonObj.getString("MANDT"));//FIXED 120
		item.put("PLANT", jsonObj.getString("PLANT"));//CNAT
		item.put("CODE", Integer.parseInt(jsonObj.getString("CODE")));//5
		item.put("IDENT", jsonObj.getString("IDENT"));
		item.put("CONTROLE", jsonObj.getString("CONTROLE"));
		item.put("REF_DOC_NO", jsonObj.getString("REF_DOC_NO"));
		item.put("PSTNG_DATE", Calendar.getInstance().getTime());
		item.put("DOC_DAT", Calendar.getInstance().getTime());
		item.put("MATERIAL", jsonObj.getString("MATERIAL"));
		//O tipo de movimento para bloqueio é 344 e para desbloqueio é o 343.
		item.put("MOVE_TYPE", Integer.parseInt(jsonObj.getString("MOVE_TYPE")));
		item.put("MOVE_COD", jsonObj.getString("MOVE_COD"));
		item.put("BATCH", jsonObj.getString("BATCH"));
		item.put("STGE_LOC", jsonObj.getString("STGE_LOC"));
		item.put("MOVE_STLOC", jsonObj.getString("MOVE_STLOC"));
		item.put("ENTRY_QNT", jsonObj.getString("ENTRY_QNT"));
		item.put("UNID_MED", jsonObj.getString("UNID_MED"));
		item.put("MOVE_PLANT", jsonObj.getString("MOVE_PLANT"));
		item.put("MOVE_BATCH", jsonObj.getString("MOVE_BATCH"));
		item.put("TIPO_NRHUNTER", jsonObj.getString("TIPO_NRHUNTER"));
		item.put("ANO", 2019);
		item.put("DOCUMENTO", jsonObj.getString("DOCUMENTO"));
		ret.add(item);

		jcoSonStart.setTableParameter("T_ZWH_CTLQA", ret);

		ReadFieldsSap readFieldsSap = null;
		try {
			retHTML = jcoSonStart.execute(solar.getDestination());

			readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retHTML = e.getLocalizedMessage();
		} catch (JCoException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retHTML = e.getLocalizedMessage();
		}

		for (SAPReturnDTO msg : readFieldsSap.getReturnDTOs()) {
			AlertSeverity sev = AlertSeverity.INFO;

			if (msg.getTipo().equals("E")) {
				sev = AlertSeverity.ERROR;
			} else if (msg.getTipo().equals("W")) sev = AlertSeverity.WARNING;
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.DOCUMENT, sev, "0371831678", msg.getMensagem(), msg.getSeq()));
		}
		return retHTML;
	}

	@POST
	@Path("/testConfTransp")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public String tstConfTransp(JsonObject jsonObj) {
		String retHTML = "";
		Gson gson = new GsonBuilder().create();
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		JCoFunction func = solar.getFunc(Constants.RFC_CONFCEGATRANSP);
		logger.info(func.toString());
		logger.info(func.getTableParameterList().getTable(Constants.TBL_CONFERENCIA_CEGA_TRANSP).toString());
		ToJsonSAP jcoSonStart = new ToJsonSAP(func);

		logger.info(jcoSonStart.toString());
		item.put("MANDT", "120");//FIXED
		item.put("NUMERO_NF", jsonObj.getString("NUMERO_NF"));//NUMERO NF
		item.put("SERIE_NF", jsonObj.getString("SERIE_NF"));//SERIE NF
		item.put("DATA_NF", jsonObj.getString("DATA_NF"));//DATA NF
		item.put("EBELN", jsonObj.getString("EBELN"));//DF da NFENTRADA
		item.put("MATNR", jsonObj.getString("MATNR"));//SKU
		item.put("QTDE_CONTADA", jsonObj.getString("QTDE_CONTADA"));//QTD CONTADA
		item.put("LGORT", jsonObj.getString("LGORT"));//destino
		item.put("CENTRO", jsonObj.getString("CENTRO"));//destino
		ret.add(item);
		jcoSonStart.setTableParameter(Constants.TBL_CONFERENCIA_CEGA_TRANSP, ret);

		ReadFieldsSap readFieldsSap = null;
		try {
			retHTML = jcoSonStart.execute(solar.getDestination());

			readFieldsSap = gson.fromJson(retHTML, ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retHTML = e.getLocalizedMessage();
		} catch (JCoException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retHTML = e.getLocalizedMessage();
		}

		for (int i = 0; i < readFieldsSap.getConfCegaDTOs().size(); i++) {
			SAPConfCegaMsgDTO msg = readFieldsSap.getConfCegaDTOs().get(i);
			AlertSeverity sev = AlertSeverity.INFO;

			if (msg.getTipo().equals("E")) {
				sev = AlertSeverity.ERROR;
			} else if (msg.getTipo().equals("W")) sev = AlertSeverity.WARNING;
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, "TESTE-INTERFACE", msg.getMensagem(), String.valueOf(i)));
		}
		return retHTML;
	}

	@GET
	@PermitAll
	@Path("/testPDF/{filename}")
	@Produces("application/pdf")
	public Response testPDF(@PathParam("filename") String fileName) {
		List<Document> transps = new ArrayList<>();
		transps.add(iSvc.getRegSvc().getDcSvc().findById(UUID.fromString("4efebde0-be70-4481-95d6-bebaa6c042dc")));
		File file = PDFUtil.createPickMirror(fileName, transps);

		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition",
						"attachment; filename=" + fileName);
		return response.build();
	}
}
