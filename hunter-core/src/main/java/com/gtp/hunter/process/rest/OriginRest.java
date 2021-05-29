package com.gtp.hunter.process.rest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gtp.hunter.core.devices.BaseDevice;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.core.repository.DeviceRepository;
import com.gtp.hunter.core.repository.SourceRepository;
import com.gtp.hunter.core.service.SourceService;
import com.gtp.hunter.process.model.Feature;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.repository.FeatureRepository;
import com.gtp.hunter.process.repository.OriginRepository;
import com.gtp.hunter.process.repository.PurposeRepository;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.OriginStreamManager;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.origin.DeviceOrigin;
import com.gtp.hunter.process.wf.origin.PortOrigin;
import com.gtp.hunter.ui.json.ViewFullOrigin;

@RequestScoped
@Path("/origin")
public class OriginRest {

	@Inject
	private OriginStreamManager	osm;

	@Inject
	private OriginRepository	oRep;

	@Inject
	private PurposeRepository	pRep;

	@Inject
	private FeatureRepository	ftRep;

	@Inject
	private DeviceRepository	dRep;

	@Inject
	private SourceRepository	sRep;

	@Inject
	private SourceService		srcSvc;

	@Inject
	private RegisterService		regSvc;

	@Inject
	private Logger				logger;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrigins() {
		return Response.ok(osm.getOrigins()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Origin getById(@PathParam("id") String id) {
		Origin o = null;
		try {
			o = oRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error("Creating new Origin" + e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			o = new Origin();
		}
		return o;
	}

	@GET
	@Path("/bypurpose/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Origin> listByPurposeId(@PathParam("id") String id) {
		Purpose p = null;
		try {
			p = pRep.findById(UUID.fromString(id));
			if (p == null)
				return null;

			return oRep.listByPurpose(p);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Origin> getAll() {
		return oRep.listAll();
	}

	@GET
	@Path("/meta/{meta}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getIdByMeta(@PathParam("meta") String meta) {
		return oRep.findByMetaname(meta).getId().toString();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Origin save(Origin origin) {
		oRep.persist(origin);
		return origin;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		try {
			oRep.removeById(UUID.fromString(id));
			return "Ok";
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			return e.getLocalizedMessage();
		}
	}

	//FIXME: Generalizar para qualquer comando
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{id}/executeon/{feature-meta}")
	public Response executeCommand(JsonObject body, @PathParam("id") String originId, @PathParam("feature-meta") String featureMeta) {
		try {
			Origin o = oRep.findById(UUID.fromString(originId));
			Feature f = ftRep.findByMetaname(o, featureMeta);
			Source s = sRep.findByMetaname(f.getSource());
			Device d = dRep.findByMetaname(s.getId(), f.getDevice());
			BaseDevice dev = srcSvc.getBaseDeviceByUUID(d.getId());
			String command = body.getString("command");

			logger.debug("Trying to invoke " + command + " ON ORIGIN " + o.getMetaname());

			for (Method m : srcSvc.getBaseDeviceByUUID(d.getId()).getClass().getMethods()) {
				if (m.getName().equalsIgnoreCase(command))
					try {
						logger.debug("Invoking " + command + " ON ORIGIN " + o.getMetaname());
						m.invoke(dev);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.ok().build();
	}

	@GET
	@Path("/full/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ViewFullOrigin getFullOrigin(@PathParam("id") String id) {
		BaseOrigin o = osm.getOrigin(UUID.fromString(id));
		ViewFullOrigin v = o instanceof DeviceOrigin ? new ViewFullOrigin((DeviceOrigin) o) : new ViewFullOrigin((PortOrigin) o);

		return v;
	}
}
