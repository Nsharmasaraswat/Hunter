package com.gtp.hunter.common.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;

@Singleton
@ApplicationScoped
public class ConnectionManager {

	@Inject
	private Logger					logger;

	public static final String		DEFAULT_DATASOURCE	= "hunter2";

	private Map<String, DataSource>	ds					= new ConcurrentHashMap<>(5);

	protected Connection initConnection(String dataSource) {
		try {
			if (!ds.containsKey(dataSource)) {
				initDatasource(dataSource);
			}
			return ds.get(dataSource).getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Connection initConnection() {
		return initConnection(DEFAULT_DATASOURCE);
	}

	protected void closeConnection(Connection con) {
		try {
			if (con != null) con.close();
			con = null;
			logger.trace("Connection closed");
		} catch (SQLException | NullPointerException ex) {
			logger.error(ex.getLocalizedMessage());
			logger.trace(ex.getLocalizedMessage(), ex);
		}
	}

	protected PreparedStatement getStatement(Connection con, String sql, Object... parms) throws Exception {
		PreparedStatement st = con.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);

		int i = 0;
		if (parms != null) {
			for (Object o : parms) {
				if (o == null) {
					st.setNull(++i, Types.VARCHAR);
				} else if (o.getClass().equals(Integer.class)) {
					st.setInt(++i, (Integer) o);
				} else if (o.getClass().equals(Double.class)) {
					st.setDouble(++i, (Double) o);
				} else if (o.getClass().equals(java.sql.Date.class)) {
					st.setDate(++i, (java.sql.Date) o);
				} else if (o.getClass().equals(String.class)) {
					st.setString(++i, (String) o);
				} else if (o.getClass().equals(Long.class)) {
					st.setLong(++i, (Long) o);
				} else if (o.getClass().equals(Timestamp.class)) {
					st.setTimestamp(++i, (Timestamp) o);
				} else if (o.getClass().equals(java.util.Date.class)) {
					st.setTimestamp(++i, new Timestamp(((java.util.Date) o).getTime()));
				} else if (o.getClass().equals(Boolean.class)) {
					st.setBoolean(++i, (Boolean) o);
				} else if (o.getClass().equals(UUID.class)) {
					st.setString(++i, o.toString());
				} else if (o.getClass().equals(String[].class)) {
					String[] ss = (String[]) o;
					for (String s : ss) {
						st.setString(++i, s);
					}
				} else if (o.getClass().equals(Integer[].class)) {
					Integer[] ii = (Integer[]) o;
					for (Integer in : ii) {
						st.setInt(++i, in);
					}
				} else {
					st.close();
					throw new IllegalArgumentException("Argument not implemented yet: " + o.getClass());
				}
			}
		}
		return st;
	}

	private void initDatasource(String name) throws NamingException {
		Context init = new InitialContext();
		Context ctx = (Context) init.lookup("java:");
		DataSource dataSource = (DataSource) ctx.lookup(name);

		ds.put(name, dataSource);
		ctx.close();
	}
}
