package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.Product;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DocumentItemRepository extends JPABaseRepository<DocumentItem, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager		em;

	@Inject
	private Event<DocumentItem>	diEvent;

	public DocumentItemRepository() {
		super(DocumentItem.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@SuppressWarnings("unchecked")
	public List<DocumentItem> getByDocumentId(UUID docId) {
		Query q = em.createQuery("from DocumentItem where document.id = :docid").setParameter("docid", docId);
		return q.getResultList();
	}

	public List<DocumentItem> getQuickDocumentItemListByDocument(UUID docId) {
		//logger.debug("PROCURANDO POR " + docId.toString());
		List<DocumentItem> ret = new ArrayList<DocumentItem>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select " + "	di.id as did, di.status as dstatus, di.measureUnit, di.document_id, di.qty, di.createdAt as dicreatedat, di.updatedAt as diupdatedat, " + " p.id, p.name, p.sku, p.createdAt, p.updatedAt, count(t.id) as dtqtd " + "from documentitem di " + "join product p on di.product_id = p.id " + "left outer join documentthing dt on dt.document_id = di.document_id " + "left outer join thing t on dt.thing_id = t.id and t.product_id = p.id " + "where di.document_id = ? " + "group by p.id");) {

			ps.setString(1, docId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					DocumentItem di = new DocumentItem();
					Product p = new Product();
					di.setId(UUID.fromString(rs.getString("did")));
					di.setQty(rs.getInt("qty"));
					di.setStatus(rs.getString("dstatus"));
					di.setQtdThings(rs.getInt("dtqtd"));
					di.setMeasureUnit(rs.getString("measureUnit"));
					di.setCreatedAt(rs.getTimestamp("dicreatedat"));
					di.setUpdatedAt(rs.getTimestamp("diupdatedat"));
					p.setId(UUID.fromString(rs.getString("id")));
					p.setName(rs.getString("name"));
					p.setSku(rs.getString("sku"));
					p.setCreatedAt(rs.getTimestamp("createdAt"));
					p.setUpdatedAt(rs.getTimestamp("updatedAt"));
					di.setProduct(p);
					ret.add(di);
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

	public List<DocumentItem> getQuickDocumentItemListByDocumentThingStatus(UUID docId, String thingStatus, String thingStatus2) {
		List<DocumentItem> ret = new ArrayList<DocumentItem>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select di.id as did, di.status as dstatus, d.id, di.qty, p.id, p.name, p.sku, count(t.id) as dtqtd " + "from document d " + "join documentitem di on di.document_id = d.id " + "join product p on di.product_id = p.id " + "left join (SELECT * FROM documentthing WHERE status <> 'CANCELADO') dt on dt.document_id = d.id " + "left join thing t on dt.thing_id = t.id and t.product_id = p.id " + "where d.id = ? " + "group by p.sku order by p.sku");) {

			//ps.setString(1, thingStatus);
			//ps.setString(2, thingStatus2 == null ? thingStatus : thingStatus2);
			ps.setString(1, docId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					DocumentItem di = new DocumentItem();
					Product p = new Product();
					di.setId(UUID.fromString(rs.getString("did")));
					di.setQty(rs.getInt("qty"));
					di.setStatus(rs.getString("dstatus"));
					di.setQtdThings(rs.getInt("dtqtd"));
					p.setId(UUID.fromString(rs.getString("id")));
					p.setName(rs.getString("name"));
					p.setSku(rs.getString("sku"));
					di.setProduct(p);
					ret.add(di);
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

	public List<DocumentItem> getQuickDocumentItemByDocumentAndProductSKU(UUID docId, String sku) {
		List<DocumentItem> ret = new ArrayList<DocumentItem>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select di.id as did, di.status as dstatus, di.document_id, di.qty, p.* from documentitem di join product p on di.product_id = p.id where document_id = ?");) {

			ps.setString(1, docId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					DocumentItem di = new DocumentItem();
					Product p = new Product();
					di.setId(UUID.fromString(rs.getString("did")));
					di.setQty(rs.getInt("qty"));
					di.setStatus(rs.getString("dstatus"));
					p.setId(UUID.fromString(rs.getString("id")));
					p.setName(rs.getString("name"));
					p.setSku(rs.getString("sku"));
					di.setProduct(p);
					ret.add(di);
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

	public DocumentItem getQuickDocumentItemByDocumentAndProductSKUAndBatch(UUID docId, String sku, String batch) {
		DocumentItem ret = null;
		List<DocumentItem> lstret = new ArrayList<DocumentItem>();
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select di.id as did, di.status as dstatus, di.document_id, di.qty, p.* from documentitem di join documentitemproperty dip on dip.documentitem_id = di.id join product p on di.product_id = p.id where di.document_id = ? and p.sku = ? and dip.properties_KEY = 'BATCH' and dip.value = ?");) {
			ps.setString(1, docId.toString());
			ps.setString(2, sku);
			ps.setString(3, batch);
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					DocumentItem di = new DocumentItem();
					Product p = new Product();
					di.setId(UUID.fromString(rs.getString("did")));
					di.setQty(rs.getInt("qty"));
					di.setStatus(rs.getString("dstatus"));
					p.setId(UUID.fromString(rs.getString("id")));
					p.setName(rs.getString("name"));
					p.setSku(rs.getString("sku"));
					di.setProduct(p);
					lstret.add(di);
				}
				if (lstret.size() > 0) ret = lstret.get(0);
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public void updateDocumentItemQuantity(UUID di, int quantity) {

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("update documentitem set qty = ? where id = ?");) {

			ps.setInt(1, quantity);
			ps.setString(2, di.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void dirtyInsert(UUID id, String metaname, String name, String status, Date createdAt, Date updatedAt, String measureUnit, Double qty, UUID document_id, UUID product_id) {
		StringBuilder sql = new StringBuilder("INSERT INTO documentitem");

		sql.append(" (id, metaname, name, status, createdAt, updatedAt, measureUnit, qty, document_id, product_id)");
		sql.append(" VALUES ");
		sql.append("(?,?,?,?,?,?,?,?,?,?)");
		sql.append(" ON DUPLICATE KEY UPDATE");
		sql.append(" metaname = values(metaname)");
		sql.append(",name=values(name)");
		sql.append(",status=values(status)");
		sql.append(",createdAt=values(createdAt)");
		sql.append(",updatedAt=values(updatedAt)");
		sql.append(",measureUnit=values(measureUnit)");
		sql.append(",qty=values(qty)");
		sql.append(",document_id=values(document_id)");
		sql.append(",product_id=values(product_id);");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, id.toString());
			ps.setString(2, metaname);
			ps.setString(3, name);
			ps.setString(4, status);
			if (createdAt != null) {
				ps.setTimestamp(5, new java.sql.Timestamp(createdAt.getTime()));
			} else {
				ps.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
			}
			if (updatedAt != null) {
				ps.setTimestamp(6, new java.sql.Timestamp(updatedAt.getTime()));
			} else {
				ps.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));
			}
			ps.setString(7, measureUnit);
			ps.setDouble(8, qty);
			if (document_id != null) {
				ps.setString(9, document_id.toString());
			} else {
				ps.setNull(9, Types.VARCHAR);
			}
			if (product_id != null) {
				ps.setString(10, product_id.toString());
			} else {
				ps.setNull(10, Types.VARCHAR);
			}

			ps.executeUpdate();
			ps.close();
			closeConnection(con);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void dirtyInsertProperty(UUID id, String key, String value) {
		StringBuilder sql = new StringBuilder("INSERT INTO documentitemproperty");

		sql.append(" (documentitem_id,properties_KEY,value)");
		sql.append(" VALUES");
		sql.append(" (?,?,?)");
		sql.append(" ON DUPLICATE KEY UPDATE");
		sql.append(" value = ?;");
		try (Connection con = initConnection();
						PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, id.toString());
			ps.setString(2, key);
			if (value != null) {
				ps.setString(3, value);
				ps.setString(4, value);
			} else {
				ps.setNull(3, Types.VARCHAR);
				ps.setNull(4, Types.VARCHAR);
			}

			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Event<DocumentItem> getEvent() {
		return diEvent;
	}

	public void quickRemoveByIds(Collection<UUID> idList) {
		if (idList != null && idList.size() > 0) {
			try (Connection con = initConnection();) {
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM documentitemproperty WHERE documentitem_id IN ('" + idList.stream().map(id -> id.toString()).collect(Collectors.joining("','")) + "')");) {
					ps.executeUpdate();
					ps.close();
				}
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM documentitem WHERE id IN ('" + idList.stream().map(id -> id.toString()).collect(Collectors.joining("','")) + "')");) {
					ps.executeUpdate();
					ps.close();
				}
				closeConnection(con);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
