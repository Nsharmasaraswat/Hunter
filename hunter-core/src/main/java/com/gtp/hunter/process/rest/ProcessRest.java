package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.repository.ProcessRepository;
import com.gtp.hunter.process.repository.PurposeRepository;
import com.gtp.hunter.process.stream.OriginStreamManager;
import com.gtp.hunter.process.stream.ProcessStreamManager;

@RequestScoped
@Path("/process")
public class ProcessRest {

	@Inject
	private ProcessStreamManager	psm;

	@Inject
	private OriginStreamManager		osm;

	@Inject
	private ProcessRepository		pRep;
	@Inject
	private PurposeRepository		purRep;

	@GET
	@Path("/success/{id}")
	public Response success(@PathParam("id") UUID id) {
		return Response.ok(psm.finishProcess(id)).build();
	}

	@GET
	@Path("/failure/{id}")
	public Response failure(@PathParam("id") UUID id) {
		if (!psm.getProcesses().get(id).isComplete()) {
			psm.getProcesses().get(id).setFailure("CANCELADO PELO USUÁRIO");
		}
		return Response.ok(psm.finishProcess(id)).build();
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Process> getAll() {
		return pRep.listAll();
	}

	@GET
	@Path("/allocation")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<UUID, UUID> getAllocation() {
		return psm.getAllocation();
	}

	@GET
	@Path("/start/{process}/{origin}")
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public String startProcess(@PathParam("process") String process, @PathParam("origin") String origin) {
		Process p = pRep.findById(UUID.fromString(process));
		if (p != null) {
			Origin o = osm.getOriginById(UUID.fromString(origin));
			if (o != null) {
				p.setOrigin(o);
				psm.activateProcess(p);
			} else {
				return "ORIGIN NAO ENCONTRADO";
			}
		} else {
			return "PROCESSO NAO ENCONTRADO";
		}
		return "OK";
	}

	@GET
	@Path("/{id}")
	public Process getById(@PathParam("id") String id) {
		Process p = null;
		try {
			p = pRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			p = new Process();
		}
		return p;
	}

	@GET
	@Path("/byBase/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Process getRunningProcess(@PathParam("id") String id) {
		return psm.getProcesses().get(UUID.fromString(id)).getModel();
	}

	@GET
	@Path("/bypurpose/{id}")
	public List<Process> listByPurposeId(@PathParam("id") String id) {
		Purpose p = null;
		try {
			p = purRep.findById(UUID.fromString(id));
			if (p == null)
				return null;
			return pRep.listByPurpose(p);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@POST
	@Path("/")
	public Process save(Process proc) {
		pRep.persist(proc);
		return proc;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		pRep.removeById(UUID.fromString(id));
		return "Ok";
	}

}
