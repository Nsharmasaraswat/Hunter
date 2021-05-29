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

import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.core.repository.SourceRepository;
import com.gtp.hunter.core.service.SourceService;

@RequestScoped
@Path("/source")
public class SourceRest {

	@Inject
	private Logger				logger;

	@Inject
	private SourceRepository	sRep;

	@Inject
	private SourceService		sSvc;

	@GET
	@Path("/all")
	public List<Source> getAll() {
		List<Source> all = sRep.listAll();

		all.forEach(s -> sSvc.fillToken(s));
		return all;
	}

	@GET
	@Path("/{id}")
	public Source getById(@PathParam("id") String id) {
		Source s = null;
		try {
			s = sRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error("Creating new Source", e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			s = new Source();
		}

		return s;
	}

	@POST
	@Path("/")
	public Source save(Source source) {
		sRep.persist(source);
		return source;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		try {
			sRep.removeById(UUID.fromString(id));
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
		return "Ok";
	}
}
