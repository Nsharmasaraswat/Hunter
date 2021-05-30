package com.gtp.hunter.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.model.Unit;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class UnitRepository extends NoJPABaseRepository<Unit, UUID> {

	private static String			qryGetUnitByTagId			= "select * from unit where tagId = ?";
	private static String			qryGetUnitByTypeAndTagId	= "SELECT * FROM unit WHERE type = ? AND tagId = ?";
	private static String			qryGetAll					= "select * from unit";
	private static String			qryGetById					= "select * from unit where id = ?";
	private static String			qryGetByMetaname			= "select * from unit where metaname = ?";
	private static String			qrylistByFieldIn			= "select * from unit where ${fld} in ('${lst}')";
	private static String			qryGetByField				= "select * from unit where ${fld} = ? ";
	private static String			qryInsert					= "INSERT INTO unit (id, metaname, name, status, createdAt, updatedAt, tagId, type) VALUES(?,?,?,?,?,?,?,?)";
	private static String			qryUpdate					= "UPDATE unit SET metaname = ?, name = ?, status = ?, createdAt = ?, updatedAt = ?, tagId = ?, type = ? WHERE id = ?";

	@Resource(mappedName = "java:/hunter2")
	private javax.sql.DataSource	em;

	public UnitRepository() {
		super(Unit.class, UUID.class);
	}

	//@Override
	protected DataSource getDataSource() {
		return em;
	}

	public Unit getUnitByTagId(String tagId) {
		List<Unit> lst = new ArrayList<Unit>();
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(qryGetUnitByTagId);) {

			stmt.setString(1, tagId);
			try (ResultSet rs = stmt.executeQuery();) {
				lst = resultSetToList(rs);
				rs.close();
			}

			stmt.close();
			conn.close();
			if (lst.size() > 0)
				return lst.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	public Unit findByTypeAndTagId(UnitType type, String tagId) {
		List<Unit> lst = new ArrayList<Unit>();
		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(qryGetUnitByTypeAndTagId);) {

			stmt.setString(1, type.toString());
			stmt.setString(2, tagId);
			try (ResultSet rs = stmt.executeQuery();) {
				lst = resultSetToList(rs);
				rs.close();
			}

			stmt.close();
			conn.close();
			if (lst.size() > 0)
				return lst.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public List<Unit> listAll() {
		List<Unit> ret = new ArrayList<Unit>();
		try (Connection conn = getConnection();
						PreparedStatement stmt = conn.prepareStatement(qryGetAll);
						ResultSet rs = stmt.executeQuery();) {
			ret = resultSetToList(rs);
			rs.close();
			stmt.close();
			conn.close();
			closeConnection(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public Unit findById(UUID i) {

		List<Unit> lst = new ArrayList<Unit>();

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(qryGetById);) {

			stmt.setString(1, i.toString());
			try (ResultSet rs = stmt.executeQuery();) {
				lst = resultSetToList(rs);
				rs.close();
			}
			stmt.close();
			conn.close();
			if (lst.size() > 0) {
				Unit u = lst.get(0);

				return u;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Unit findByMetaname(String meta) {

		List<Unit> lst = new ArrayList<Unit>();

		try (Connection conn = getConnection();
						PreparedStatement stmt = conn.prepareStatement(qryGetByMetaname);) {

			stmt.setString(1, meta);
			try (ResultSet rs = stmt.executeQuery();) {
				lst = resultSetToList(rs);
				rs.close();
			}

			stmt.close();
			conn.close();
			if (lst.size() > 0)
				return lst.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Unit findByField(String fld, Object val) {
		Unit ret = null;
		List<Unit> lstret = new ArrayList<Unit>();
		try (Connection conn = getConnection();
						PreparedStatement stmt = conn.prepareStatement(qryGetByField.replace("${fld}", fld));) {

			stmt.setString(1, val.toString());
			try (ResultSet rs = stmt.executeQuery();) {
				lstret = resultSetToList(rs);
				rs.close();
			}

			stmt.close();
			conn.close();
			if (lstret.size() > 0)
				ret = lstret.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public List<Unit> listById(Collection<UUID> ids) {
		return listByFieldIn("id", ids.parallelStream().map(i -> i.toString()).collect(Collectors.toList()));
	}

	@Override
	public List<Unit> listByFieldIn(String fld, List<String> val) {
		List<Unit> ret = new ArrayList<Unit>();
		String items = val.parallelStream().collect(Collectors.joining("','"));

		try (Connection conn = getConnection();
						PreparedStatement stmt = conn.prepareStatement(qrylistByFieldIn.replace("${fld}", fld).replace("${lst}", items));) {

			try (ResultSet rs = stmt.executeQuery();) {
				ret = resultSetToList(rs);
				rs.close();
			}

			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public List<Unit> listByField(String fld, Object val) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeById(UUID id) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Unit insert(Unit t) {
		try (Connection conn = getConnection();
						PreparedStatement stmt = conn.prepareStatement(qryInsert);) {
			SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			UUID id = UUID.randomUUID();
			t.setId(id);
			stmt.setString(1, id.toString());
			stmt.setString(2, t.getMetaname());
			stmt.setString(3, t.getName());
			stmt.setString(4, t.getStatus());
			stmt.setString(5, sdf.format(new Date()));
			stmt.setString(6, sdf.format(new Date()));
			stmt.setString(7, t.getTagId());
			stmt.setString(8, t.getType().toString());
			stmt.executeUpdate();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return t;
	}

	@Override
	protected Unit update(Unit t) {
		try (Connection conn = getConnection();
						PreparedStatement stmt = conn.prepareStatement(qryUpdate);) {
			SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			stmt.setString(1, t.getMetaname());
			stmt.setString(2, t.getName());
			stmt.setString(3, t.getStatus());
			stmt.setString(4, sdf.format(new Date()));
			stmt.setString(5, sdf.format(new Date()));
			stmt.setString(6, t.getTagId());
			stmt.setString(7, t.getType().toString());
			stmt.setString(8, t.getId().toString());
			stmt.executeUpdate();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return t;
	}

	@Override
	protected List<Unit> resultSetToList(ResultSet rs) {
		List<Unit> ret = new ArrayList<Unit>();

		try {
			while (rs.next()) {
				Unit u = new Unit();

				u.setId(UUID.fromString(rs.getString("id")));
				u.setCreatedAt(rs.getTimestamp("createdAt"));
				u.setMetaname(rs.getString("metaname"));
				u.setName(rs.getString("name"));
				u.setStatus(rs.getString("status"));
				u.setTagId(rs.getString("tagId"));
				u.setType(UnitType.valueOf(rs.getString("type")));
				u.setUpdatedAt(rs.getTimestamp("updatedAt"));
				ret.add(u);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

}
