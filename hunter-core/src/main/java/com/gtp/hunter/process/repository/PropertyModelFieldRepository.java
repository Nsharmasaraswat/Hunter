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
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class PropertyModelFieldRepository extends JPABaseRepository<PropertyModelField, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager				em;

	@Inject
	private Event<PropertyModelField>	pmfEvent;

	public PropertyModelFieldRepository() {
		super(PropertyModelField.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@SuppressWarnings("unchecked")
	public List<PropertyModelField> listByModel(UUID propModId) {
		Query q = em.createQuery("from PropertyModelField where model.id = :propmodid").setParameter("propmodid", propModId);
		return q.getResultList();
	}

	public List<PropertyModelField> listQuickPropertyModelFieldFromProduct(UUID prd) {
		List<PropertyModelField> ret = new ArrayList<PropertyModelField>();
		Connection con = initConnection();
		ResultSet rs;
		try {
			PreparedStatement ps = con.prepareStatement("select ppmf.* from product p join productmodel pm on p.productmodel_id = pm.id join propertymodel ppm on pm.propertymodel_id = ppm.id join propertymodelfield ppmf on ppmf.propertymodel_id = ppm.id where p.id = ?");
			ps.setString(1, prd.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				PropertyModelField pmf = new PropertyModelField();
				pmf.setId(UUID.fromString(rs.getString("id")));
				pmf.setCreatedAt(rs.getDate("createdAt"));
				pmf.setMetaname(rs.getString("metaname"));
				pmf.setName(rs.getString("name"));
				pmf.setStatus(rs.getString("status"));
				pmf.setType(FieldType.valueOf(rs.getString("type")));
				pmf.setUpdatedAt(rs.getDate("updatedAt"));
				pmf.setVisible(rs.getBoolean("visible"));
				pmf.setOrdem(rs.getInt("ordem"));
				ret.add(pmf);
			}
			rs.close();
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	protected Event<PropertyModelField> getEvent() {
		return pmfEvent;
	}

	public PropertyModelField findByModelAndMetaname(PropertyModel pm, String metaname) {
		try {
			return em.createQuery("from PropertyModelField where model = :propmod and metaname = :metaname", PropertyModelField.class)
							.setParameter("propmod", pm)
							.setParameter("metaname", metaname)
							.setMaxResults(1)
							.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
