package com.gtp.hunter.process.rest;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.process.model.Feature;
import com.gtp.hunter.process.repository.FeatureRepository;

@RequestScoped
@Path("/feature")
public class FeatureRest {

	@Inject
	private FeatureRepository	fRep;
	
	@Inject
	private Logger				logger;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Feature> getAll() {
		return fRep.listAll();
	}

	@GET
	@Path("/{id}")
	public Feature getById(@PathParam("id") String id) {
		Feature o = null;
		try {
			o = fRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error("Creating new Feature" + e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			o = new Feature();
		}
		return o;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		try {
			fRep.removeById(UUID.fromString(id));
			return "Ok";
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			return e.getLocalizedMessage();
		}
	}

	@POST
	public Feature persist(Feature feat) {
		fRep.persist(feat);
		return feat;
	}

}
