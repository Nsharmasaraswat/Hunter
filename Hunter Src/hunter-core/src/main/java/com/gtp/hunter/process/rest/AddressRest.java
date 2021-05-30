package com.gtp.hunter.process.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.model.AddressModel;
import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.service.RegisterService;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.AffineTransformation;

@RequestScoped
@Path("/address")
//@Transactional//TOFIX: CAUSING LAZY EXCEPTION
public class AddressRest {

	@Inject
	private Logger			logger;

	@Inject
	private RegisterService	regSvc;

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response persist(Address addr) {
		if (addr.getLocation_id() == null) return Response.status(Status.BAD_REQUEST).entity("location_id CANNOT BE NULL").build();
		Location loc = regSvc.getLocSvc().findById(UUID.fromString(addr.getLocation_id()));
		if (loc == null) return Response.status(Status.BAD_REQUEST).entity("Invalid location: " + addr.getLocation_id()).build();
		if (addr.getParent_id() != null) {
			Address parent = regSvc.getAddSvc().findById(UUID.fromString(addr.getParent_id()));
			if (parent == null) return Response.status(Status.BAD_REQUEST).entity("Invalid parent: " + addr.getParent_id()).build();
			addr.setParent(parent);
		}
		if (addr.getModel() == null || addr.getModel().getId() == null) return Response.status(Status.BAD_REQUEST).entity("model.id CANNOT BE NULL").build();
		AddressModel am = regSvc.getAdmSvc().findById(addr.getModel().getId());
		if (am == null) return Response.status(Status.BAD_REQUEST).entity("model " + addr.getModel().getId() + " is invalid").build();

		addr.setLocation(loc);
		addr.setModel(am);
		try {
			return Response.ok(regSvc.getAddSvc().persist(addr)).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
		}
	}

