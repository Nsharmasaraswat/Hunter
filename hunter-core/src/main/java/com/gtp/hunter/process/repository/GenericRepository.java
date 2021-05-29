package com.gtp.hunter.process.repository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.persistence.Id;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.manager.ConnectionManager;
import com.gtp.hunter.core.model.BaseModel;

public abstract class GenericRepository<T extends BaseModel<ID>, ID extends Serializable> extends ConnectionManager {

	private final Logger	logger	= LoggerFactory.getLogger(GenericRepository.class);
	protected StringBuilder	SELECT_HEADER;
	protected StringBuilder	SELECT_TABLE;
	protected StringBuilder	SELECT_JOINS;
	private Class<T>		persistentClass;

	@SuppressWarnings("unchecked")
	public GenericRepository(Class<? extends GenericRepository<T, ID>> c) throws NamingException {
		super();
		this.persistentClass = (Class<T>) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0];
		this.SELECT_HEADER = new StringBuilder();
		this.SELECT_TABLE = new StringBuilder();
		this.SELECT_JOINS = new StringBuilder();
	}

	protected Class<T> getPersistentClass() {
		return persistentClass;
	}

	protected T buildObject(ResultSet rs) throws SQLException {
		List<T> ret = buildObjectList(rs);

		return ret.isEmpty() ? null : ret.get(0);
	}

	protected abstract List<T> buildObjectList(ResultSet rs) throws SQLException;

	public T getById(ID id) {
		// T o;
		// try {
		// o = getPersistentClass().getConstructor().newInstance();
		// StringBuilder sql = new StringBuilder();
		//
		// sql.append("SELECT");
		// for (Field f : o.getClass().getDeclaredFields()) {
		// if (f.isAnnotationPresent(Id.class)) {
		// String setterName = "set" + StringUtils.capitalize(f.getName());
		//
		// for (Method m : this.getClass().getDeclaredMethods()) {
		// if (m.getName().equals(setterName)) {
		// // m.invoke(o, f.getType().cast(value));
		// break;
		// }
		// }
		// break;
		// } else if (f.isAnnotationPresent(Column.class)) {
		//
		// } else if (f.isAnnotationPresent(JoinColumn.class)) {
		//
		// }
		// }
		// } catch (InstantiationException | IllegalAccessException |
		// IllegalArgumentException | InvocationTargetException | NoSuchMethodException
		// | SecurityException e) {
		// RuntimeException re = new RuntimeException(e.getLocalizedMessage());
		//
		// re.initCause(e.getCause());
		// re.setStackTrace(e.getStackTrace());
		// throw re;
		// }
		// return o;
		throw new NotImplementedException("Not Implemented YET");
	}

	protected T getByStatement(String sql, Object... parms) {
		List<T> list = listByStatement(sql, parms);
		return list.size() > 0 ? list.get(0) : null;
	}

	//	protected T getByStatement(String dataSource, String sql, Object... parms) {
	//		T ret = null;
	//
	//		try (Connection con = initConnection(dataSource); PreparedStatement st = getStatement(con, sql, parms);) {
	//
	//			if (st.execute()) {
	//				try (ResultSet rs = st.getResultSet();) {
	//					ret = buildObject(rs);
	//					rs.close();
	//				}
	//			}
	//			st.close();
	//		} catch (Exception e) {
	//			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
	//			re.initCause(e.getCause());
	//			re.setStackTrace(e.getStackTrace());
	//			logger.trace(e.getLocalizedMessage(), e);
	//			throw re;
	//		}
	//		return ret;
	//	}

	protected List<T> listByStatement(String sql, Object... parms) {
		return listByStatementWithDatasource(DEFAULT_DATASOURCE, sql, parms);
	}

	protected List<T> listByStatementWithDatasource(String dataSource, String sql, Object... parms) {
		List<T> ret = new ArrayList<T>();

		try (Connection con = initConnection(dataSource); PreparedStatement st = getStatement(con, sql, parms);) {
			if (st.execute()) {
				ResultSet rs = st.getResultSet();

				ret = buildObjectList(rs);
				rs.close();
			}
			st.close();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			logger.trace(e.getLocalizedMessage(), e);
			throw re;
		}
		//		} finally {
		//			recycleResource(con);
		//		}
		return ret;
	};

	protected T saveByStatement(String sql, T entity, Object... parms) {
		return saveByStatement(DEFAULT_DATASOURCE, sql, entity, parms);
	}

	protected T saveByStatement(String dataSource, String sql, T entity, Object... parms) {
		try (Connection con = initConnection(dataSource); PreparedStatement st = getStatement(con, sql, parms);) {

			if (st.executeUpdate() > 0) {
				try (ResultSet rs = st.getGeneratedKeys();) {
					if (rs.next())
						entity = setID(entity, rs.getObject(1));
					rs.close();
				}
			}
			st.close();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			logger.trace(e.getLocalizedMessage(), e);
			throw re;
		}
		return entity;
	}

	protected T deleteByStatement(String sql, T entity, Object... parms) {
		return deleteByStatement(DEFAULT_DATASOURCE, sql, entity, parms);
	}

	protected T deleteByStatement(String dataSource, String sql, T entity, Object... parms) {
		try (Connection con = initConnection(dataSource); PreparedStatement st = getStatement(con, sql, parms);) {
			if (st.executeUpdate() > 0) {
				entity = setID(entity, 0L);
			}
			st.close();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			logger.trace(e.getLocalizedMessage(), e);
			throw re;
		}
		return entity;
	}

	private T setID(T o, Object value) {
		for (Field f : o.getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(Id.class)) {
				String setterName = "set" + StringUtils.capitalize(f.getName());

				for (Method m : o.getClass().getDeclaredMethods()) {
					if (m.getName().equals(setterName)) {
						try {
							if (f.getType() == value.getClass())
								m.invoke(o, value);
							else if (f.getType() == Integer.class && value instanceof Long)
								m.invoke(o, Integer.parseInt(String.valueOf(value)));
							else if (f.getType() == Integer.class && value instanceof BigInteger)
								m.invoke(o, Long.parseLong(String.valueOf(value)));
							else if (f.getType() == Long.class && value instanceof BigInteger)
								m.invoke(o, Long.parseLong(String.valueOf(value)));
						} catch (IllegalAccessException e) {
							logger.trace(e.getLocalizedMessage(), e);
							throw new RuntimeException("Cannot find appropriate accessor for @Id field ");
						} catch (InvocationTargetException e) {
							logger.trace(e.getLocalizedMessage(), e);
							throw new RuntimeException("Exception thrown w/in accessor");
						}
						break;
					}
				}
				break;
			}
		}
		return o;
	}

	public List<T> listAll() {
		StringBuilder sql = new StringBuilder(SELECT_HEADER);

		sql.append(SELECT_TABLE);
		sql.append(SELECT_JOINS);
		return listByStatement(sql.toString());
	}

	public abstract T makePersistent(T entity);

	public T makeTransient(T entity) {
		// Object entityClass;
		String idFieldName;
		String methodName;

		for (Field f : entity.getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(Id.class)) {
				idFieldName = f.getName();
				methodName = "get" + StringUtils.capitalize(idFieldName);
				for (Method m : entity.getClass().getDeclaredMethods()) {
					if (m.getName().equals(methodName)) {
						// entityClass = m.getReturnType();
						break;
					}
				}
				break;
			}
		}
		return null;
	}
}
