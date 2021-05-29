package com.gtp.hunter.report.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonArray;

import com.gtp.hunter.report.repository.QueryReportRepository;

@Stateless
public class QueryReportService {

	@Inject
	QueryReportRepository rep;

	public JsonArray getReport(String datasource, JsonArray columns, String query, JsonArray actions) {
		if (datasource == null)
			return rep.runQuery(columns, actions, query, new Object[] {});
		else
			return rep.runQuery(datasource, columns, actions, query, new Object[] {});
	}

}
