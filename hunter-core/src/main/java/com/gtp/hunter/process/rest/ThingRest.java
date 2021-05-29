package com.gtp.hunter.process.rest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;

@Transactional
@RequestScoped
@Path("/thing")
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class ThingRest {

	@Inject
	private Logger			logger;

	@Inject
	private RegisterService	regSvc;

	@GET
	@Path("/quickByProduct/{productid}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> quickListByProductId(@PathParam("productid") String productId) {
		return regSvc.getThSvc().quickListByProductId(UUID.fromString(productId));
	}

	@GET
	@Path("/quickByProductIdAndStatus/{productid}/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> quickListByProductId(@PathParam("productid") UUID productId, @PathParam("status") String status) {
		return regSvc.getThSvc().quickListByProductIdAndStatus(productId, status);
	}

	@GET
	@Path("/listByStatus/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByStatus(@PathParam("status") String status, @DefaultValue("0") @QueryParam("start") int start, @DefaultValue("9999999") @QueryParam("end") int end) {
		return regSvc.getThSvc().listByStatusLimit(status, start, end);
	}

	@GET
	@Path("/listByModel/{modelid}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByModel(@PathParam("modelid") UUID modelId) {
		PropertyModel ppm = regSvc.getPrmSvc().findById(modelId);
		List<Thing> tList = regSvc.getThSvc().listByModel(ppm);

		for (Thing t : tList)
			regSvc.getThSvc().fillUnits(t);
		return tList;
	}

	@GET
	@Path("/listByModelAndStatus/{modelid}/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByModelAndStatus(@PathParam("modelid") UUID modelId, @PathParam("status") String status) {
		PropertyModel ppm = regSvc.getPrmSvc().findById(modelId);
		List<Thing> tList = regSvc.getThSvc().listByModelAndStatus(ppm, status);

		for (Thing t : tList)
			regSvc.getThSvc().fillUnits(t);
		return tList;
	}

	@GET
	@Path("/listByModelAndFieldValue/{modelid}/{field}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByModelAndStatus(@PathParam("modelid") UUID modelId, @PathParam("field") String field, @PathParam("value") String value) {
		PropertyModel ppm = regSvc.getPrmSvc().findById(modelId);
		List<Thing> tList = regSvc.getThSvc().listByModelAndPropertyValue(ppm, field, value);

		for (Thing t : tList)
			regSvc.getThSvc().fillUnits(t);
		return tList;
	}

	@GET
	@Path("/listByModelAndStatusAndFieldValue/{modelid}/{status}/{field}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByModelAndStatus(@PathParam("modelid") UUID modelId, @PathParam("status") String status, @PathParam("field") String field, @PathParam("value") String value) {
		PropertyModel ppm = regSvc.getPrmSvc().findById(modelId);
		List<Thing> tList = regSvc.getThSvc().listByModelAndStatusAndPropertyValue(ppm, status, field, value);

		for (Thing t : tList)
			regSvc.getThSvc().fillUnits(t);
		return tList;
	}

	@GET
	@Path("/listByDocument/{documentid}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByDocumentId(@PathParam("documentid") String documentId) {
		Document d = regSvc.getDcSvc().findById(UUID.fromString(documentId));
		List<Thing> tList = regSvc.getThSvc().listByDocument(d);

		for (Thing t : tList)
			regSvc.getThSvc().fillUnits(t);
		return tList;
	}

	@GET
	@Path("/bytagid/{tagid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Thing getByTagId(@PathParam("tagid") String tagId) {
		return regSvc.getThSvc().findByTagId(tagId);
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> getListContainsAllThings() {
		return regSvc.getThSvc().listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Thing getOneThing(@PathParam("id") UUID id) {
		return regSvc.getThSvc().fillUnits(regSvc.getThSvc().findById(id));
	}

	@GET
	@Path("/parent/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Thing findParent(@PathParam("id") UUID id) {
		Thing t = regSvc.getThSvc().findById(id);

		if (t.getParent() != null)
			return regSvc.getThSvc().findById(t.getParent().getId());
		return null;
	}

	@GET
	@Path("/bytype/{metanamemodel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Thing getThingByMetaName(@PathParam("metaname") String metaName) {
		return regSvc.getThSvc().findByMetaName(metaName);
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteThingById(@PathParam("id") UUID id) {
		try {
			regSvc.getThSvc().removeById(id);
			return Response.ok().build();
		} catch (Exception cve) {
			return Response.status(Status.CONFLICT).entity(cve.getLocalizedMessage()).build();
		}
	}

	@DELETE
	@Path("/remove/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn removeThingById(@PathParam("id") UUID id) {
		Thing toDel = new Thing();

		toDel.setId(id);
		regSvc.getThSvc().remove(toDel);
		return IntegrationReturn.OK;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addThing(Thing thing) {
		return Response.ok(regSvc.getThSvc().persist(thing)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateThing(@PathParam("id") UUID id, Thing thing) {
		if (id == null || thing.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!thing.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		thing.getProperties().parallelStream().forEach(pr -> pr.setThing(thing));
		return Response.ok(regSvc.getThSvc().persist(thing)).build();
	}

	@GET
	@Path("/bylocation/{locid}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByLocation(@PathParam("locid") UUID locId) {
		logger.info("ListThingByLocation: " + locId);
		return regSvc.getThSvc().listByLocationId(locId);
	}

	@GET
	@Path("/byaddress/{addrid}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByAddress(@PathParam("addrid") UUID addrId) {
		logger.info("ListThingByAddress: " + addrId);
		return regSvc.getThSvc().listByAddressId(addrId);
	}

	@GET
	@Path("/byaddresschildren/{addrid}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByAddressChildren(@PathParam("addrid") UUID addrId) {
		return regSvc.getThSvc().listByChildAddressId(addrId);
	}

	@GET
	@Path("/byproductidandstatus/{prdid}/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByProductIdAndStatus(@PathParam("prdid") UUID prdId, @PathParam("status") String status) {
		return regSvc.getThSvc().listByProductIdAndStatus(prdId, status);
	}

	@GET
	@Path("/byproductid/{prdid}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByProductId(@PathParam("prdid") UUID prdId) {
		return regSvc.getThSvc().listByProductId(prdId);
	}

	@GET
	@Path("/byproductidandnotstatus/{prdid}/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Thing> listByProductIdAndNotStatus(@PathParam("prdid") UUID prdId, @PathParam("status") String status) {
		return regSvc.getThSvc().listByProductIdAndNotStatus(prdId, status);
	}

	@GET
	@Path("/fillunits/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Thing fillUnits(@PathParam("id") UUID id) {
		Thing th = regSvc.getThSvc().findById(id);

		regSvc.getThSvc().fillUnits(th);
		return th;
	}
}
