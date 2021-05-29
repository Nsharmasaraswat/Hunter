package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import com.gtp.hunter.common.util.DBUtil;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressModel;
import com.gtp.hunter.process.model.Location;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class AddressRepository extends JPABaseRepository<Address, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Address>	fEvent;

	public AddressRepository() {
		super(Address.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public List<Address> listByLocation(UUID locId) {
		List<Address> aList = em.createQuery("from Address where location.id = :locId", Address.class)
						.setParameter("locId", locId)
						.getResultList();
		return aList;
	}

	public List<Address> listByModelAndLocation(AddressModel model, Location loc) {
		List<Address> aList = em.createQuery("from Address where location = :loc and model = :mdl", Address.class)
						.setParameter("loc", loc)
						.setParameter("mdl", model)
						.getResultList();
		return aList;
	}

	public List<Address> listOrphanByLocation(UUID locId) {
		List<Address> aList = em.createQuery("from Address where location.id = :locId and parent is null", Address.class)
						.setParameter("locId", locId)
						.getResultList();
		return aList;
	}

	public List<Address> listByModelMetaname(String value) {
		EntityManager em = getEntityManager();
		TypedQuery<Address> q = em.createQuery("from Address where model.metaname = :meta", Address.class);

		return q.setParameter("meta", value).getResultList();
	}

	@Override
	protected Event<Address> getEvent() {
		return fEvent;
	}

	public List<Address> listByLocationNewerThan(UUID locationId, Date updated) {
		EntityManager em = getEntityManager();
		TypedQuery<Address> q = em.createQuery("from Address where location.id = :locid and updatedAt > :updated order by updatedAt", Address.class);

		return q.setParameter("locid", locationId).setParameter("updated", updated, TemporalType.TIMESTAMP).getResultList();
	}

	public Address quickFindParent(UUID id) {
		Address ret = null;

		if (id != null)
			try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT a.* FROM address a JOIN address s ON s.parent_id = a.id WHERE s.id = ?");) {
				ps.setString(1, id.toString());
				try (ResultSet rs = ps.executeQuery();) {
					List<Address> lst = (List<Address>) DBUtil.resultSetToList(rs, Address.class);
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

	public List<Address> quickListByModelMetaname(String metaname) {
		List<Address> ret = new ArrayList<>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT a.* FROM address a INNER JOIN addressmodel am ON a.addressmodel_id = am.id WHERE am.metaname = ?");) {
			ps.setString(1, metaname);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Address>) DBUtil.resultSetToList(rs, Address.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public List<Address> quickListByModelMetanameAndLocationId(String metaname, UUID locId) {
		List<Address> ret = new ArrayList<>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT a.* FROM address a INNER JOIN addressmodel am ON a.addressmodel_id = am.id WHERE am.metaname = ? AND a.location_id = ?");) {
			ps.setString(1, metaname);
			ps.setString(2, locId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Address>) DBUtil.resultSetToList(rs, Address.class);
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void quickUpdateRegionFromWKT(UUID addressId, String wkt) {
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("UPDATE address SET region = ST_GeomFromText(?) WHERE id = ?");) {
			ps.setString(1, wkt);
			ps.setString(2, addressId.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
