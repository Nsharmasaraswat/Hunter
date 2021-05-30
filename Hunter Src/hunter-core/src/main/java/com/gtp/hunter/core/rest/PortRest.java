package com.gtp.hunter.core.rest;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.core.repository.DeviceRepository;
import com.gtp.hunter.core.repository.PortRepository;

@RequestScoped
@Path("/port")
public class PortRest {

	@Inject
	private Logger				logger;

	@Inject
	private PortRepository		pRep;

	@Inject
	private DeviceRepository	dRep;

	@GET
	@Path("/all")
	public List<Port> getAll() {
		return pRep.listAll();
	}

	@GET
	@Path("/{id}")
	public Port getById(@PathParam("id") String id) {
		Port p = null;
		try {
			p = pRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error("Creating new Port", e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			p = new Port();
		}
		return p;
	}

	@GET
	@Path("/bydevice/{id}")
	public List<Port> getByDevice(@PathParam("id") String id) {
		Device d = dRep.findById(UUID.fromString(id));
		if (d == null)
			return null;
		return pRep.listByField("device", d);
	}

	@POST
	@Path("/")
	public Port save(Port port) {
		pRep.persist(port);
		return port;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		try {
			pRep.removeById(UUID.fromString(id));
			return "Ok";
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
	}
}
