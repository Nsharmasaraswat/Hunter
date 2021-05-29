package com.gtp.hunter.report.repository;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.naming.NamingException;

import com.gtp.hunter.common.manager.ConnectionManager;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class QueryReportRepository extends ConnectionManager {

	private static final SimpleDateFormat	sdfTimestamp	= new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final SimpleDateFormat	sdfDate			= new SimpleDateFormat("dd/MM/yyyy");

	public QueryReportRepository() throws NamingException {
		super();
	}

	@Lock(LockType.READ)
	public JsonArray runQuery(JsonArray columns, JsonArray actions, String sql, Object... parms) {
		return runQuery(DEFAULT_DATASOURCE, columns, actions, sql, parms);
	}

	@Lock(LockType.READ)
	public JsonArray runQuery(String dataSource, JsonArray columns, JsonArray actions, String sql, Object... parms) {
		final JsonArrayBuilder ret = Json.createArrayBuilder();

		try (Connection con = initConnection(dataSource); PreparedStatement st = getStatement(con, sql, parms);) {
			if (st.execute()) {
				ResultSet rs = st.getResultSet();

				while (rs.next()) {
					JsonObjectBuilder returnObject = Json.createObjectBuilder();

					for (JsonValue column : columns) {
						JsonObject obj = Json.createReader(new StringReader(column.toString())).readObject();
						Class<?> type = Class.forName(obj.getString("type"));
						String nullString = obj.getString("null-string");
						String col = obj.getString("field");

						if (type.equals(Integer.class) || type.equals(int.class)) {
							Integer v = rs.getInt(col);

							if (rs.wasNull())
								returnObject.add(col, nullString);
							else
								returnObject.add(col, v);
						} else if (type.equals(Long.class) || type.equals(long.class)) {
							Long v = rs.getLong(col);
							if (rs.wasNull())
								returnObject.add(col, nullString);
							else
								returnObject.add(col, v);
						} else if (type.equals(Date.class)) {
							Date v = rs.getDate(col);
							if (rs.wasNull())
								returnObject.add(col, nullString);
							else
								returnObject.add(col, sdfDate.format(v));
						} else if (type.equals(Timestamp.class)) {
							Date v = rs.getTimestamp(col);
							if (rs.wasNull())
								returnObject.add(col, nullString);
							else
								returnObject.add(col, sdfTimestamp.format(v));
						} else {
							String v = rs.getString(col);
							if (rs.wasNull())
								returnObject.add(col, nullString);
							else
								returnObject.add(col, v);
						} /* else if (type.equals(String.class)) {
							String v = rs.getString(col);
							if (rs.wasNull())
							returnObject.add(col, nullString);
							else
							returnObject.add(col, v);
							}*/
						for (JsonValue actionJ : actions) {
							JsonObject action = Json.createReader(new StringReader(actionJ.toString())).readObject();
							String actionVar = rs.getString(action.getString("field"));
							String act = action.getString("action").replace("${" + action.getString("field") + "}", actionVar);

							returnObject.add("action_" + action.getString("field"), act);
						}
					}
					ret.add(returnObject);
				}
				rs.close();
			}
			st.close();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret.build();
	}
}
