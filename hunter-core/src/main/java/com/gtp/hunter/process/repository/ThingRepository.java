package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.gtp.hunter.common.enums.FieldType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.util.DBUtil;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class ThingRepository extends JPABaseRepository<Thing, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Thing>	tEvent;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public ThingRepository() {
		super(Thing.class, UUID.class);
	}

	public Thing findByUnit(Unit u) {
		if (u != null) {
			try {
				return em.createQuery("from Thing where :unId in ELEMENTS(units)", Thing.class)
								.setParameter("unId", u.getId())
								.setMaxResults(1)
								.setFirstResult(0)
								.getSingleResult();
			} catch (NoResultException nre) {
				//return null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public Thing findByTagId(String tagId) {
		if (tagId != null) {
			try {
				return em.createQuery("select t from Thing t join Unit u ON u.id IN ELEMENTS(t.units) where u.tagId = :tag", Thing.class)
								.setParameter("tag", tagId)
								.setMaxResults(1)
								.setFirstResult(0)
								.getSingleResult();
			} catch (NoResultException nre) {
				//return null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public Thing findByUnitId(UUID tagId) {
		if (tagId == null)
			return null;
		Thing ret = null;
		StringBuilder sql = new StringBuilder("SELECT t.*, prm.*, pm.*, p.*, u.* ");

		sql.append(" FROM thing t");
		sql.append("  INNER JOIN thingunits tu ON t.id = tu.thing_id ");
		sql.append("  INNER JOIN unit u ON tu.unit_id = u.id ");
		sql.append("  INNER JOIN product p ON t.product_id = p.id ");
		sql.append("  INNER JOIN productmodel pm ON p.productmodel_id = pm.id ");
		sql.append("  INNER JOIN propertymodel prm ON pm.propertymodel_id = prm.id ");
		sql.append(" WHERE u.id = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {

			ps.setString(1, tagId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					Unit u = new Unit(rs.getString("u.tagid"), UnitType.valueOf(rs.getString("u.type")));
					u.setId(UUID.fromString(rs.getString("u.id")));
					u.setName(rs.getString("u.name"));
					u.setMetaname(rs.getString("u.metaname"));
					u.setStatus(rs.getString("u.status"));

					if (ret == null) {
						PropertyModel prm = new PropertyModel(rs.getString("prm.name"), rs.getString("prm.metaname"));
						prm.setId(UUID.fromString(rs.getString("prm.id")));
						prm.setStatus(rs.getString("prm.status"));
						prm.setCreatedAt(rs.getTimestamp("prm.createdAt"));
						prm.setUpdatedAt(rs.getTimestamp("prm.updatedAt"));

						ProductModel pm = new ProductModel(rs.getString("pm.name"), rs.getString("pm.metaname"), prm, rs.getString("pm.status"));
						pm.setId(UUID.fromString(rs.getString("pm.id")));
						pm.setCreatedAt(rs.getTimestamp("pm.createdAt"));
						pm.setUpdatedAt(rs.getTimestamp("pm.updatedAt"));

						Product p = new Product(rs.getString("p.name"), pm, rs.getString("p.sku"), rs.getString("p.status"));
						p.setId(UUID.fromString(rs.getString("p.id")));
						p.setCreatedAt(rs.getTimestamp("p.createdAt"));
						p.setUpdatedAt(rs.getTimestamp("p.updatedAt"));

						ret = new Thing(rs.getString("t.name"), p, prm, rs.getString("t.status"));
						ret.setId(UUID.fromString(rs.getString("t.id")));
						ret.setMetaname(rs.getString("t.metaname"));
						ret.setUnits(new HashSet<UUID>(Arrays.asList(u.getId())));
						ret.setUnitModel(new HashSet<Unit>(Arrays.asList(u)));
						ret.setCreatedAt(rs.getTimestamp("t.createdAt"));
						ret.setUpdatedAt(rs.getTimestamp("t.updatedAt"));

						fillUnits(ret);
						fillProperties(ret);
					}
				}
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Thing quickFindByUnitTagId(String tagId) {
		if (tagId == null)
			return null;
		Thing ret = null;
		StringBuilder sql = new StringBuilder("SELECT t.*, prm.*, pm.*, p.*, u.* ");

		sql.append(" FROM thing t");
		sql.append("  INNER JOIN thingunits tu ON t.id = tu.thing_id ");
		sql.append("  INNER JOIN unit u ON tu.unit_id = u.id ");
		sql.append("  INNER JOIN product p ON t.product_id = p.id ");
		sql.append("  INNER JOIN productmodel pm ON p.productmodel_id = pm.id ");
		sql.append("  INNER JOIN propertymodel prm ON pm.propertymodel_id = prm.id ");
		sql.append(" WHERE u.tagid = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {

			ps.setString(1, tagId);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					Unit u = new Unit(rs.getString("u.tagid"), UnitType.valueOf(rs.getString("u.type")));
					u.setId(UUID.fromString(rs.getString("u.id")));
					u.setName(rs.getString("u.name"));
					u.setMetaname(rs.getString("u.metaname"));
					u.setStatus(rs.getString("u.status"));
					u.setCreatedAt(rs.getTimestamp("u.createdAt"));
					u.setUpdatedAt(rs.getTimestamp("u.updatedAt"));

					PropertyModel prm = new PropertyModel(rs.getString("prm.name"), rs.getString("prm.metaname"));
					prm.setId(UUID.fromString(rs.getString("prm.id")));
					prm.setStatus(rs.getString("prm.status"));
					prm.setCreatedAt(rs.getTimestamp("prm.createdAt"));
					prm.setUpdatedAt(rs.getTimestamp("prm.updatedAt"));

					ProductModel pm = new ProductModel(rs.getString("pm.name"), rs.getString("pm.metaname"), prm, rs.getString("pm.status"));
					pm.setId(UUID.fromString(rs.getString("pm.id")));
					pm.setCreatedAt(rs.getTimestamp("pm.createdAt"));
					pm.setUpdatedAt(rs.getTimestamp("pm.updatedAt"));

					Product p = new Product(rs.getString("p.name"), pm, rs.getString("p.sku"), rs.getString("p.status"));
					p.setId(UUID.fromString(rs.getString("p.id")));
					p.setCreatedAt(rs.getTimestamp("p.createdAt"));
					p.setUpdatedAt(rs.getTimestamp("p.updatedAt"));

					ret = new Thing(rs.getString("t.name"), p, prm, rs.getString("t.status"));
					ret.setId(UUID.fromString(rs.getString("t.id")));
					ret.setMetaname(rs.getString("t.metaname"));
					ret.setStatus(rs.getString("t.status"));
					ret.setCreatedAt(rs.getTimestamp("t.createdAt"));
					ret.setUpdatedAt(rs.getTimestamp("t.updatedAt"));
					ret.setUnits(new HashSet<UUID>(Arrays.asList(u.getId())));
					ret.setUnitModel(new HashSet<Unit>(Arrays.asList(u)));
					fillUnits(ret);
					fillProperties(ret);
				}
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	private void fillProperties(Thing t) {
		StringBuilder sql = new StringBuilder("SELECT pr.*, prmf.*");

		sql.append(" FROM property pr");
		sql.append("  INNER JOIN thing t ON pr.thing_id = t.id");
		sql.append("  INNER JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id AND prmf.propertymodel_id = t.propertymodel_id");
		sql.append(" WHERE pr.thing_id = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {

			if (t.getProperties() == null)
				t.setProperties(new HashSet<Property>());
			ps.setString(1, t.getId().toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					PropertyModelField modelField = new PropertyModelField(rs.getString("prmf.name"), t.getModel(), FieldType.valueOf(rs.getString("prmf.type")));

					modelField.setId(UUID.fromString(rs.getString("prmf.id")));
					modelField.setMetaname(rs.getString("prmf.metaname"));
					modelField.setStatus(rs.getString("prmf.status"));
					modelField.setCreatedAt(rs.getTimestamp("prmf.createdAt"));
					modelField.setUpdatedAt(rs.getTimestamp("prmf.updatedAt"));
					modelField.setOrdem(rs.getInt("prmf.ordem"));
					modelField.setVisible(rs.getBoolean("prmf.visible"));

					Property pr = new Property(t, modelField, rs.getString("pr.value"));

					pr.setId(UUID.fromString(rs.getString("pr.id")));
					pr.setName(rs.getString("pr.name"));
					pr.setMetaname(rs.getString("pr.metaname"));
					pr.setStatus(rs.getString("pr.status"));
					pr.setCreatedAt(rs.getTimestamp("pr.createdAt"));
					pr.setUpdatedAt(rs.getTimestamp("pr.updatedAt"));
					t.getProperties().add(pr);
				}
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fillUnits(Thing t) {
		StringBuilder sql = new StringBuilder("SELECT u.*");

		sql.append(" FROM unit u");
		sql.append("  INNER JOIN thingunits tu ON u.id = tu.unit_id ");
		sql.append(" WHERE tu.thing_id = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {

			ps.setString(1, t.getId().toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					UUID uId = UUID.fromString(rs.getString("u.id"));
					if (t.getUnitModel().parallelStream().noneMatch(u -> u.getId().equals(uId)));
					Unit u = new Unit(rs.getString("u.tagid"), UnitType.valueOf(rs.getString("u.type")));

					u.setId(uId);
					u.setName(rs.getString("u.name"));
					u.setMetaname(rs.getString("u.metaname"));
					u.setStatus(rs.getString("u.status"));
					u.setCreatedAt(rs.getTimestamp("u.createdAt"));
					u.setUpdatedAt(rs.getTimestamp("u.updatedAt"));
					t.getUnitModel().add(u);
					if (!t.getUnits().contains(uId))
						t.getUnits().add(uId);
				}
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Thing> listByParent(Thing parent) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Thing> cq = cb.createQuery(Thing.class);
		Root<Thing> cls = cq.from(Thing.class);
		cq.select(cls);
		cq.where(cb.equal(cls.get("parent"), parent));
		TypedQuery<Thing> q = em.createQuery(cq);
		List<Thing> ret = q.getResultList();
		return ret;
	}

	public int getCountThingsBySKUAndProperty(String sku, String property, String value) {
		int ret = 0;
		String qry = "select count(p.id) as qty\n" + "from propertymodelfield pmf \n" + "join property p on p.propertymodelfield_id = pmf.id \n" + "join thing t on p.thing_id = t.id\n" + "join product prod on t.product_id = prod.id\n" + "where pmf.metaname = ? \n" + "and p.value = ?\n" + "and prod.sku = ?";

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(qry);) {

			ps.setString(1, property);
			ps.setString(2, value);
			ps.setString(3, sku);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					ret = rs.getInt("qty");
				}
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Thing> listByProduct(Product prod) {
		return em.createQuery("from Thing where product = :product", Thing.class)
						.setParameter("product", prod)
						.getResultList();
	}

	public List<Thing> listByStatus(String status) {
		return em.createQuery("from Thing where status = :status", Thing.class)
						.setParameter("status", status)
						.getResultList();
	}

	public List<Thing> listByModel(PropertyModel ppm) {
		return em.createQuery("from Thing where model = :ppm", Thing.class)
						.setParameter("ppm", ppm)
						.getResultList();
	}

	public List<Thing> listByModelAndStatus(PropertyModel ppm, String status) {
		return em.createQuery("from Thing where model = :ppm and status = :status", Thing.class)
						.setParameter("ppm", ppm)
						.setParameter("status", status)
						.getResultList();
	}

	public List<Thing> listByModelMeta(String meta) {
		return em.createQuery("from Thing where model.metaname = :meta", Thing.class).setParameter("meta", meta).getResultList();
	}

	// TODO: ARRRRRRRRRRRRRRRRRGHHHHH QUE LIXOOOOOOOOO.... MORRI, desculpa ai
	public List<Thing> listByDocument(Document d) {
		List<Thing> ret = new ArrayList<>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(
						"SELECT t.*, prm.*, pm.*, p.*, u.*, batch.`value` as BATCH, manufacture.`value` AS MANUFACTURE, expiry.`value` AS EXPIRY, barcode.`value` AS BARCODE FROM thing t INNER JOIN thingunits tu ON t.id = tu.thing_id INNER JOIN unit u ON tu.unit_id = u.id INNER JOIN product p ON t.product_id = p.id INNER JOIN productmodel pm ON p.productmodel_id = pm.id INNER JOIN documentthing dt on dt.thing_id = t.id INNER JOIN document d ON dt.document_id = d.id INNER JOIN propertymodel prm ON pm.propertymodel_id = prm.id  INNER JOIN propertymodelfield batchfield ON prm.id = batchfield.propertymodel_id AND batchfield.metaname = 'BATCH' INNER JOIN property batch ON batchfield.id = batch.propertymodelfield_id AND batch.thing_id = t.id INNER JOIN propertymodelfield manufacturefield ON prm.id = manufacturefield.propertymodel_id AND manufacturefield.metaname = 'MANUFACTURE' INNER JOIN property manufacture ON manufacturefield.id = manufacture.propertymodelfield_id AND manufacture.thing_id = t.id INNER JOIN propertymodelfield expiryfield ON prm.id = expiryfield.propertymodel_id AND expiryfield.metaname = 'EXPIRY' INNER JOIN property expiry ON expiryfield.id = expiry.propertymodelfield_id AND expiry.thing_id = t.id INNER JOIN propertymodelfield barcodefield ON prm.id = barcodefield.propertymodel_id AND barcodefield.metaname = 'BARCODE' INNER JOIN property barcode ON barcodefield.id = barcode.propertymodelfield_id AND barcode.thing_id = t.id WHERE d.id = ?");) {

			ps.setString(1, d.getId().toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					Unit u = new Unit(rs.getString("u.tagid"), UnitType.EPC96);
					u.setId(UUID.fromString(rs.getString("u.id")));
					u.setName(rs.getString("u.name"));
					u.setMetaname(rs.getString("u.metaname"));
					u.setStatus(rs.getString("u.status"));
					PropertyModel prm = new PropertyModel(rs.getString("prm.name"), rs.getString("prm.metaname"));
					prm.setStatus(rs.getString("prm.status"));
					prm.setId(UUID.fromString(rs.getString("prm.id")));
					ProductModel pm = new ProductModel(rs.getString("pm.name"), rs.getString("pm.metaname"), prm, rs.getString("pm.status"));
					pm.setId(UUID.fromString(rs.getString("pm.id")));
					Product p = new Product(rs.getString("p.name"), pm, rs.getString("p.sku"), rs.getString("p.status"));
					p.setId(UUID.fromString(rs.getString("p.id")));
					p.setBarcode(rs.getString("p.barcode"));
					Property batch = new Property();
					batch.setMetaname("BATCH");
					batch.setValue(rs.getString("BATCH"));
					Property manufacture = new Property();
					manufacture.setValue(rs.getString("MANUFACTURE"));
					manufacture.setMetaname("MANUFACTURE");
					Property expiry = new Property();
					expiry.setValue(rs.getString("EXPIRY"));
					expiry.setMetaname("EXPIRY");
					Property barcode = new Property();
					barcode.setValue(rs.getString("BARCODE"));
					barcode.setMetaname("BARCODE");

					Thing t = new Thing(rs.getString("name"), p, prm, rs.getString("status"));
					t.setId(UUID.fromString(rs.getString("t.id")));
					t.setUnits(new HashSet<UUID>(Arrays.asList(UUID.fromString(rs.getString("u.id")))));
					t.setUnitModel(new HashSet<Unit>(Arrays.asList(u)));
					t.setProperties(new HashSet<Property>(Arrays.asList(batch, manufacture, expiry, barcode)));
					ret.add(t);
				}
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public List<Thing> quickListByProductId(UUID productId) {
		List<Thing> ret = new ArrayList<>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT t.* FROM thing t WHERE t.product_id = ?");) {

			ps.setString(1, productId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Thing>) DBUtil.resultSetToList(rs, Thing.class);
				rs.close();
			}
			closeConnection(con);
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Thing> quickListByProductIdAndStatus(UUID productId, String status) {
		List<Thing> ret = new ArrayList<>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT t.* FROM thing t WHERE t.product_id = ? AND t.status = ?");) {

			ps.setString(1, productId.toString());
			ps.setString(2, status);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Thing>) DBUtil.resultSetToList(rs, Thing.class);
				rs.close();
			}
			closeConnection(con);
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public void quickUpdateThingStatus(UUID thing, String status) {

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("update thing set status = ? where id = ?");) {

			ps.setString(1, status);
			ps.setString(2, thing.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void quickUpdateParentId(UUID thingId, UUID parentId) {
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("update thing set parent_id = ? where id = ?");) {

			ps.setString(1, parentId.toString());
			ps.setString(2, thingId.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void quickRemoveUnit(UUID thingId, UUID unitId) {
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("delete from thingunits WHERE thing_id = ? AND unit_id = ?");) {

			ps.setString(1, thingId.toString());
			ps.setString(2, unitId.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Thing dirtyInsert(Thing t) {
		if (t.getId() == null) t.setId(UUID.randomUUID());
		dirtyInsert(t.getId(), t.getMetaname(), t.getName(), t.getStatus(), t.getCreatedAt(), t.getAddress() != null ? t.getAddress().getId() : null, t.getModel() != null ? t.getModel().getId() : null, t.getParent() != null ? t.getParent().getId() : null, t.getProduct() != null ? t.getProduct().getId() : null);

		return t;
	}

	public UUID dirtyInsert(UUID id, String metaname, String name, String status, Date createdAt, UUID address_id, UUID propertymodel_id, UUID parent_id, UUID product_id) {
		StringBuilder sql = new StringBuilder("INSERT INTO thing");

		sql.append(" (id,metaname,name,status,createdAt,updatedAt,address_id,propertymodel_id,parent_id,product_id) ");
		sql.append(" VALUES");
		sql.append(" (?,?,?,?,?,now(),?,?,?,?)");
		sql.append(" ON DUPLICATE KEY UPDATE");
		sql.append(" metaname = ?,");
		sql.append(" name = ?,");
		sql.append(" status = ?,");
		sql.append(" createdAt = ?,");
		sql.append(" updatedAt = now(),");
		sql.append(" address_id = ?,");
		sql.append(" propertymodel_id = ?,");
		sql.append(" parent_id = ?,");
		sql.append(" product_id = ?;");
		if (id == null) {
			id = UUID.randomUUID();
		}
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, id.toString());
			ps.setString(2, metaname);
			ps.setString(10, metaname);
			ps.setString(3, name);
			ps.setString(11, name);
			ps.setString(4, status);
			ps.setString(12, status);
			ps.setTimestamp(5, createdAt != null ? new java.sql.Timestamp(createdAt.getTime()) : new java.sql.Timestamp(new Date().getTime()));
			ps.setTimestamp(13, createdAt != null ? new java.sql.Timestamp(createdAt.getTime()) : new java.sql.Timestamp(new Date().getTime()));
			if (address_id != null) {
				ps.setString(6, address_id.toString());
				ps.setString(14, address_id.toString());
			} else {
				ps.setNull(6, Types.CHAR);
				ps.setNull(14, Types.CHAR);
			}
			if (propertymodel_id != null) {
				ps.setString(7, propertymodel_id.toString());
				ps.setString(15, propertymodel_id.toString());
			} else {
				ps.setNull(7, Types.CHAR);
				ps.setNull(15, Types.CHAR);
			}
			if (parent_id != null) {
				ps.setString(8, parent_id.toString());
				ps.setString(16, parent_id.toString());
			} else {
				ps.setNull(8, Types.CHAR);
				ps.setNull(16, Types.CHAR);
			}
			if (product_id != null) {
				ps.setString(9, product_id.toString());
				ps.setString(17, product_id.toString());
			} else {
				ps.setNull(9, Types.CHAR);
				ps.setNull(17, Types.CHAR);
			}
			ps.executeUpdate();
			ps.close();
		} catch (SQLIntegrityConstraintViolationException e) {
			dirtyUpdate(id, metaname, name, status, createdAt, address_id, propertymodel_id, parent_id, product_id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public void dirtyUpdate(UUID id, String metaname, String name, String status, Date createdAt, UUID address_id, UUID propertymodel_id, UUID parent_id, UUID product_id) {
		if (id == null) {
			return;
		}

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("UPDATE thing " + "SET metaname = ?, name = ?, status = ?, createdAt = ?, updatedAt = now(), address_id = ?, propertymodel_id = ?, parent_id = ?, product_id = ? WHERE id = ?");) {
			ps.setString(1, metaname);
			ps.setString(2, name);
			ps.setString(3, status);
			ps.setTimestamp(4, createdAt != null ? new java.sql.Timestamp(createdAt.getTime()) : new java.sql.Timestamp(new Date().getTime()));
			ps.setString(5, address_id != null ? address_id.toString() : null);
			ps.setString(6, propertymodel_id != null ? propertymodel_id.toString() : null);
			ps.setString(7, parent_id != null ? parent_id.toString() : null);
			ps.setString(8, product_id != null ? product_id.toString() : null);
			ps.setString(9, id.toString());
			ps.close();
		} catch (SQLIntegrityConstraintViolationException ignored) {
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static int getRowCount(Connection con) throws SQLException {
		Statement st = con.createStatement();
		ResultSet rowCount = st.executeQuery("SELECT FOUND_ROWS()");

		rowCount.next();
		return rowCount.getInt(1);
	}

	public List<Thing> listByStatusLimit(String status, int start, int end) {
		List<Thing> ret = new ArrayList<>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(
						"SELECT SQL_CALC_FOUND_ROWS t.*, prm.*, pm.*, p.*, u.*, batch.`value` as BATCH, manufacture.`value` AS MANUFACTURE, expiry.`value` AS EXPIRY, barcode.`value` AS BARCODE FROM thing t INNER JOIN thingunits tu ON t.id = tu.thing_id INNER JOIN unit u ON tu.unit_id = u.id INNER JOIN product p ON t.product_id = p.id INNER JOIN productmodel pm ON p.productmodel_id = pm.id INNER JOIN documentthing dt on dt.thing_id = t.id INNER JOIN document d ON dt.document_id = d.id INNER JOIN propertymodel prm ON pm.propertymodel_id = prm.id  INNER JOIN propertymodelfield batchfield ON prm.id = batchfield.propertymodel_id AND batchfield.metaname = 'BATCH' INNER JOIN property batch ON batchfield.id = batch.propertymodelfield_id AND batch.thing_id = t.id INNER JOIN propertymodelfield manufacturefield ON prm.id = manufacturefield.propertymodel_id AND manufacturefield.metaname = 'MANUFACTURE' INNER JOIN property manufacture ON manufacturefield.id = manufacture.propertymodelfield_id AND manufacture.thing_id = t.id INNER JOIN propertymodelfield expiryfield ON prm.id = expiryfield.propertymodel_id AND expiryfield.metaname = 'EXPIRY' INNER JOIN property expiry ON expiryfield.id = expiry.propertymodelfield_id AND expiry.thing_id = t.id INNER JOIN propertymodelfield barcodefield ON prm.id = barcodefield.propertymodel_id AND barcodefield.metaname = 'BARCODE' INNER JOIN property barcode ON barcodefield.id = barcode.propertymodelfield_id AND barcode.thing_id = t.id WHERE t.status = ? LIMIT ?, ?");) {

			ps.setString(1, status);
			ps.setInt(2, start);
			ps.setInt(3, end);
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					Unit u = new Unit(rs.getString("u.tagid"), UnitType.EPC96);
					u.setId(UUID.fromString(rs.getString("u.id")));
					u.setName(rs.getString("u.name"));
					u.setMetaname(rs.getString("u.metaname"));
					u.setStatus(rs.getString("u.status"));
					PropertyModel prm = new PropertyModel(rs.getString("prm.name"), rs.getString("prm.metaname"));
					prm.setStatus(rs.getString("prm.status"));
					prm.setId(UUID.fromString(rs.getString("prm.id")));
					ProductModel pm = new ProductModel(rs.getString("pm.name"), rs.getString("pm.metaname"), prm, rs.getString("pm.status"));
					pm.setId(UUID.fromString(rs.getString("pm.id")));
					Product p = new Product(rs.getString("p.name"), pm, rs.getString("p.sku"), rs.getString("p.status"));
					p.setId(UUID.fromString(rs.getString("p.id")));
					p.setBarcode(rs.getString("p.barcode"));
					Property batch = new Property();
					batch.setMetaname("BATCH");
					batch.setValue(rs.getString("BATCH"));
					Property manufacture = new Property();
					manufacture.setValue(rs.getString("MANUFACTURE"));
					manufacture.setMetaname("MANUFACTURE");
					Property expiry = new Property();
					expiry.setValue(rs.getString("EXPIRY"));
					expiry.setMetaname("EXPIRY");
					Property barcode = new Property();
					barcode.setValue(rs.getString("BARCODE"));
					barcode.setMetaname("BARCODE");

					Thing t = new Thing(rs.getString("name"), p, prm, rs.getString("status"));
					t.setId(UUID.fromString(rs.getString("t.id")));
					t.setUnits(new HashSet<UUID>(Arrays.asList(UUID.fromString(rs.getString("u.id")))));
					t.setUnitModel(new HashSet<Unit>(Arrays.asList(u)));
					t.setProperties(new HashSet<Property>(Arrays.asList(batch, manufacture, expiry, barcode)));
					t.setMetaname(getRowCount(con) + "");
					ret.add(t);
				}
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	protected Event<Thing> getEvent() {
		return tEvent;
	}

	public List<Thing> listByAddressId(UUID addrId) {
		return em.createQuery("from Thing where address.id = :addrId", Thing.class).setParameter("addrId", addrId).getResultList();
	}

	public List<Thing> listByAddressIdNoOrphan(UUID addrId) {
		return em.createQuery("from Thing where address.id = :addrId and parent.id is not null", Thing.class).setParameter("addrId", addrId).getResultList();
	}

	public List<Thing> listParentByChildAddressId(UUID addrId) {
		return em.createQuery("from Thing where address.parent.id = :addrId and parent.id is null", Thing.class).setParameter("addrId", addrId).getResultList();
	}

	public List<Thing> listByChildAddressId(UUID addrId) {
		return em.createQuery("from Thing where address.parent.id = :addrId and parent.id is not null", Thing.class).setParameter("addrId", addrId).getResultList();
	}

	public List<Thing> listByLocationId(UUID locId) {
		return em.createQuery("from Thing where address.location.id = :locId", Thing.class).setParameter("locId", locId).getResultList();
	}

	public List<Thing> listByLocationAndProduct(Location loc, Product prd) {
		return em.createQuery("from Thing where address.location = :loc and product = :prd", Thing.class).setParameter("loc", loc).setParameter("prd", prd).getResultList();
	}

	public List<Thing> listByProductIdAndStatus(UUID prdId, String status) {
		return em.createQuery("from Thing where product.id = :prdId and status = :status", Thing.class).setParameter("prdId", prdId).setParameter("status", status).getResultList();
	}

	public List<Thing> listByProductIdAndNotStatus(UUID prdId, String status) {
		return em.createQuery("from Thing where product.id = :prdId and status <> 'CANCELADO' and status <> :status", Thing.class).setParameter("prdId", prdId).setParameter("status", status).getResultList();
	}

	public List<Thing> listByProductId(UUID prdId) {
		return em.createQuery("from Thing where product.id = :prdId", Thing.class).setParameter("prdId", prdId).getResultList();
	}

	public List<Thing> quickListByModelAndPropertyValue(String model, String fieldMeta, String value) {
		List<Thing> ret = null;
		StringBuilder sql = new StringBuilder("SELECT t.* ");

		sql.append(" FROM thing t ");
		sql.append(" INNER JOIN property pr ON pr.thing_id = t.id ");
		sql.append(" INNER JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id ");
		sql.append(" INNER JOIN propertymodel prm ON prmf.propertymodel_id = prm.id ");
		sql.append(" WHERE prmf.metaname = ? ");
		sql.append("	AND pr.value = ?");
		sql.append("	AND prm.metaname = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, fieldMeta);
			ps.setString(2, value);
			ps.setString(3, model);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Thing>) DBUtil.resultSetToList(rs, Thing.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Thing> quickListByModelAndStatusAndPropertyValue(String model, String status, String fieldMeta, String value) {
		List<Thing> ret = null;
		StringBuilder sql = new StringBuilder("SELECT t.* ");

		sql.append(" FROM thing t ");
		sql.append(" INNER JOIN property pr ON pr.thing_id = t.id ");
		sql.append(" INNER JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id ");
		sql.append(" INNER JOIN propertymodel prm ON prmf.propertymodel_id = prm.id ");
		sql.append(" WHERE prmf.metaname = ? ");
		sql.append("	AND pr.value = ?");
		sql.append("	AND t.status = ?");
		sql.append("	AND prm.metaname = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, fieldMeta);
			ps.setString(2, value);
			ps.setString(3, status);
			ps.setString(4, model);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Thing>) DBUtil.resultSetToList(rs, Thing.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Thing> quickListByPropertyValue(String fieldMeta, String value) {
		List<Thing> ret = null;
		StringBuilder sql = new StringBuilder("SELECT t.* ");

		sql.append(" FROM thing t ");
		sql.append(" INNER JOIN property pr ON pr.thing_id = t.id ");
		sql.append(" INNER JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id ");
		sql.append(" WHERE prmf.metaname = ? ");
		sql.append("	AND pr.value = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, fieldMeta);
			ps.setString(2, value);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Thing>) DBUtil.resultSetToList(rs, Thing.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Thing findByAddress(Address add) {
		if (add != null) {
			try {
				return em.createQuery("from Thing where address = :add", Thing.class)
								.setParameter("add", add)
								.setMaxResults(1)
								.setFirstResult(0)
								.getSingleResult();
			} catch (NoResultException nre) {
				//return null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
