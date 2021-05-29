package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import javax.persistence.Query;

import com.gtp.hunter.common.enums.FieldType;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModelField;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DocumentFieldRepository extends JPABaseRepository<DocumentField, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager			em;

	@Inject
	private Event<DocumentField>	dfEvent;

	public DocumentFieldRepository() {
		super(DocumentField.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@SuppressWarnings("unchecked")
	public List<DocumentField> listByDocumentId(UUID docId) {
		Query q = em.createQuery("from DocumentField where document.id = :docid").setParameter("docid", docId);
		return q.getResultList();
	}

	public List<DocumentField> quickListByDocumentId(UUID docId) {
		StringBuilder sb = new StringBuilder("SELECT df.*, dmf.*");
		List<DocumentField> ret = new ArrayList<>();

		sb.append(" FROM documentfield df");
		sb.append(" INNER JOIN documentmodelfield dmf ON df.documentmodelfield_id = dmf.id");
		sb.append(" WHERE document_id = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sb.toString());) {
			ps.setString(1, docId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					DocumentModelField dmf = new DocumentModelField();
					DocumentField df = new DocumentField();

					dmf.setId(UUID.fromString(rs.getString("dmf.id")));
					dmf.setName(rs.getString("dmf.name"));
					dmf.setMetaname(rs.getString("dmf.metaname"));
					dmf.setCreatedAt(rs.getTimestamp("dmf.createdAt"));
					dmf.setUpdatedAt(rs.getTimestamp("dmf.updatedAt"));
					dmf.setType(FieldType.valueOf(rs.getString("dmf.type")));
					dmf.setOrdem(rs.getInt("dmf.ordem"));
					dmf.setVisible(rs.getBoolean("dmf.visible"));
					df.setId(UUID.fromString(rs.getString("df.id")));
					df.setStatus(rs.getString("df.status"));
					df.setCreatedAt(rs.getTimestamp("df.createdAt"));
					df.setUpdatedAt(rs.getTimestamp("df.updatedAt"));
					df.setValue(rs.getString("df.value"));
					df.setField(dmf);
					ret.add(df);
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

	@Deprecated
	public void quickRemoveDocumentField(UUID docmodel, String value) {

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("delete from documentfield where documentmodelfield_id = ? and value = ?");) {

			ps.setString(1, docmodel.toString());
			ps.setString(2, value);
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void quickInsertDocumentField(UUID document, UUID docmodel, String value) {
		StringBuilder sql = new StringBuilder("INSERT INTO documentfield");

		sql.append(" (id, createdAt, updatedAt, status, document_id, documentmodelfield_id, value)");
		sql.append(" VALUES ");
		sql.append("(uuid(),now(),now(),'NOVO',?,?,?)");
		sql.append(" ON DUPLICATE KEY UPDATE");
		sql.append(" metaname = values(metaname)");
		sql.append(",name=values(name)");
		sql.append(",status=values(status)");
		sql.append(",createdAt=values(createdAt)");
		sql.append(",updatedAt=values(updatedAt)");
		sql.append(",document_id=values(document_id)");
		sql.append(",documentmodelfield_id=values(documentmodelfield_id)");
		sql.append(",value=values(value);");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {

			ps.setString(1, document.toString());
			ps.setString(2, docmodel.toString());
			ps.setString(3, value);
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void quickChangeModel(UUID document, UUID docmodelFrom, UUID docmodelTo) {
		StringBuilder query = new StringBuilder("UPDATE documentfield");

		query.append(" SET documentmodelfield_id = ?");
		query.append(" WHERE document_id = ?");
		query.append(" AND documentmodelfield_id = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(query.toString());) {

			ps.setString(1, docmodelTo.toString());
			ps.setString(2, document.toString());
			ps.setString(3, docmodelFrom.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Event<DocumentField> getEvent() {
		return dfEvent;
	}

	public void quickInsert(UUID document, UUID docmodelfield, String value) {
		StringBuilder sql = new StringBuilder("INSERT INTO documentfield");

		sql.append(" (id, createdAt, updatedAt, status, document_id, documentmodelfield_id, value)");
		sql.append(" VALUES ");
		sql.append("(uuid(),now(),now(),'NOVO',?,?,?)");
		sql.append(" ON DUPLICATE KEY UPDATE");
		sql.append(" metaname = values(metaname)");
		sql.append(",createdAt=values(createdAt)");
		sql.append(",updatedAt=values(updatedAt)");
		sql.append(",value=values(value);");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY);) {

			ps.setString(1, document.toString());
			ps.setString(2, docmodelfield.toString());
			ps.setString(3, value);
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<DocumentField> listByModelMetaValue(String field, String value) {
		EntityManager em = getEntityManager();

		try {
			return em.createQuery("from DocumentField where field.metaname = :fld and value = :val", DocumentField.class)
							.setParameter("fld", field)
							.setParameter("val", value)
							.getResultList();
		} catch (NoResultException nre) {
			//return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

}
