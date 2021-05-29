package com.gtp.hunter.core.repository;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.google.common.base.Enums;
import com.gtp.hunter.common.util.DBUtil;
import com.gtp.hunter.core.model.BaseModel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;

public abstract class NoJPABaseRepository<T extends BaseModel<I>, I> extends BaseRepository<T, I> {
	private DataSource			ds;
	private Class<T>			persistentClass;
	private Class<I>			keyClass;
	private Map<String, Method>	setters	= new HashMap<String, Method>();

	public NoJPABaseRepository(Class<T> cls, Class<I> id) {
		this.persistentClass = cls;
		this.keyClass = id;
		try {
			Arrays.asList(Introspector.getBeanInfo(this.persistentClass, Object.class).getPropertyDescriptors()).stream().filter(pd -> Objects.nonNull(pd.getWriteMethod())).forEach(pd -> {
				try {
					//logger.info("EXISTE WRITEMETHOD PARA " + pd.getName() + "? " + Boolean.toString(pd.getWriteMethod() != null));
					setters.put(pd.getName(), pd.getWriteMethod());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
	}

	public T persist(T t) {
		if (t.getId() == null) {
			t = insert(t);
		} else {
			t = update(t);
		}
		return t;
	}

	protected List<T> resultSetToList(ResultSet rs) {
		ArrayList<T> ret = new ArrayList<T>();

		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int cntColumns = rsmd.getColumnCount();

			while (rs.next()) {
				T t = (T) Class.forName(persistentClass.getName()).newInstance();
				for (int cnt = 1; cnt < cntColumns; cnt++) {
					int columnType = rsmd.getColumnType(cnt);
					switch (columnType) {
						case Types.BIT:
							setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getInt(cnt));
							break;
						case Types.BLOB:
							setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getObject(cnt));
							break;
						case Types.DATE:
						case Types.TIMESTAMP:
							setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getDate(cnt));
							break;
						case Types.INTEGER:
							setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getInt(cnt));
							break;
						case Types.CHAR:
							setters.get(rsmd.getColumnName(cnt)).invoke(t, UUID.fromString(rs.getString(cnt)));
							break;
						case Types.VARCHAR:
							if (setters.get(rsmd.getColumnName(cnt)).getParameterTypes()[0].isEnum()) {
								Class c = setters.get(rsmd.getColumnName(cnt)).getParameterTypes()[0];
								setters.get(rsmd.getColumnName(cnt)).invoke(t, Enum.valueOf(c, rs.getString(cnt)));
							} else {
								setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getString(cnt));
							}
							break;
						default:
							LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()).warn("Tipo desconhecido em " + t.getClass().getSimpleName() + ": " + rsmd.getColumnName(cnt) + " - " + rsmd.getColumnTypeName(cnt));
					}
				}
				ret.add(t);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	protected Connection getConnection() {
		Connection conn = null;

		try {
			conn = initConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;
	}

	protected List<T> getQuery(String qry, Object... objects) {
		List<T> ret = null;

		try (Connection con = getConnection(); PreparedStatement ps = (objects != null ? getStatement(con.prepareStatement(qry), objects) : con.prepareStatement(qry));) {
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<T>) DBUtil.resultSetToList(rs, this.persistentClass);
				rs.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	protected PreparedStatement getStatement(PreparedStatement st, Object... parms) throws Exception {
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
				} else if (o.getClass().equals(Geometry.class)) {
					st.setBytes(++i, new WKBWriter().write((Geometry) o));
				} else if (o.getClass().isEnum()) {
					Class c = o.getClass();
					st.setString(++i, Enums.stringConverter(c).convert(o).toString());
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

	protected Connection initConnection() {
		Connection con = null;

		try {
			if (ds == null) {
				Context init = new InitialContext();
				Context ctx = (Context) init.lookup("java:");

				ds = (DataSource) ctx.lookup("hunter2");
				ctx.close();
			}
			con = ds.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}

	// protected abstract javax.sql.DataSource getDataSource();

	protected abstract T insert(T t);

	protected abstract T update(T t);

}
