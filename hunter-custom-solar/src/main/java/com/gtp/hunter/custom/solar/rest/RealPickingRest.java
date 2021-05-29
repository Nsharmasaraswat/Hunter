package com.gtp.hunter.custom.solar.rest;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.realpicking.RPDeliveryDTO;
import com.gtp.hunter.custom.solar.realpicking.RPDeliveryRepository;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.RealpickingService;
import com.gtp.hunter.custom.solar.util.PDFUtil;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.Product;

@RequestScoped
@Path("/realPicking")
public class RealPickingRest {

	@Inject
	private RealpickingService rpSvc;

	@POST
	@Path("/loaddeliveries/{deliveryDate}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadDeliveries(@PathParam("deliveryDate") String date) throws ParseException {
		Date deliveryDate = new SimpleDateFormat("yyyyMMdd").parse(date);

		rpSvc.checkDelivery(deliveryDate, ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
		return Response.ok().build();
	}

	@POST
	@Path("/loadtrips/{deliveryDate}")
	@Produces("application/pdf")
	public Response loadTrips(@PathParam("deliveryDate") String date) throws ParseException {
		Date deliveryDate = new SimpleDateFormat("yyyyMMdd").parse(date);
		List<Document> transps = rpSvc.checkTrips(deliveryDate, ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
		String fileName = date + new SimpleDateFormat("hhmmss").format(new Date()) + ".pdf";
		File file = PDFUtil.createPickMirror(fileName, transps);

		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition",
						"attachment; filename=" + fileName);
		return response.build();
	}

	@GET
	@Path("/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runRealPicking(@PathParam("param") String param) {
		RPDeliveryRepository dataRepository = rpSvc.getDataRepository();
		IntegrationService integrationService = rpSvc.getIntegrationService();
		HashMap<String, Object> response = new HashMap<String, Object>();

		try {
			int docsProcessados = 0;

			List<RPDeliveryDTO> dataQueryDTOs = dataRepository.getDeliveryList(Calendar.getInstance().getTime(), param);
			DocumentModel documentModelTransport = integrationService.getRegSvc().getDmSvc().findByMetaname("TRANSPORT");
			DocumentModel documentModelPicking = integrationService.getRegSvc().getDmSvc().findByMetaname("PICKING");
			DocumentModelField documentModelField = integrationService.getRegSvc().getDmfSvc().findByModelAndMetaname(documentModelTransport, "TRUCK_ID");

			for (RPDeliveryDTO dataQueryDTO : dataQueryDTOs) {

				String tripNumber = dataQueryDTO.getTrip_number();

				/***
				 * Tratando o Transporter
				 */
				Document documentTransport = integrationService.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(tripNumber, "TRANSPORT");

				if (documentTransport == null) {
					documentTransport = new Document(documentModelTransport, "TRANSPORT ".concat(tripNumber), tripNumber, "NOVO");

					integrationService.getRegSvc().getDcSvc().persist(documentTransport);

					DocumentField documentField = new DocumentField();
					documentField.setDocument(documentTransport);
					documentField.setField(documentModelField);
					documentField.setValue(dataQueryDTO.getVehicle_id());

					integrationService.getRegSvc().getDfSvc().persist(documentField);
				}

				/***
				 * Tratando o picking
				 */
				String picking = dataQueryDTO.getTrip_number().concat(dataQueryDTO.getBay_label());

				Document documentPicking = integrationService.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(picking, "PICKING");

				if (documentPicking == null) {
					documentPicking = new Document(documentModelPicking, "PICKING ".concat(picking), picking, "NOVO");
					documentPicking.setParent(documentTransport);
					integrationService.getRegSvc().getDcSvc().persist(documentPicking);
				}

				/**
				 * Document Item
				 */

				Product product = integrationService.getRegSvc().getPrdSvc().findBySKU(dataQueryDTO.getProduct_id());

				if (product != null) {
					DocumentItem documentItem = new DocumentItem();
					documentItem.setDocument(documentPicking);
					documentItem.setProduct(product);
					documentItem.setQty(dataQueryDTO.getQuantity());
					documentItem.getProperties().put("SEQ", String.valueOf(dataQueryDTO.getSequence_ticket()));
					//documentItem.getProperties().put("LAYER", dataQueryDTO.getLayer_description());
					documentItem.getProperties().put("SEQ", Integer.toString(dataQueryDTO.getSequence_ticket()));
					documentItem.getProperties().put("PRODUCT_DESCRIPTION_LONG", dataQueryDTO.getProduct_description_long());
					documentItem.getProperties().put("PRODUCT_DESCRIPTION_SHORT", dataQueryDTO.getProduct_description_short());
					documentItem.getProperties().put("HIGHLIGHT", Boolean.toString(dataQueryDTO.isHighlight()));
					documentItem.getProperties().put("SEPARATOR", Boolean.toString(dataQueryDTO.isSeparator_after_ticket()));

					integrationService.getRegSvc().getDiSvc().persist(documentItem);
				} else {
					System.out.println("Produto Não localizado. -> " + dataQueryDTO.getProduct_id());
					Alert alert = new Alert(AlertType.DOCUMENT, AlertSeverity.ERROR, dataQueryDTO.getProduct_id(),
									"Produto não encontrado.", String.format("Produto: %s", dataQueryDTO.getProduct_id()));
					integrationService.getRegSvc().getAlertSvc().persist(alert);
				}

				docsProcessados++;
			}

			response.put("Registros Processados", docsProcessados);

		} catch (Exception e) {
			response.put("Erro Capturado", e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();

		}

		return Response.ok(response).build();
	}

}
