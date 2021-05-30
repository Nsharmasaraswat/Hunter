package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Thing;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DocumentTransportRepository extends JPABaseRepository<DocumentTransport, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager				em;

	@Inject
	private Event<DocumentTransport>	dtrEvent;

	public DocumentTransportRepository() {
		super(DocumentTransport.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<DocumentTransport> getEvent() {
		return dtrEvent;
	}

	public List<DocumentTransport> listByDocumentId(UUID docId) {
		TypedQuery<DocumentTransport> query = em.createQuery("from DocumentTransport where document.id = :docid", DocumentTransport.class)
						.setParameter("docid", docId);
		return query.getResultList();
	}

	public void dirtyInsert(UUID id, String metaname, String name, String status, Date createdAt, Date updatedAt, int seq, UUID document_id, UUID thing_id, UUID address_id, UUID origin_id) {
		StringBuilder sql = new StringBuilder("INSERT INTO documenttransport");

		sql.append("(id,metaname,name,status,createdAt,updatedAt,seq,document_id,thing_id,address_id,origin_id) ");
		sql.append("VALUES ");
		sql.append("(?,?,?,?,?,?,?,?,?,?,?)");
		sql.append("ON DUPLICATE KEY UPDATE `metaname` = ?,`name` = ?,`status` = ?,`createdAt` = ?,`updatedAt` = ?,");
		sql.append("`seq`= ?, `document_id` = ?, `thing_id` = ?, `address_id` = ?, `origin_id` = ?; ");

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, id.toString());
			ps.setString(2, metaname);
			ps.setString(12, metaname);
			ps.setString(3, name);
			ps.setString(13, name);
			ps.setString(4, status);
			ps.setString(14, status);
			if (createdAt != null) {
				ps.setTimestamp(5, new java.sql.Timestamp(createdAt.getTime()));
				ps.setTimestamp(15, new java.sql.Timestamp(createdAt.getTime()));
			} else {
				ps.setTimestamp(5, new java.sql.Timestamp(new Date().getTime()));
				ps.setTimestamp(15, new java.sql.Timestamp(new Date().getTime()));
			}

			if (updatedAt != null) {
				ps.setTimestamp(6, new java.sql.Timestamp(updatedAt.getTime()));
				ps.setTimestamp(16, new java.sql.Timestamp(updatedAt.getTime()));
			} else {
				ps.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));
				ps.setTimestamp(16, new java.sql.Timestamp(new Date().getTime()));
			}

			ps.setInt(7, seq);
			ps.setInt(17, seq);

			if (document_id != null) {
				ps.setString(8, document_id.toString());
				ps.setString(18, document_id.toString());
			} else {
				ps.setNull(8, Types.VARCHAR);
				ps.setNull(18, Types.VARCHAR);
			}

			if (thing_id != null) {
				ps.setString(9, thing_id.toString());
				ps.setString(19, thing_id.toString());
			} else {
				ps.setNull(9, Types.VARCHAR);
				ps.setNull(19, Types.VARCHAR);
			}

			if (address_id != null) {
				ps.setString(10, address_id.toString());
				ps.setString(20, address_id.toString());
			} else {
				ps.setNull(10, Types.VARCHAR);
				ps.setNull(20, Types.VARCHAR);
			}

			if (origin_id != null) {
				ps.setString(11, origin_id.toString());
				ps.setString(21, origin_id.toString());
			} else {
				ps.setNull(11, Types.VARCHAR);
				ps.setNull(21, Types.VARCHAR);
			}

			ps.executeUpdate();
			ps.close();
			closeConnection(con);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void dirtyRemoveThing(Thing toDel) {
		if (toDel != null && toDel.getId() != null) {
			StringBuilder sql = new StringBuilder("DELETE FROM documenttransport ");

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