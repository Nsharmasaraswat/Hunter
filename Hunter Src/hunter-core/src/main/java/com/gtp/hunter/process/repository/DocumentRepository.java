package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
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
import javax.persistence.NoResultException;

import org.hibernate.MultiIdentifierLoadAccess;
import org.hibernate.Session;

import com.gtp.hunter.common.util.DBUtil;
import com.gtp.hunter.core.annotation.qualifier.InsertQualifier;
import com.gtp.hunter.core.annotation.qualifier.SuccessQualifier;
import com.gtp.hunter.core.annotation.qualifier.UpdateQualifier;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.Person;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DocumentRepository extends JPABaseRepository<Document, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Document>	documentEvent;

	public DocumentRepository() {
		super(Document.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public Document findByModelAndCode(DocumentModel dm, String code) {
		Document ret = null;

		List<Document> lstRet = em.createQuery("from Document d where d.model = :model and code = :code", Document.class).setParameter("model", dm).setParameter("code", code).getResultList();

		if (lstRet.size() > 0) ret = lstRet.get(0);

		return ret;
	}

	public Document findByModelAndCodeAndPersonCode(DocumentModel dm, String code, String personCode) {
		Document ret = null;

		EntityManager em = getEntityManager();

		try {
			ret = em.createQuery("from Document d where d.model = :model and d.code = :code and d.person.code = :pscode", Document.class)
							.setParameter("model", dm)
							.setParameter("code", code)
							.setParameter("pscode", personCode)
							.setMaxResults(1)
							.setFirstResult(0)
							.getSingleResult();
		} catch (NoResultException nre) {
			//return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public Document findLastByTypeAndStatus(DocumentModel dm, String status) {
		try {
			return em.createQuery("from Document d where d.model = :model and status = :status order by d.createdAt desc", Document.class).setParameter("model", dm).setParameter("status", status).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<Document> getDocumentsByModelAndStatus(DocumentModel model, String status) {
		return em.createQuery("from Document d where d.model = :model and status = :status", Document.class)
						.setParameter("model", model)
						.setParameter("status", status)
						.getResultList();
	}

	public List<Document> getDocumentsByModelAndStatusIn(DocumentModel model, String[] status) {
		return em.createQuery("from Document d where d.model = :model and status in :status", Document.class)
						.setParameter("model", model)
						.setParameter("status", status)
						.getResultList();
	}

	public List<Document> listByModelAndThingStatus(DocumentModel model, String status) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select * from document where id in (select distinct d.id \n" + "from document d \n" + "join documentmodel dm on d.documentmodel_id = dm.id \n" + "join documentthing dt on dt.document_id = d.id \n" + "join thing t on dt.thing_id = t.id \n" + "where dm.metaname = ? and t.status = ?)");) {
			ps.setString(1, model.getMetaname());
			ps.setString(2, status);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
		// return em.createQuery("from Document d join fetch d.things dt join fetch
		// dt.thing t where d.model = :model and t.status = :status",
		// Document.class).setParameter("model", model).setParameter("status",
		// status).getResultList();
	}

	public List<Document> getDocumentsByParentIdAndThingStatus(UUID parent, String status) {
		return em.createQuery("from Document d join fetch d.parent p join fetch d.things dt join fetch dt.thing t where p.id = :parent and t.status = :status", Document.class).setParameter("parent", parent).setParameter("status", status).getResultList();
	}

	public List<Document> getDocumentsByModelMetaname(String model) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d join documentmodel dm on d.documentmodel_id = dm.id where dm.metaname = ? and d.status <> 'CANCELADO'");) {

			ps.setString(1, model.toString());
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> getDocumentsByModelAndItemThingDifference(DocumentModel model) {

		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select distinct d.* from document d where d.id in " + "(select id from " + "(select d.id, p.sku, di.qty, count(dt.id) as qtd " + "from documentitem di " + "join product p on di.product_id = p.id " + "join document d on di.document_id = d.id " + "left join documentthing dt on dt.document_id = d.id " + "left join thing t on dt.thing_id = t.id and t.product_id = p.id and t.status <> 'CANCELADO' " + "where d.documentmodel_id = ? " + "group by d.id, p.id " + "having di.qty <> qtd)" + " g) and d.status <> 'CANCELADO'");) {
			ps.setString(1, model.getId().toString());
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@SuppressWarnings("unchecked")
	public List<Document> getDocumentsByModelAndStatusAndItemThingDifference(DocumentModel model, String docStatus) {
		// @formatter:off
		return em.createNativeQuery("select distinct d.* from document d where d.id in " + "(select id from " + "(select d.id, p.sku, di.qty, count(dt.id) as qtd " + "from documentitem di " + "join product p on di.product_id = p.id " + "join document d on di.document_id = d.id " + "left join documentthing dt on dt.document_id = d.id " + "left join thing t on dt.thing_id = t.id and t.product_id = p.id and t.status <> :cantstatus " + "where d.documentmodel_id = :model " + "group by d.id, p.id " + "having di.qty <> qtd)" + " g) and d.status = :dstatus", Document.class).setParameter("cantstatus", "CANCELADO").setParameter("model", model.getId().toString()).setParameter("dstatus", docStatus).getResultList();
		// @formatter:on
	}

	public List<Document> getDocumentsByStatus(String status) {
		return em.createQuery("from Document d where status = :status", Document.class)
						.setParameter("status", status)
						.getResultList();
	}

	public List<Document> listByThingStatus(String status) {
		return em.createQuery("from Document d join fetch d.things dt join fetch dt.thing t where d.status <> :canstatus and t.status = :status", Document.class)
						.setParameter("canstatus", "CANCELADO")
						.setParameter("status", status)
						.getResultList();
	}

	public Document quickFindById(UUID id) {
		Document ret = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select * from document where id = ?");) {

			ps.setString(1, id.toString());
			try (ResultSet rs = ps.executeQuery();) {
				List<Document> lst = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				if (lst.size() > 0) ret = lst.get(0);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Document quickFindByMetaname(String metaname) {
		Document ret = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select * from document where metaname = ?");) {

			ps.setString(1, metaname);
			try (ResultSet rs = ps.executeQuery();) {
				List<Document> lst = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				if (lst.size() > 0) ret = lst.get(0);
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Document quickFindByCodeAndModelMetaname(String code, String modelMetaname) {
		Document ret = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT d.* FROM document d INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id WHERE d.code = ? AND dm.metaname = ? and d.status <> 'CANCELADO' ORDER BY createdAt DESC");) {
			ps.setString(1, code);
			ps.setString(2, modelMetaname);
			try (ResultSet rs = ps.executeQuery();) {
				List<Document> lst = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				if (lst.size() > 0) ret = lst.get(0);
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public UUID quickFindIDByCodeAndModelMetanameAndPerson(String code, String modelMetaname, String person) {
		UUID ret = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT d.* FROM document d INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id WHERE d.code = ? AND dm.metaname = ? and d.status <> 'CANCELADO' and d.person_id = ?");) {
			ps.setString(1, code);
			ps.setString(2, modelMetaname);
			ps.setString(3, person);
			try (ResultSet rs = ps.executeQuery();) {
				//List<Document> lst = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				while (rs.next()) {
					ret = UUID.fromString(rs.getString("id"));
				}
				//if (lst.size() > 0)
				//ret = lst.get(0);
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Document findLastByTypeAndStatusAndFieldValue(DocumentModel model, String status, String fieldMeta, String fieldValue) {
		try {
			return em.createQuery("from Document d join fetch d.model mdl join fetch d.fields df where mdl = :model and d.status = :status and df.field.metaname = :fieldmeta and df.value = :fieldvalue order by d.createdAt desc", Document.class)
							.setParameter("model", model)
							.setParameter("status", status)
							.setParameter("fieldmeta", fieldMeta)
							.setParameter("fieldvalue", fieldValue)
							.setMaxResults(1)
							.setFirstResult(0)
							.getSingleResult();
		} catch (NoResultException nre) {
			//return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Document quickFindParentDoc(String id) {
		Document ret = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d join document p on p.parent_id = d.id where p.id = ?");) {
			ps.setString(1, id);
			try (ResultSet rs = ps.executeQuery();) {
				List<Document> lst = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				if (lst.size() > 0) ret = lst.get(0);
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> getQuickListByType(String type) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d join documentmodel dm on d.documentmodel_id = dm.id where dm.metaname = ? and d.status <> 'CANCELADO'");) {
			ps.setString(1, type);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public List<Document> getQuickListOrphanedByType(String type) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d join documentmodel dm on d.documentmodel_id = dm.id where dm.metaname = ? AND d.parent_id IS NULL and d.status <> 'CANCELADO'");) {
			ps.setString(1, type);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> getQuickListByParentId(String parentId) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d where d.parent_id = ?");) {

			ps.setString(1, parentId);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> quickListByTypeStatus(String type, String status) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d join documentmodel dm on d.documentmodel_id = dm.id where dm.metaname = ? AND d.status = ? ORDER BY d.createdAt");) {
			ps.setString(1, type);
			ps.setString(2, status);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> quickListByTypeStatusFieldValue(String type, String status, String fieldMeta, String fieldValue) {
		List<Document> ret = new ArrayList<Document>();
		StringBuilder query = new StringBuilder("SELECT d.*");

		query.append(" FROM document d ");
		query.append(" INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id ");
		query.append(" INNER JOIN documentmodelfield dmf ON dmf.documentmodel_id = dm.id ");
		query.append(" INNER JOIN documentfield df ON df.document_id = d.id AND df.documentmodelfield_id = dmf.id ");
		query.append(" WHERE dm.metaname = ? ");
		query.append(" AND d.status = ? ");
		query.append(" AND dmf.metaname = ? ");
		query.append(" AND df.value = ? ");
		query.append(" ORDER BY d.createdAt ");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(query.toString());) {
			ps.setString(1, type);
			ps.setString(2, status);
			ps.setString(3, fieldMeta);
			ps.setString(4, fieldValue);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> quickListByTypeStatusListFieldValue(String type, List<String> statusList, String fieldMeta, String fieldValue) {
		List<Document> ret = new ArrayList<Document>();
		StringBuilder query = new StringBuilder("SELECT d.*");

		query.append(" FROM document d ");
		query.append(" INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id ");
		query.append(" INNER JOIN documentmodelfield dmf ON dmf.documentmodel_id = dm.id ");
		query.append(" INNER JOIN documentfield df ON df.document_id = d.id AND df.documentmodelfield_id = dmf.id ");
		query.append(" WHERE dm.metaname = ? ");
		query.append(" AND d.status IN (${stList}) ");
		query.append(" AND dmf.metaname = ? ");
		query.append(" AND df.value = ? ");
		query.append(" ORDER BY d.createdAt ");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(query.toString().replace("${stList}", "'" + statusList.parallelStream().collect(Collectors.joining("','")) + "'"));) {
			ps.setString(1, type);
			ps.setString(2, fieldMeta);
			ps.setString(3, fieldValue);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> quickListByTypeStatusParentFieldValue(String type, String status, String fieldMeta, String fieldValue) {
		List<Document> ret = new ArrayList<Document>();
		StringBuilder query = new StringBuilder("SELECT d.*");

		query.append(" FROM document d ");
		query.append(" INNER JOIN document dp ON d.parent_id = dp.id");
		query.append(" INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id ");
		query.append(" INNER JOIN documentmodel dmp ON dp.documentmodel_id = dmp.id");
		query.append(" INNER JOIN documentmodelfield dmf ON dmf.documentmodel_id = dmp.id ");
		query.append(" INNER JOIN documentfield df ON df.document_id = dp.id AND df.documentmodelfield_id = dmf.id ");
		query.append(" WHERE dm.metaname = ? ");
		query.append(" AND d.status = ? ");
		query.append(" AND dmf.metaname = ? ");
		query.append(" AND df.value = ? ");
		query.append(" ORDER BY d.createdAt ");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(query.toString());) {
			ps.setString(1, type);
			ps.setString(2, status);
			ps.setString(3, fieldMeta);
			ps.setString(4, fieldValue);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> quickListByThingId(UUID thingId) {
		List<Document> ret = new ArrayList<Document>();
		StringBuilder sql = new StringBuilder("SELECT d.* ");

		sql.append(" FROM document d ");
		sql.append(" INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id ");
		sql.append(" INNER JOIN documentthing dt ON dt.document_id = d.id ");
		sql.append(" WHERE dt.thing_id = ? ");
		sql.append(" GROUP BY d.code ");
		sql.append(" ORDER BY d.createdAt");

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, thingId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> quickListByTypeThingNoUnitStatus(String type, String status) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT d.* FROM document d INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id INNER JOIN documentthing dt ON dt.document_id = d.id INNER JOIN thing t ON dt.thing_id = t.id LEFT OUTER JOIN thingunits tu ON tu.thing_id = t.id WHERE dm.metaname = ? AND d.status = ? AND tu.unit_id IS NULL AND t.status != 'CANCELADO' GROUP BY d.code HAVING count(dt.id) > 0 ORDER BY d.createdAt");) {
			ps.setString(1, type);
			ps.setString(2, status);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> quickListByTypeThingNoUnit(String type) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT d.* FROM document d INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id INNER JOIN documentthing dt ON dt.document_id = d.id INNER JOIN thing t ON dt.thing_id = t.id LEFT OUTER JOIN thingunits tu ON tu.thing_id = dt.thing_id WHERE dm.metaname = ? AND tu.unit_id IS NULL AND t.status != 'CANCELADO' GROUP BY d.code HAVING count(dt.id) > 0 ORDER BY d.createdAt");) {
			ps.setString(1, type);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> getQuickChildrenListByTypeStatus(UUID id, String type, String status) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d join documentmodel dm on d.documentmodel_id = dm.id where d.parent_id = ? AND dm.metaname = ? AND d.status = ?");) {
			ps.setString(1, id.toString());
			ps.setString(2, type);
			ps.setString(3, status);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> getQuickChildrenList(UUID id) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d where d.parent_id = ?");) {

			ps.setString(1, id.toString());
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> getQuickChildrenListByType(UUID id, String type) {
		List<Document> ret = new ArrayList<Document>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d join documentmodel dm on d.documentmodel_id = dm.id where d.parent_id = ? AND dm.metaname = ?");) {
			ps.setString(1, id.toString());
			ps.setString(2, type);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Document getQuickDocumentByPropertyValue(String prop, String value) {
		Document ret = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.* from document d join documentfield df on df.document_id = d.id join documentmodelfield dmf on df.documentmodelfield_id = dmf.id where dmf.metaname = ? and df.value = ?");) {
			ps.setString(1, prop);
			ps.setString(2, value);
			try (ResultSet rs = ps.executeQuery();) {
				List<Document> lst = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				if (lst.size() > 0) ret = lst.get(0);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Document> quickListByPropertyValue(String fieldMeta, String value) {
		List<Document> ret = null;
		StringBuilder sql = new StringBuilder("SELECT d.* ");

		sql.append(" FROM document d ");
		sql.append(" INNER JOIN documentfield df ON df.document_id = d.id ");
		sql.append(" INNER JOIN documentmodelfield dmf ON df.documentmodelfield_id = dmf.id ");
		sql.append(" WHERE dmf.metaname = ? ");
		sql.append("	AND df.value = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString());) {
			ps.setString(1, fieldMeta);
			ps.setString(2, value);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Document>) DBUtil.resultSetToList(rs, Document.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public void quickUpdateStatus(UUID id, String status) {

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("update document set status = ? where id = ?");) {

			ps.setString(1, status);
			ps.setString(2, id.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
			getEvent().select(new SuccessQualifier()).select(new UpdateQualifier()).fire(findById(id));
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public List<Document> listOrphanByPersonTypeAndCode(String personType, String personCode) {
		return em.createQuery("from Document d WHERE d.person.model.metaname = :personType AND d.person.code = :personCode AND d.parent IS NULL", Document.class).setParameter("personType", personType).setParameter("personCode", personCode).getResultList();
	}

	public String quickGetPersonId(UUID id) {
		String ret = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT person_id FROM document where id = ?");) {
			ps.setString(1, id.toString());
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.first()) {
					return rs.getString("person_id");
				}
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public void dirtyInsert(UUID id, String metaname, String name, String status, Date createdAt, Date updatedAt, String code, UUID documentmodel_id, UUID parent_id, UUID user_id, UUID person_id, boolean fireEvent) {
		StringBuilder sql = new StringBuilder("INSERT INTO document ");

		sql.append("(id,metaname,name,status,createdAt,updatedAt,code,documentmodel_id,parent_id,user_id,person_id) ");
		sql.append("VALUES ");
		sql.append("(?,?,?,?,?,?,?,?,?,?,?)");
		sql.append("ON DUPLICATE KEY UPDATE `metaname` = ?,`name` = ?,`status` = ?,`createdAt` = ?,`updatedAt` = ?,");
		sql.append("`code`= ?, `documentmodel_id` = ?, `parent_id` = ?, `user_id` = ?, `person_id` = ?; ");
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
			ps.setString(7, code);
			ps.setString(17, code);
			ps.setString(8, documentmodel_id.toString());
			ps.setString(18, documentmodel_id.toString());
			if (parent_id != null) {
				ps.setString(9, parent_id.toString());
				ps.setString(19, parent_id.toString());
			} else {
				ps.setNull(9, Types.VARCHAR);
				ps.setNull(19, Types.VARCHAR);
			}
			if (user_id != null) {
				ps.setString(10, user_id.toString());
				ps.setString(20, user_id.toString());
			} else {
				ps.setNull(10, Types.VARCHAR);
				ps.setNull(20, Types.VARCHAR);
			}
			if (person_id != null) {
				ps.setString(11, person_id.toString());
				ps.setString(21, person_id.toString());
			} else {
				ps.setNull(11, Types.VARCHAR);
				ps.setNull(21, Types.VARCHAR);
			}
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
			if (fireEvent) {
				Document d = findById(id);

				if (d != null)
					getEvent().select(new SuccessQualifier()).select(new InsertQualifier()).fire(d);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void quickUpdateParentId(UUID id, UUID parent_id) {

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("update document set parent_id = ? where id = ?");) {

			ps.setString(1, parent_id.toString());
			ps.setString(2, id.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
			getEvent().select(new SuccessQualifier()).select(new UpdateQualifier()).fire(findById(id));
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public List<UUID> listIdsByModelMetanameAndTagIDAndNotStatus(String metaname, String tagid, String status) {

		List<UUID> ret = new ArrayList<UUID>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("select d.id from document d join documentmodel dm on d.documentmodel_id = dm.id join documentthing dt on dt.document_id = d.id join thingunits tu on dt.thing_id = tu.thing_id join unit u on tu.unit_id = u.id where u.tagid = ? and dm.metaname = ? and d.status <> ? AND d.status <> 'CANCELADO'");) {
			ps.setString(1, tagid);
			ps.setString(2, metaname);
			ps.setString(3, status);
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					ret.add(UUID.fromString(rs.getString("id")));
				}
			}

			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	protected Event<Document> getEvent() {
		return documentEvent;
	}

	public List<Document> listByModelMeta(String type) {
		return em.createQuery("from Document d join fetch d.model mdl where mdl.metaname = :modelmeta", Document.class)
						.setParameter("modelmeta", type)
						.getResultList();
	}

	public List<Document> listByTypeAndStatus(String type, String status) {
		return em.createQuery("from Document d join fetch d.model mdl where mdl.metaname = :modelmeta and d.status = :status", Document.class)
						.setParameter("modelmeta", type)
						.setParameter("status", status)
						.getResultList();
	}

	public Document findByTypeAndCode(String type, String code) {
		Document ret = null;

		List<Document> lstRet = em.createQuery("from Document d join fetch d.model mdl where mdl.metaname = :modelmeta and d.code = :code", Document.class)
						.setParameter("modelmeta", type)
						.setParameter("code", code)
						.getResultList();

		if (lstRet.size() > 0) ret = lstRet.get(0);

		return ret;
	}

	public List<Document> customIncompleteQuickListOrphanByPersonTypeAndCode(String personType, String personCode) {
		StringBuilder sb = new StringBuilder("SELECT d.*, dm.*, ps.*");
		List<Document> ret = new ArrayList<>();

		sb.append(" FROM document d");
		sb.append(" INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id");
		sb.append(" INNER JOIN person ps ON d.person_id = ps.id");
		sb.append(" INNER JOIN personmodel psm ON ps.personmodel_id = psm.id");
		sb.append(" WHERE psm.metaname = ?");
		sb.append(" AND ps.code = ?");
		sb.append(" AND d.parent_id IS NULL");
		sb.append(" AND d.status != 'CANCELADO' AND d.status != 'CONCLUIDO';");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sb.toString());) {
			ps.setString(1, personType);
			ps.setString(2, personCode);
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					Document d = new Document();
					DocumentModel dm = new DocumentModel();
					Person prs = new Person();

					prs.setId(UUID.fromString(rs.getString("ps.id")));
					dm.setId(UUID.fromString(rs.getString("dm.id")));
					dm.setMetaname(rs.getString("dm.metaname"));
					dm.setName(rs.getString("dm.name"));
					dm.setStatus(rs.getString("dm.status"));
					d.setId(UUID.fromString(rs.getString("d.id")));
					d.setModel(dm);
					d.setCode(rs.getString("d.code"));
					d.setStatus(rs.getString("d.status"));
					d.setName(rs.getString("d.name"));
					d.setPerson(prs);
					ret.add(d);
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

	public List<Document> listByMultipleIds(List<UUID> idList) {
		Session session = em.unwrap(Session.class);
		MultiIdentifierLoadAccess<Document> multiLoadAccess = session.byMultipleIds(Document.class);

		return multiLoadAccess.enableSessionCheck(true).multiLoad(idList);
	}

	public List<Document> listByTransportThingId(UUID thingId) {

		return em.createQuery("select d from Document d join DocumentTransport dtr ON dtr.document = d WHERE dtr.thing.id = :thId", Document.class)
						.setParameter("thId", thingId)
						.getResultList();
	}

	public List<Document> listByTypeFrom(String model, Date created) {
		return em.createQuery("from Document where model.metaname = :model and createdAt >= :created", Document.class)
						.setParameter("model", model)
						.setParameter("created", created)
						.getResultList();
	}
}
