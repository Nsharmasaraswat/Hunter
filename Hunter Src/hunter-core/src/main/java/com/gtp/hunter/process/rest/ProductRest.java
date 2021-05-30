package com.gtp.hunter.process.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.service.RegisterService;

@RequestScoped
@Path("/product")
public class ProductRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		Profiler prf = new Profiler("Profiling ProductRest.all", false);

		List<Product> pList = regSvc.getPrdSvc().listAll();
		prf.done("Loaded Products", false, true);
		return Response.ok(pList).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getById(@PathParam("id") String id) {
		return Response.ok(regSvc.getPrdSvc().findById(UUID.fromString(id))).build();
	}

	@GET
	@Path("/bytype/{metanamemodel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getListByType(@PathParam("metanamemodel") String type) {
		return Response.ok(regSvc.getPrdSvc().listByModelMetaname(type)).build();
	}

	@GET
	@Path("/bytypeandsiblings/{metanamemodel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getListByTypeAndSiblings(@PathParam("metanamemodel") String type) {
		return Response.ok(regSvc.getPrdSvc().listByModelMetanameAndSiblings(type)).build();
	}

	@GET
	@Path("/bysku/{sku}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBySKU(@PathParam("sku") String sku) {
		return Response.ok(regSvc.getPrdSvc().findBySKU(sku)).build();
	}

	@GET
	@Path("/quickByDocument/{documentId}")
	public Response quickListByDocument(@PathParam("documentId") String documentId) {
		return Response.ok(regSvc.getPrdSvc().listByDocumentId(UUID.fromString(documentId))).build();
	}

	@GET
	@Path("/metaname/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProductByMetaNameProduct(@PathParam("metaname") String metaName) {
		return Response.ok(regSvc.getPrdSvc().findByMetaname(metaName)).build();
	}

	@GET
	@Path("/fromupdated/{updated}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response listFromUpdated(@PathParam("updated") String updated) {
		try {
			Date upd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).parse(updated);

			return Response.ok(regSvc.getPrdSvc().listFromUpdated(upd)).build();
		} catch (ParseException e) {
			return Response.status(400).build();
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response persist(Product p) {
		regSvc.getPrdSvc().persist(p);
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteById(@PathParam("id") String id) {
		regSvc.getPrdSvc().deleteProductByID(UUID.fromString(id));
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteProductById(@PathParam("id") UUID id) {
		regSvc.getPrdSvc().deleteProductByID(id);
		return Response.ok().build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(Product p) {
		regSvc.getPrdSvc().persist(p);
		for (ProductField psf : p.getFields())
			psf.setProduct(p);
		return Response.ok(p).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProduct(@PathParam("id") UUID id, Product p) {
		if (id == null || p.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!p.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		p.getFields().parallelStream().forEach(pf -> pf.setProduct(p));
		return Response.ok(regSvc.getPrdSvc().persist(p)).build();
	}
}