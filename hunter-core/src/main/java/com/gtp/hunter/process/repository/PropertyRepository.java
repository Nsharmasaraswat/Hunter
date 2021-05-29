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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import com.gtp.hunter.common.util.DBUtil;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.Property;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class PropertyRepository extends JPABaseRepository<Property, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Property>	prEvent;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<Property> getEvent() {
		return prEvent;
	}

	public PropertyRepository() {
		super(Property.class, UUID.class);
	}

	public void quickInsert(UUID thing_id, UUID propmodelfield_id, String value) {
		StringBuilder sql = new StringBuilder("INSERT INTO `property`");

		sql.append(" (`id`, `createdAt`, `updatedAt`, `status`, `thing_id`, `propertymodelfield_id`, `value`)");
		sql.append(" VALUES ");
		sql.append("(uuid(),now(),now(),'NOVO',?,?,?)");
		sql.append(" ON DUPLICATE KEY UPDATE");
		sql.append(" `metaname` = values(`metaname`)");
		sql.append(",`updatedAt` = values(`updatedAt`)");
		sql.append(",`value` = values(`value`);");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY);) {

			ps.setString(1, thing_id.toString());
			ps.setString(2, propmodelfield_id.toString());
			ps.setString(3, value);
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Property> quickListByThingId(UUID thingId) {
		List<Property> ret = new ArrayList<Property>();

		if (thingId != null) {
			try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM property WHERE thing_id = ?");) {
				ps.setString(1, thingId.toString());
				try (ResultSet rs = ps.executeQuery();) {
					ret = (List<Property>) DBUtil.resultSetToList(rs, Property.class);
					rs.close();
				}
				ps.close();
				closeConnection(con);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public void removeByThingId(UUID id) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<Property> cd = cb.createCriteriaDelete(Property.class);
		Root<Property> r = cd.from(Property.class);

		cd.where(cb.equal(r.get("thing").get("id"), id));
		getEntityManager().createQuery(cd).executeUpdate();
	}
}