	@GET
	@Path("/bylocation/{location-id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAddressByLocation(@PathParam("location-id") String locId) {
		logger.debug("Loading addresses from " + locId);
		return Response.ok(regSvc.getAddSvc().listByLocation(UUID.fromString(locId))).build();
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getListContainsAllAddress() {
		return Response.ok(regSvc.getAddSvc().listAll()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOneAddress(@PathParam("id") UUID id) {
		return Response.ok(regSvc.getAddSvc().findById(id)).build();
	}

	@GET
	@Path("/metaname/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAddressByMetaName(@PathParam("metaname") String metaName) {
		return Response.ok(regSvc.getAddSvc().findByMetaname(metaName)).build();
	}

	@GET
	@Path("/bytype/{metanamemodel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAddressByMetaNameModel(@PathParam("metanamemodel") String metaName) {
		AddressModel am = regSvc.getAdmSvc().findByMetaname(metaName);
		if (am == null) return Response.status(Status.BAD_REQUEST).entity("model " + metaName + " is invalid").build();

		return Response.ok(regSvc.getAddSvc().listByModelMetaname(metaName)).build();
	}

	@GET
	@Path("/quickbytype/{metanamemodel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQuickAddressByMetaNameModel(@PathParam("metanamemodel") String metaName) {
		List<Address> addList = regSvc.getAddSvc().quickListByModelMetaname(metaName);

		addList.forEach(a -> a.setFields(new HashSet<AddressField>(regSvc.getAdfSvc().listByAddressId(a.getId()))));
		return Response.ok(addList).build();
	}

	@GET
	@Path("/quickbytypeandlocation/{metanamemodel}/{locId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQuickAddressByMetaNameModel(@PathParam("metanamemodel") String metaName, @PathParam("locId") UUID locId) {
		List<Address> addList = regSvc.getAddSvc().quickListByModelMetanameAndLocationId(metaName, locId);

		addList.forEach(a -> a.setFields(new HashSet<AddressField>(regSvc.getAdfSvc().listByAddressId(a.getId()))));
		return Response.ok(addList).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteAddressById(@PathParam("id") UUID id) {
		regSvc.getAddSvc().removeById(id);
		return Response.ok().build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAddress(@PathParam("id") UUID id, Address addr) {
		if (id == null || addr.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!addr.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		Address a = regSvc.getAddSvc().findById(addr.getId());
		if (a == null) return Response.status(Status.BAD_REQUEST).entity("address " + addr.getId() + " is invalid").build();
		if (addr.getLocation_id() == null) return Response.status(Status.BAD_REQUEST).entity("location_id CANNOT BE NUL").build();
		Location loc = regSvc.getLocSvc().findById(UUID.fromString(addr.getLocation_id()));
		if (loc == null) return Response.status(Status.BAD_REQUEST).entity("Invalid location: " + addr.getLocation_id()).build();
		if (addr.getParent_id() != null) {
			Address parent = regSvc.getAddSvc().findById(UUID.fromString(addr.getParent_id()));
			if (parent == null) return Response.status(Status.BAD_REQUEST).entity("Invalid parent: " + addr.getParent_id()).build();
			addr.setParent(parent);
		}
		if (addr.getModel() == null || addr.getModel().getId() == null) return Response.status(Status.BAD_REQUEST).entity("model.id CANNOT BE NUL").build();
		AddressModel am = regSvc.getAdmSvc().findById(addr.getModel().getId());
		if (am == null) return Response.status(Status.BAD_REQUEST).entity("model " + addr.getModel().getId() + " is invalid").build();

		addr.getFields().parallelStream().forEach(af -> af.setAddress(addr));
		addr.setLocation(loc);
		addr.setModel(am);
		return Response.ok(regSvc.getAddSvc().persist(addr)).build();
	}

	@GET
	@Path("/fromupdated/{updated}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response listFromUpdated(@PathParam("updated") String updated) {
		try {
			Date upd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).parse(updated);

			return Response.ok(regSvc.getAddSvc().listFromUpdated(upd)).build();
		} catch (ParseException e) {
			return Response.status(400).build();
		}
	}

	@GET
	@Path("/bylocation/fromupdated/{locationid}/{updated}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response listByLocationFromUpdated(@PathParam("locationid") UUID locId, @PathParam("updated") String updated) {
		try {
			Date upd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).parse(updated);
			List<Address> aList = regSvc.getAddSvc().listByLocationIdFromUpdated(locId, upd);
			//TOFIX: Desespero Solar!
			aList.addAll(regSvc.getAddSvc().listByLocationIdFromUpdated(UUID.fromString("3d69771f-4c59-11e9-a948-0266c0e70a8c"), upd));
			//			aList.forEach(a -> {
			//				initLoc(a);
			//				a.getSiblings().forEach(as -> initLoc(as));
			//			});
			return Response.ok(aList).build();
		} catch (ParseException e) {
			return Response.status(400).build();
		}
	}

	//	private void initLoc(Address a) {
	//		Hibernate.initialize(a.getLocation());
	//		a.setLocation_id(a.getLocation().getId().toString());
	//		a.setWkt(a.getRegion().toString());
	//	}

	@GET
	@Path("/bytypeandlocation/{type}/{locationid}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response listByTypeAndLocation(@PathParam("type") String modelMeta, @PathParam("locationid") UUID locId) {
		try {
			AddressModel type = regSvc.getAdmSvc().findByMetaname(modelMeta);
			Location loc = regSvc.getLocSvc().findById(locId);

			return Response.ok(regSvc.getAddSvc().listByModelAndLocation(type, loc)).build();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return Response.status(500).build();
		}
	}

	@GET
	@Path("/bymodelandlocation/{model}/{locationid}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response listByModelAndLocation(@PathParam("model") UUID modelId, @PathParam("locationid") UUID locId) {
		try {
			AddressModel type = regSvc.getAdmSvc().findById(modelId);
			Location loc = regSvc.getLocSvc().findById(locId);

			return Response.ok(regSvc.getAddSvc().listByModelAndLocation(type, loc)).build();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return Response.status(500).build();
		}
	}

	@GET
	@Path("/stripFields")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stripFields() {
		Profiler prf = new Profiler("Profiling ProductRest.stripFields", false);
		List<Address> pList = regSvc.getAddSvc().listAll();
		prf.step("Loaded List", false);
		//Json too big with fields
		pList.forEach(p -> p.setFields(new HashSet<AddressField>()));
		prf.done("Striped Fields", false, true);
		return Response.ok(pList).build();
	}

	@PUT
	@Path("/wkt/{addId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response saveWkt(@PathParam("addId") UUID addressId, String wkt) {
		regSvc.getAddSvc().quickUpdateWKT(addressId, wkt);
		return Response.ok().build();
	}

	@PUT
	@Path("/move/{addid}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Address moveRegion(@PathParam("addid") UUID addrId, JsonObject displacement) {
		Address a = regSvc.getAddSvc().findById(addrId);

		if (a != null) {
			double dispX = displacement.getJsonNumber("x").doubleValue();
			double dispY = displacement.getJsonNumber("y").doubleValue();
			AffineTransformation at = new AffineTransformation().translate(dispX, dispY);
			Geometry translatedGeom = at.transform(a.getRegion());

			a.setRegion(translatedGeom);
			regSvc.getAddSvc().persist(a);
		}
		return a;
	}

	@PUT
	@Path("/moveall/{locid}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response moveLocationRegions(@PathParam("locid") UUID locationId, JsonObject displacement) {
		double dispX = displacement.getJsonNumber("x").doubleValue();
		double dispY = displacement.getJsonNumber("y").doubleValue();
		List<Address> addrList = regSvc.getAddSvc().listByLocation(locationId);

		for (Address a : addrList) {
			AffineTransformation at = new AffineTransformation().translate(dispX, dispY);
			Geometry translatedGeom = at.transform(a.getRegion());

			a.setRegion(translatedGeom);
			regSvc.getAddSvc().persist(a);
		}
		return Response.ok().build();
	}

	@GET
	@Path("nearestFromByTypeWithValue/{type}/{originId}/{field}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response nearestFromByModel(@PathParam("type") String type, @PathParam("originId") UUID originId, @PathParam("field") String field, @PathParam("value") String value) {
		return Response.ok(regSvc.getAddSvc().findNearestByModelAndOriginWithFieldValue(type, originId, field, value)).build();
	}
}
