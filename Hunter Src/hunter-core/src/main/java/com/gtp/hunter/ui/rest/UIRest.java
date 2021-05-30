package com.gtp.hunter.ui.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.ui.json.PalletHistory;
import com.gtp.hunter.ui.json.RNCProductStub;

@Path("/ui")
public class UIRest {

	@Inject
	private RegisterService			regSvc;

	@Inject
	private RegisterStreamManager	rsm;

	@GET
	@Path("/rnclist/{prdid}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<RNCProductStub> listRNCByProductId(@PathParam("prdid") UUID prdId) {
		return regSvc.getUISvc().getStubsByProductId(prdId);
	}

	@PUT
	@Path("/priority/{docId}")
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn changePriority(@Context HttpHeaders rs, @PathParam("docId") UUID docId, Integer priority) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = regSvc.getAuthSvc().getUser(token);
		IntegrationReturn iRet = IntegrationReturn.OK;
		Document doc = regSvc.getDcSvc().findById(docId);

		if (doc != null) {
			rsm.getTsm().cancelTask(usr.getId(), doc);
			Optional<DocumentModelField> optDmfPriority = doc.getModel().getFields().stream()
							.filter(dmf -> dmf.getMetaname().equals("PRIORITY"))
							.findAny();
			if (optDmfPriority.isPresent()) {
				DocumentModelField dmfPriority = optDmfPriority.get();
				DocumentField dfPrio = doc.getFields().stream()
								.filter(df -> df.getField().getId().equals(dmfPriority.getId()))
								.findAny().orElse(new DocumentField(doc, dmfPriority, "NOVO", String.valueOf(priority)));

				dfPrio.setValue(String.valueOf(priority));
				regSvc.getDfSvc().persist(dfPrio);
			} else
				iRet = new IntegrationReturn(false, "Documento Não Priorizável");
			rsm.getTsm().unlockTask(doc);
		} else
			iRet = new IntegrationReturn(false, "Documento Inexistente");

		return iRet;
	}

	@GET
	@Path("/palletHistory/{thingId}")
	@Produces(MediaType.APPLICATION_JSON)
	public PalletHistory getPalletHistory(@PathParam("thingId") UUID thingId) {
		return regSvc.getUISvc().getPalletHistory(thingId);
	}
}
