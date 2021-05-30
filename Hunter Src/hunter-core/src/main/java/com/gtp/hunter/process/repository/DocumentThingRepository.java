package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

import com.gtp.hunter.common.enums.FieldType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Person;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DocumentThingRepository extends JPABaseRepository<DocumentThing, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager			em;

	@Inject
	private Event<DocumentThing>	dtEvent;

	public DocumentThingRepository() {
		super(DocumentThing.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public DocumentThing findByDocumentAndThing(Document d, Thing t) {
		try {
			return em.createQuery("from DocumentThing where document = :d and thing = :t", DocumentThing.class)
							.setParameter("d", d)
							.setParameter("t", t)
							.setMaxResults(1)
							.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public DocumentThing findByDocumentIdAndThingId(UUID docId, UUID thId) {
		try {
			return em.createQuery("from DocumentThing where document.id = :d and thing.id = :t", DocumentThing.class)
							.setParameter("d", docId)
							.setParameter("t", thId)
							.setMaxResults(1)
							.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<DocumentThing> listByDocumentId(UUID documentId) {
		List<DocumentThing> ret = new ArrayList<DocumentThing>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select dt.id, dt.createdAt, dt.updatedAt,\r\n" + "dt.status, p.sku, u.tagid, p.name, t.status as tstatus, pty.value\r\n" + "from document d \r\n" + "join documentmodel dm on d.documentmodel_id = dm.id \r\n" + "join documentthing dt on dt.document_id = d.id \r\n" + "join thing t on dt.thing_id = t.id \r\n" + "join product p on t.product_id = p.id \r\n" + "join thingunits tu on tu.thing_id = t.id \r\n" + "join unit u on tu.unit_id = u.id \r\n" + "left outer join property pty on pty.thing_id = t.id\r\n" + "left outer join propertymodelfield pmf on pty.propertymodelfield_id = pmf.id\r\n" + "where d.id = ? " + "and pmf.metaname = 'BATCH'");) {

			ps.setString(1, documentId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					DocumentThing dt = new DocumentThing();
					dt.setId(UUID.fromString(rs.getString("id")));
					dt.setName(rs.getString("name"));
					dt.setCreatedAt(rs.getDate("createdAt"));
					dt.setUpdatedAt(rs.getDate("updatedAt"));
					dt.setStatus(rs.getString("status"));
					dt.setSku(rs.getString("sku"));
					dt.setUnit(rs.getString("tagid"));
					dt.setDescription(rs.getString("name"));
					dt.setTstatus(rs.getString("tstatus"));
					dt.settLot(rs.getString("value"));
					ret.add(dt);
				}
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<DocumentThing> getQuickByTypeCode(String type, String code) {
		List<DocumentThing> ret = new ArrayList<DocumentThing>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select dt.id, dt.createdAt, dt.updatedAt, " + "dt.status, p.sku, u.tagid, t.status as tstatus " + "from document d " + "join documentmodel dm on d.documentmodel_id = dm.id " + "join documentthing dt on dt.document_id = d.id " + "join thing t on dt.thing_id = t.id " + "join product p on t.product_id = p.id " + "join thingunits tu on tu.thing_id = t.id " + "join unit u on tu.unit_id = u.id " + "where d.code = ? and dm.metaname = ? ");) {

			ps.setString(1, code);
			ps.setString(2, type);
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					DocumentThing dt = new DocumentThing();
					dt.setId(UUID.fromString(rs.getString("id")));
					dt.setCreatedAt(rs.getDate("createdAt"));
					dt.setUpdatedAt(rs.getDate("updatedAt"));
					dt.setStatus(rs.getString("status"));
					dt.setSku(rs.getString("sku"));
					dt.setUnit(rs.getString("tagid"));
					dt.setTstatus(rs.getString("tstatus"));
					ret.add(dt);
				}
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<DocumentThing> listNoParentsWithThingPropertyAndProductByTypeCode(String type, String code) {
		Profiler prof = new Profiler();
		List<DocumentThing> ret = new ArrayList<DocumentThing>();
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT d.id AS dId, d.name AS dName, d.metaname AS dMetaname, d.createdAt AS dCreatedAt, d.updatedAt AS dUpdatedAt, d.status AS dStatus, d.code AS dCode, d.documentmodel_id AS dDocumentModelId, d.parent_id AS dParentId, d.supplier_id AS dSupplierId ");
		sql.append(" ,s.id AS sId, s.name AS sName, s.metaname AS sMetaname, s.createdAt AS sCreatedAt, s.updatedAt AS sUpdatedAt, s.status AS sStatus, s.extId AS sExtId, s.hasprinter AS sHasPrinter");
		sql.append(" ,dm.id AS dmId, dm.name AS dmName, dm.metaname AS dmMetaname, dm.createdAt AS dmCreatedAt, dm.updatedAt AS dmUpdatedAt, dm.status AS dmStatus, dm.classe AS dmClasse, dm.parent_id AS dmParentId ");
		sql.append(" ,dt.id AS dtId, dt.name AS dtName, dt.metaname AS dtMetaname, dt.createdAt AS dtCreatedAt, dt.updatedAt AS dtUpdatedAt, dt.status AS dtStatus, dt.document_id AS dtDocumentId, dt.thing_id AS dtThingId ");
		sql.append(" ,t.id AS tId, t.name AS tName, t.metaname AS tMetaname, t.createdAt AS tCreatedAt, t.updatedAt AS tUpdatedAt, t.status AS tStatus, t.propertymodel_id AS tPropertyModelId, t.parent_id AS tParentId, t.product_id AS tProductId, t.address_id AS tAddressId ");
		sql.append(" ,p.id AS pId, p.name AS pName, p.metaname AS pMetaname, p.createdAt AS pCreatedAt, p.updatedAt AS pUpdatedAt, p.status AS pStatus, p.sku AS pSku, p.productmodel_id AS pProductModelId, p.parent_id AS pParentId, p.barcode AS pBarcode ");
		sql.append(" ,pr.id AS prId, pr.name AS prName, pr.metaname AS prMetaname, pr.createdAt AS prCreatedAt, pr.updatedAt AS prUpdatedAt, pr.status AS prStatus, pr.value AS prValue, pr.propertymodelfield_id AS prPropertyModelFieldId, pr.thing_id AS prThingId ");
		sql.append(" ,prmf.id AS prmfId, prmf.name AS prmfName, prmf.metaname AS prmfMetaname, prmf.createdAt AS prmfCreatedAt, prmf.updatedAt AS prmfUpdatedAt, prmf.status AS prmfStatus, prmf.type AS prmfType, prmf.propertymodel_id AS prmfPropertyModelId, prmf.visible AS prmfVisible ");
		sql.append(" ,prm.id AS prmId, prm.name AS prmName, prm.metaname AS prmMetaname, prm.createdAt AS prmCreatedAt, prm.updatedAt AS prmUpdatedAt, prm.status AS prmStatus ");
		sql.append(" ,u.id AS uId, u.name AS uName, u.metaname AS uMetaname, u.createdAt AS uCreatedAt, u.updatedAt AS uUpdatedAt, u.status AS uStatus, u.tagId AS uTagId, u.type AS uType ");
		sql.append(" FROM document d ");
		sql.append(" INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id ");
		sql.append(" INNER JOIN documentthing dt ON dt.document_id = d.id ");
		sql.append(" INNER JOIN thing t ON dt.thing_id = t.id ");
		sql.append(" INNER JOIN product p ON t.product_id = p.id ");
		sql.append(" INNER JOIN thingunits tu ON tu.thing_id = t.id ");
		sql.append(" INNER JOIN unit u ON tu.unit_id = u.id ");
		sql.append(" LEFT JOIN property pr ON pr.thing_id = t.id ");
		sql.append(" LEFT JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id ");
		sql.append(" LEFT JOIN propertymodel prm ON prmf.propertymodel_id = prm.id ");
		sql.append(" LEFT JOIN supplier s ON d.supplier_id = s.id");
		sql.append(" WHERE d.code = ? AND dm.metaname = ? ");
		sql.append(" ORDER BY dt.id, dt.updatedAt, u.tagId");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, code);
			ps.setString(2, type);
			try (ResultSet rs = ps.executeQuery();) {
				DocumentThing lastDt = new DocumentThing();

				prof.step("Query Executed", false);
				while (rs.next()) {
					DocumentModel dm = null;
					String dmId = rs.getString("dmId");

					if (!rs.wasNull()) {
						dm = new DocumentModel();
						dm.setId(UUID.fromString(dmId));
						dm.setName(rs.getString("dmName"));
						dm.setMetaname(rs.getString("dmMetaname"));
						dm.setCreatedAt(rs.getDate("dmCreatedAt"));
						dm.setUpdatedAt(rs.getDate("dmUpdatedAt"));
						dm.setStatus(rs.getString("dmStatus"));
						dm.setClasse(rs.getString("dmClasse"));
					}

					Person s = null;
					String sId = rs.getString("sId");

					if (!rs.wasNull()) {
						s = new Person();
						s.setId(UUID.fromString(sId));
						s.setName(rs.getString("sName"));
						s.setMetaname(rs.getString("sMetaname"));
						s.setCreatedAt(rs.getDate("sCreatedAt"));
						s.setUpdatedAt(rs.getDate("sUpdatedAt"));
						s.setStatus(rs.getString("sStatus"));
					}

					Document d = null;
					String dId = rs.getString("dId");

					if (!rs.wasNull()) {
						d = new Document();
						d.setId(UUID.fromString(dId));
						d.setName(rs.getString("dName"));
						d.setMetaname(rs.getString("dMetaname"));
						d.setCreatedAt(rs.getDate("dCreatedAt"));
						d.setUpdatedAt(rs.getDate("dUpdatedAt"));
						d.setStatus(rs.getString("dStatus"));
						d.setCode(rs.getString("dCode"));
						d.setModel(dm);
						d.setPerson(s);
					}

					Product p = null;
					String pId = rs.getString("pId");

					if (!rs.wasNull()) {
						p = new Product();
						p.setId(UUID.fromString(pId));
						p.setName(rs.getString("pName"));
						p.setMetaname(rs.getString("pMetaname"));
						p.setCreatedAt(rs.getDate("pCreatedAt"));
						p.setUpdatedAt(rs.getDate("pUpdatedAt"));
						p.setStatus(rs.getString("pStatus"));
						p.setSku(rs.getString("pSku"));
					}

					PropertyModel prm = null;
					String prmId = rs.getString("prmId");

					if (!rs.wasNull()) {
						prm = new PropertyModel();
						prm.setId(UUID.fromString(prmId));
						prm.setName(rs.getString("prmName"));
						prm.setMetaname(rs.getString("prmMetaname"));
						prm.setCreatedAt(rs.getDate("prmCreatedAt"));
						prm.setUpdatedAt(rs.getDate("prmUpdatedAt"));
						prm.setStatus(rs.getString("prmStatus"));
					}

					PropertyModelField prmf = null;
					String prmfId = rs.getString("prmfId");

					if (!rs.wasNull()) {
						prmf = new PropertyModelField();
						prmf.setId(UUID.fromString(prmfId));
						prmf.setName(rs.getString("prmfName"));
						prmf.setMetaname(rs.getString("prmfMetaname"));
						prmf.setCreatedAt(rs.getDate("prmfCreatedAt"));
						prmf.setUpdatedAt(rs.getDate("prmfUpdatedAt"));
						prmf.setStatus(rs.getString("prmfStatus"));
						prmf.setType(FieldType.valueOf(rs.getString("prmfType")));
						prmf.setModel(prm);
						prmf.setVisible(rs.getBoolean("prmfVisible"));
					}

					Property pr = null;
					String prId = rs.getString("prId");

					if (!rs.wasNull()) {
						pr = new Property();
						pr.setId(UUID.fromString(prId));
						pr.setName(rs.getString("prName"));
						pr.setMetaname(rs.getString("prMetaname"));
						pr.setCreatedAt(rs.getDate("prCreatedAt"));
						pr.setUpdatedAt(rs.getDate("prUpdatedAt"));
						pr.setStatus(rs.getString("prStatus"));
						pr.setValue(rs.getString("prValue"));
						pr.setField(prmf);
					}

					Unit u = null;
					String uId = rs.getString("uId");

					if (!rs.wasNull()) {
						u = new Unit();
						u.setId(UUID.fromString(uId));
						u.setName(rs.getString("uName"));
						u.setMetaname(rs.getString("uMetaname"));
						u.setCreatedAt(rs.getDate("uCreatedAt"));
						u.setUpdatedAt(rs.getDate("uUpdatedAt"));
						u.setStatus(rs.getString("uStatus"));
						u.setTagId(rs.getString("uTagId"));
						u.setType(UnitType.valueOf(rs.getString("uType")));
					}

					DocumentThing dt = null;
					String dtId = rs.getString("dtId");

					if (!rs.wasNull()) {
						dt = new DocumentThing();
						dt.setId(UUID.fromString(dtId));
						dt.setName(rs.getString("dtName"));
						dt.setMetaname(rs.getString("dtMetaname"));
						dt.setCreatedAt(rs.getDate("dtCreatedAt"));
						dt.setUpdatedAt(rs.getDate("dtUpdatedAt"));
						dt.setStatus(rs.getString("dtStatus"));
						dt.setSku(rs.getString("pSku"));
						dt.setUnit(rs.getString("uTagId"));
						dt.setTstatus(rs.getString("tStatus"));
						dt.setDocument(d);
					}

					Thing t = null;
					String tId = rs.getString("tId");

					if (!rs.wasNull()) {
						if (lastDt.getId() == null || lastDt.getThing() == null || !lastDt.getId().equals(dt.getId())) {
							t = new Thing();
							t.setUnits(new HashSet<UUID>());
							t.setProperties(new HashSet<Property>());
						} else
							t = lastDt.getThing();
						t.setId(UUID.fromString(tId));
						t.setName(rs.getString("tName"));
						t.setMetaname(rs.getString("tMetaname"));
						t.setCreatedAt(rs.getDate("tCreatedAt"));
						t.setUpdatedAt(rs.getDate("tUpdatedAt"));
						t.setStatus(rs.getString("tStatus"));
						t.setProduct(p);
						t.setModel(prm);
						t.getUnits().add(u.getId());
						t.getProperties().add(pr);
						dt.setThing(t);
					}
					if (lastDt.getId() == null) {
						lastDt = dt;
					} else if (lastDt.getId().equals(dt.getId())) {
						lastDt.setThing(t);
					} else {
						ret.add(lastDt);
						lastDt = dt;
					}
				}
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		prof.done("ListDocumentThings: " + ret.size() + " Average: " + ((prof.getLap().ceilingKey(prof.getLap().firstKey() + 1) - prof.getT0()) / (ret.size() > 0 ? ret.size() : 1)), false, true);
		return ret;
	}

	public int getQuickDocumentThingCountByDocAndProduct(UUID docid, UUID prodid) {

		int res = 0;
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select count(dt.id) as qty from documentthing dt join thing t on dt.thing_id = t.id where dt.document_id = ? and t.product_id = ? and dt.status <> 'CANCELADO'");) {

			ps.setString(1, docid.toString());
			ps.setString(2, prodid.toString());
			try (ResultSet rs = ps.executeQuery();) {
				rs.next();
				res = rs.getInt("qty");
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	public int getQuickDocumentThingCountByDocAndStatus(UUID docid, String status) {
		int res = 0;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select count(dt.id) as qty from documentthing dt join thing t on dt.thing_id = t.id where dt.document_id = ? and t.product_id = ? and dt.status <> 'CANCELADO'");) {

			ps.setString(1, docid.toString());
			ps.setString(2, status);
			try (ResultSet rs = ps.executeQuery();) {
				rs.next();
				res = rs.getInt("qty");
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	public DocumentThing quickFindByThingIdAndDocModelMeta(UUID thing, String docmeta) {
		DocumentThing dt = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select dt.*, d.name, d.status, d.code from documentthing dt join document d on d.id = dt.document_id join documentmodel dm on d.documentmodel_id = dm.id where dt.thing_id = ? and dm.metaname = ? ORDER BY dt.createdAt DESC LIMIT 1");) {

			ps.setString(1, thing.toString());
			ps.setString(2, docmeta);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					dt = new DocumentThing();
					dt.setId(UUID.fromString(rs.getString("dt.id")));
					dt.setMetaname(rs.getString("dt.metaname"));
					dt.setName(rs.getString("dt.name"));
					dt.setStatus(rs.getString("dt.status"));
					Document d = new Document();

					d.setId(UUID.fromString(rs.getString("dt.document_id")));
					d.setName(rs.getString("d.name"));
					d.setStatus(rs.getString("d.status"));
					d.setCode(rs.getString("d.code"));
					dt.setDocument(d);
				}
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return dt;
	}

	public void quickUpdateStatus(UUID id, String status) {

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("update documentthing set status = ? where id = ?");) {

			ps.setString(1, status);
			ps.setString(2, id.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void quickInsert(UUID doc, UUID thing, String status) {
		StringBuilder sql = new StringBuilder("INSERT INTO documentthing");

		sql.append(" (id, createdAt, updatedAt, status, document_id, thing_id)");
		sql.append(" VALUES ");
		sql.append("(uuid(),now(),now(),?,?,?)");
		sql.append(" ON DUPLICATE KEY UPDATE");
		sql.append(" metaname = values(metaname)");
		sql.append(",name=values(name)");
		sql.append(",status=values(status)");
		sql.append(",createdAt=values(createdAt)");
		sql.append(",updatedAt=values(updatedAt)");
		sql.append(",document_id=values(document_id)");
		sql.append(",thing_id=values(thing_id);");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {

			ps.setString(1, status);
			ps.setString(2, doc.toString());
			ps.setString(3, thing.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected Event<DocumentThing> getEvent() {
		return dtEvent;
	}

	public void dirtyRemoveThing(Thing toDel) {
		if (toDel != null && toDel.getId() != null) {
			StringBuilder sql = new StringBuilder("DELETE FROM documentthing ");

			sql.append(" WHERE thing_id = ?");

			try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
				ps.setString(1, toDel.getId().toString());
				ps.executeUpdate();
				ps.close();
				closeConnection(con);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
