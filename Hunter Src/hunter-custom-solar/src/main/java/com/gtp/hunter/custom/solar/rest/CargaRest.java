package com.gtp.hunter.custom.solar.rest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.util.RestUtil;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.AGLCustomer;
import com.gtp.hunter.process.jsonstubs.AGLProdModel;
import com.gtp.hunter.process.jsonstubs.AGLTruck;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Person;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.Thing;

@RequestScoped
@Path("/carga")
public class CargaRest {

	@Inject
	private Logger					logger;

	@Inject
	private IntegrationService		iSvc;

	private final SimpleDateFormat	sdf	= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	@GET
	@PermitAll
	@Path("/sendproductmodel")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendProductModel() {
		List<ProductModel> lst = iSvc.getRegSvc().getPmSvc().listAll();
		RestUtil rst = new RestUtil(ConfigUtil.get("hunter-custom-solar", "WMS-Base", "http://10.62.132.46:8080/wms/servlet"));
		String method = "com.wms.comunicador.aproduct_model";

		for (ProductModel pm : lst) {
			pm.setName(stripChars(pm.getName()));
			AGLProdModel apm = iSvc.getRegSvc().getAglSvc().convertProdModelToAGL(pm);
			String json = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(apm);
			IntegrationReturn r = rst.sendSync(json, method, "POST", null);

			if (!r.isResult() && r.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) {
				apm.setUpdated_at(sdf.format(Calendar.getInstance().getTime()));
				rst.sendSync(json, method, "PUT", apm.getId().toString());
			}
		}
		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/senddocument/{httpmethod}/{docid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendDocument(@PathParam("httpmethod") String httpMethod, @PathParam("docid") UUID docId) {
		Document d = iSvc.getRegSvc().getDcSvc().findById(docId);

		iSvc.getRegSvc().getAglSvc().sendDocToWMS(d, httpMethod);
		return Response.ok("OK").build();
	}

	private String stripChars(String name) {
		return name.toUpperCase().replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U").replace("Ä", "A").replace("Ë", "E").replace("Ï", "I").replace("Ö", "O").replace("Ü", "U").replace("Â", "A").replace("Ê", "E").replace("Î", "I").replace("Ô", "O").replace("Û", "U").replace("Ã", "A").replace("Õ", "O").replace("Ç", "C").replace("\\", "_").replace("-", "_");
	}

	@GET
	@PermitAll
	@Path("/sendproduct")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendProduct() {
		List<Product> lst = iSvc.getRegSvc().getPrdSvc().listAll();

		for (Product pm : lst) {
			String oldName = pm.getName();

			pm.setName(stripChars(oldName));
			try {
				IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendProductToWMS(pm, "POST").get();
				if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) iSvc.getRegSvc().getAglSvc().sendProductToWMS(pm, "PUT");
			} catch (InterruptedException e) {
				logger.error(e.getLocalizedMessage(), e);
			} catch (ExecutionException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
			pm.setName(oldName);
		}
		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendproduct/{productid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendProduct(@PathParam("productid") UUID productId) {
		Product p = iSvc.getRegSvc().getPrdSvc().findById(productId);
		String oldName = p.getName();

		p.setName(stripChars(oldName));
		try {
			IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendProductToWMS(p, "POST").get();
			if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) iSvc.getRegSvc().getAglSvc().sendProductToWMS(p, "PUT");
		} catch (InterruptedException e) {
			logger.error(e.getLocalizedMessage(), e);
		} catch (ExecutionException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		p.setName(oldName);
		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendaddressfromlocation/{location_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendAllFromLocation(@PathParam("location_id") UUID location_id) {
		List<Address> lst = iSvc.getRegSvc().getAddSvc().listOrphanByLocation(location_id);

		lst.sort((Address p1, Address p2) -> {
			return p1.getUpdatedAt().compareTo(p2.getUpdatedAt());
		});
		for (Address a : lst) {
			try {
				IntegrationReturn r = iSvc.getRegSvc().getAglSvc().sendAddressToWMS(a, "POST").get();

				if (!r.isResult() && r.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) {
					a.setUpdatedAt(Calendar.getInstance().getTime());
					iSvc.getRegSvc().getAglSvc().sendAddressToWMS(a, "PUT").get();
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendaddress/{addressid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendAddress(@PathParam("addressid") UUID addressid) {
		Address a = iSvc.getRegSvc().getAddSvc().findById(addressid);

		try {
			IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendAddressToWMS(a, "POST").get();
			if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) {
				a.setUpdatedAt(Calendar.getInstance().getTime());
				a.getSiblings().forEach(as -> as.setUpdatedAt(Calendar.getInstance().getTime()));
				iSvc.getRegSvc().getAglSvc().sendAddressToWMS(a, "PUT");
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getLocalizedMessage(), e);
		}

		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendtruck")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendTruck() {
		List<Product> listProd = iSvc.getRegSvc().getPrdSvc().listByModelMetaname("TRUCK");
		String method = "com.wms.comunicador.atruck";

		for (Product p : listProd) {
			p.setName(stripChars(p.getName()));
			List<Thing> lst = iSvc.getRegSvc().getThSvc().listByProduct(p);
			RestUtil rst = new RestUtil(ConfigUtil.get("hunter-custom-solar", "WMS-Base", "http://10.62.132.46:8080/wms/servlet"));

			for (Thing t : lst) {
				if (t.getName() != null) t.setName(stripChars(t.getName()));
				AGLTruck truck = iSvc.getRegSvc().getAglSvc().convertThingToAGLTruck(t);
				String json = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(truck);
				IntegrationReturn r = rst.sendSync(json, method, "POST", null);

				if (!r.isResult() && r.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) {
					truck.setUpdated_at(sdf.format(Calendar.getInstance().getTime()));
					rst.sendSync(json, method, "PUT", truck.getId().toString());
				}
			}
		}
		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendcustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendCustomer() {
		List<Person> listPers = iSvc.getRegSvc().getPsSvc().listByModelMetaname("CUSTOMER");
		String method = "com.wms.comunicador.acustomer";

		for (Person ps : listPers) {
			ps.setName(stripChars(ps.getName()));
			RestUtil rst = new RestUtil(ConfigUtil.get("hunter-custom-solar", "WMS-Base", "http://10.62.132.46:8080/wms/servlet"));
			AGLCustomer customer = iSvc.getRegSvc().getAglSvc().convertPersonToAGLCustomer(ps);
			String json = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(customer);
			IntegrationReturn r = rst.sendSync(json, method, "POST", null);

			if (!r.isResult() && r.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) {
				customer.setUpdated_at(sdf.format(Calendar.getInstance().getTime()));
				rst.sendSync(json, method, "PUT", customer.getId().toString());
			}
		}
		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendcustomer/{personid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendCustomer(@PathParam("personid") UUID personid) {
		Person ps = iSvc.getRegSvc().getPsSvc().findById(personid);

		try {
			IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendCustomerToWMS(ps, "POST").get();
			if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) iSvc.getRegSvc().getAglSvc().sendCustomerToWMS(ps, "PUT");
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getLocalizedMessage(), e);
		}

		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendsupplier")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendSupplier() {
		List<Person> listPers = iSvc.getRegSvc().getPsSvc().listByModelMetaname("CUSTOMER");

		for (Person ps : listPers) {
			try {
				IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendSupplierToWMS(ps, "POST").get();
				if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) {
					ps.setUpdatedAt(Calendar.getInstance().getTime());
					iSvc.getRegSvc().getAglSvc().sendSupplierToWMS(ps, "PUT");
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendcsupplier/{personid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendSupplier(@PathParam("personid") UUID personid) {
		Person ps = iSvc.getRegSvc().getPsSvc().findById(personid);

		try {
			IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendSupplierToWMS(ps, "POST").get();
			if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO.")) {
				ps.setUpdatedAt(Calendar.getInstance().getTime());
				iSvc.getRegSvc().getAglSvc().sendSupplierToWMS(ps, "PUT");
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getLocalizedMessage(), e);
		}

		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendthings/{propmodel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendThing(@PathParam("propmodel") String propModel) {
		List<Thing> listThing = iSvc.getRegSvc().getThSvc().listByModelMeta(propModel);

		for (Thing t : listThing) {
			try {
				IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "POST").get();
				if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE NO CADASTRO.")) {
					t.setUpdatedAt(Calendar.getInstance().getTime());
					iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "PUT");
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendthing/{thingid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendThing(@PathParam("thingid") UUID thingId) {
		Thing t = iSvc.getRegSvc().getThSvc().findById(thingId);

		try {
			IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "POST").get();

			if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE NO CADASTRO.")) {
				t.setUpdatedAt(Calendar.getInstance().getTime());
				iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "PUT");
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getLocalizedMessage(), e);
		}

		return Response.ok("OK").build();
	}

	@GET
	@PermitAll
	@Path("/sendthingbystatus/{status}/{statusto}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendThingByStatus(@PathParam("status") String status, @PathParam("statusto") String statusTo) {
		List<Thing> tList = iSvc.getRegSvc().getThSvc().listByStatus(status).stream()
						.filter(t -> !t.getSiblings().isEmpty())
						.collect(Collectors.toList());

		for (Thing t : tList) {
			try {
				IntegrationReturn ret = iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "POST").get();

				if (!ret.isResult() && ret.getMessage().toUpperCase().contains("JA EXISTE NO CADASTRO.")) {
					t.setUpdatedAt(Calendar.getInstance().getTime());
					ret = iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "PUT").get();
				}
				if (ret.isResult()) {
					t.setStatus(statusTo);
					t.getSiblings().forEach(ts -> ts.setStatus(statusTo));
					iSvc.getRegSvc().getThSvc().persist(t);
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}

		return Response.ok("OK").build();
	}

}
