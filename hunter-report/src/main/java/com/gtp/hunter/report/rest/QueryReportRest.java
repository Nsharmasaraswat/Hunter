package com.gtp.hunter.report.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import com.gtp.hunter.common.manager.ConnectionManager;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.report.service.QueryReportService;

@RequestScoped
@Path("/query")
public class QueryReportRest {

	@Inject
	QueryReportService				trSvc;

	@Inject
	private transient Logger		logger;
	private final JsonObjectBuilder	ret		= Json.createObjectBuilder();
	private final JsonObjectBuilder	status	= Json.createObjectBuilder();

	@GET
	@Lock(LockType.READ)
	@Path("/byFile/{fileMeta}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getQueryResult(@PathParam(value = "fileMeta") String fileMeta, @Context UriInfo uriInfo) {
		if (!fileMeta.endsWith(".json")) fileMeta += ".json";
		try {
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			JsonObject columnsQuery = readQueryFile(fileMeta);
			JsonArray columns = columnsQuery.getJsonArray("columns");
			JsonArray actions = columnsQuery.getJsonArray("actions");
			String query = columnsQuery.getString("query");
			String name = columnsQuery.getString("name");
			String datasource = columnsQuery.containsKey("datasource") ? columnsQuery.getString("datasource") : ConnectionManager.DEFAULT_DATASOURCE;

			ret.add("file", fileMeta);
			ret.add("name", name);
			ret.add("query", query);
			ret.add("columns", columns);
			ret.add("datasource", datasource);
			ret.add("actions", actions);
			for (String k : queryParams.keySet()) {
				query = query.replace("${" + k + "}", "'" + queryParams.get(k).get(0) + "'");
			}
			logger.debug(query);
			ret.add("data", trSvc.getReport(datasource, columns, query, actions));
			status.add("result", true);
			status.add("message", "");
		} catch (IOException ioe) {
			status.add("result", false);
			status.add("message", "Error accessing file: " + fileMeta + ":" + ioe.getLocalizedMessage());
		} catch (RuntimeException re) {
			status.add("result", false);
			status.add("message", re.getLocalizedMessage() == null ? "NullPointerException" : re.getLocalizedMessage());
		}
		ret.add("status", status);
		return ret.build();
	}

	@GET
	@Lock(LockType.READ)
	@Path("/listVariables/{filemeta}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getQueryVariables(@PathParam(value = "filemeta") String fileMeta) {
		if (!fileMeta.endsWith(".json")) fileMeta += ".json";
		try {
			JsonObject obj = readQueryFile(fileMeta);

			ret.add("data", obj.getJsonArray("variables"));
			status.add("result", true);
			status.add("message", "");
		} catch (IOException ioe) {
			status.add("result", false);
			status.add("message", "Error accessing file: " + fileMeta + ":" + ioe.getLocalizedMessage());
		}
		ret.add("status", status);
		return ret.build();
	}

	@GET
	@Lock(LockType.READ)
	@Path("/listFiles")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getQueryFiles() {
		String json_dir = ConfigUtil.get("hunter-report", "query-path", System.getProperty("jboss.server.config.dir") + "/hunter-report/query/");

		try {
			JsonArrayBuilder arr = Json.createArrayBuilder();
			List<java.nio.file.Path> paths = Files.list(Paths.get(json_dir)).filter(s -> s.toString().endsWith(".json")).collect(Collectors.toList());

			for (java.nio.file.Path f : paths) {
				JsonObjectBuilder obj = Json.createObjectBuilder();
				String fileName = f.getFileName().toString();
				JsonObject reportFile = readQueryFile(fileName);

				obj.add("name", reportFile.getString("name"));
				obj.add("query", reportFile.getString("query"));
				obj.add("columns", reportFile.getJsonArray("columns"));
				obj.add("variables", reportFile.getJsonArray("variables"));
				obj.add("datasource", reportFile.getString("datasource"));
				obj.add("actions", reportFile.getJsonArray("actions"));
				obj.add("file", fileName);
				arr.add(obj);
			}
			ret.add("data", arr.build());
			status.add("result", true);
			status.add("message", "");
		} catch (IOException ioe) {
			status.add("result", false);
			status.add("message", "Error accessing dir: " + json_dir + ":" + ioe.getLocalizedMessage());
		} catch (NullPointerException npe) {
			status.add("result", false);
			status.add("message", "Error reading report name from file");
		}
		ret.add("status", status);
		return ret.build();
	}

	@GET
	@Lock(LockType.READ)
	@Path("/loadFile/{fileName}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject loadFile(@PathParam("fileName") String filename) {
		try {
			ret.add("data", readQueryFile(filename));
			status.add("result", true);
		} catch (IOException ioe) {
			logger.error(ioe.getLocalizedMessage());
			status.add("result", false);
			status.add("message", ioe.getLocalizedMessage());
		}
		ret.add("status", status);
		return ret.build();
	}

	@Lock(LockType.READ)
	private JsonObject readQueryFile(String filename) throws IOException {
		String jsonDir = ConfigUtil.get("hunter-report", "query-path", System.getProperty("jboss.server.config.dir") + "/hunter-report/query/");
		InputStream fis = new FileInputStream(jsonDir + "/" + filename);
		JsonReader reader = Json.createReader(fis);
		JsonObject ret = reader.readObject();

		fis.close();
		reader.close();
		return ret;
	}
}
