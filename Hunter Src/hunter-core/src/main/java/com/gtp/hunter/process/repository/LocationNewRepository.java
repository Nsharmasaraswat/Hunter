package com.gtp.hunter.process.repository;

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
import javax.naming.NamingException;

import com.gtp.hunter.common.enums.FieldType;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.model.AddressModel;
import com.gtp.hunter.process.model.AddressModelField;
import com.gtp.hunter.process.model.Location;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class LocationNewRepository extends GenericRepository<Location, UUID> {

	public LocationNewRepository() throws NamingException {
		super(LocationNewRepository.class);
		this.SELECT_HEADER.append("SELECT l.id AS lId, l.metaname AS lMetaname, l.name AS lName, l.status AS lStatus, l.createdAt AS lCreatedAt, l.updatedAt AS lUpdatedAt, l.mapfile AS lMapFile, l.parent_id AS lParentId, l.crs AS lCRS, l.rotation AS lRotation, ST_AsText(l.center) AS lCenter");
		this.SELECT_HEADER.append(", a.id AS aId, a.metaname AS aMetaname, a.name AS aName, a.status AS aStatus, a.createdAt AS aCreatedAt, a.updatedAt AS aUpdatedAt, a.location_id AS aLocationId, a.parent_id AS aParentId, ST_ASTEXT(a.region) AS aRegion, a.addressmodel_id AS aAddressModelId");
		this.SELECT_HEADER.append(", am.id AS amId, am.metaname AS amMetaname, am.name AS amName, am.status AS amStatus, am.createdAt AS amCreatedAt, am.updatedAt AS amUpdatedAt, am.classe AS amClasse, am.parent_id AS amParentId");
		this.SELECT_HEADER.append(", amf.id AS amfId, amf.metaname AS amfMetaname, amf.name AS amfName, amf.status AS amfStatus, amf.createdAt AS amfCreatedAt, amf.updatedAt AS amfUpdatedAt, amf.type AS amfType, amf.addressmodel_id AS amfAddressModelId");
		this.SELECT_HEADER.append(", af.id AS afId, af.metaname AS afMetaname, af.name AS afName, af.status AS afStatus, af.createdAt AS afCreatedAt, af.updatedAt AS afUpdatedAt, af.value AS afValue, af.address_id AS afAddressId, af.addressmodelfield_id AS afAddressModelFieldId");
		this.SELECT_TABLE.append(" FROM location l ");
		this.SELECT_JOINS.append(" LEFT JOIN address a ON a.location_id = l.id ");
		this.SELECT_JOINS.append(" LEFT JOIN addressmodel am ON a.addressmodel_id = am.id ");
		this.SELECT_JOINS.append(" LEFT JOIN addressmodelfield amf ON amf.addressmodel_id = am.id ");
		this.SELECT_JOINS.append(" LEFT JOIN addressfield af ON af.address_id = a.id ");
	}

	public Location findById(UUID id) {
		return findById(id.toString());
	}

	public Location findById(String id) {
		StringBuilder sql = new StringBuilder(SELECT_HEADER);

		sql.append(SELECT_TABLE);
		sql.append(SELECT_JOINS);
		sql.append(" WHERE l.id = ?");
		return getByStatement(sql.toString(), id);
	}

	public Location findByMetaname(String metaname) {
		StringBuilder sql = new StringBuilder(SELECT_HEADER);

		sql.append(SELECT_TABLE);
		sql.append(SELECT_JOINS);
		sql.append(" WHERE l.metaname = ?");
		return getByStatement(sql.toString(), metaname);
	}

	@Override
	public Location makePersistent(Location l) {
		return null;
	}

	@Override
	protected List<Location> buildObjectList(ResultSet rs) throws SQLException {
		List<Location> ret = new ArrayList<>();

		while (rs.next()) {
			String lId = rs.getString("lId");
			UUID lUuId = UUID.fromString(lId);
			String lMetaname = rs.getString("lMetaname");
			String lName = rs.getString("lName");
			String lStatus = rs.getString("lStatus");
			Date lCreatedAt = rs.getTimestamp("lCreatedAt");
			Date lUpdatedAt = rs.getTimestamp("lUpdatedAt");
			String lMapFile = rs.getString("lMapFile");
			String lCenter = rs.getString("lCenter");
			int lRotation = rs.getInt("lRotation");
			String lCRS = rs.getString("lCRS");
			String lParentId = rs.getString("lParentId");
			Location l = new Location();
			Address a = new Address();
			AddressModel am = new AddressModel();
			AddressModelField amf = new AddressModelField();

			for (int i = 0; i < ret.size(); i++)
				if (ret.get(i).getId().equals(lUuId))
					l = ret.remove(i);

			l.setId(lUuId);
			l.setMetaname(lMetaname);
			l.setName(lName);
			l.setStatus(lStatus);
			l.setCreatedAt(lCreatedAt);
			l.setUpdatedAt(lUpdatedAt);
			l.setMapfile(lMapFile);
			l.setRotation(lRotation);
			l.setWkt(lCenter);
			l.setCrs(lCRS);
			if (!rs.wasNull() && !lParentId.equalsIgnoreCase(lId)) {
				l.setParent(findById(UUID.fromString(lParentId)));
			}

			String amIdString = rs.getString("amId");
			if (!rs.wasNull()) {
				UUID amId = UUID.fromString(amIdString);
				String amMetaname = rs.getString("amMetaname");
				String amName = rs.getString("amName");
				String amStatus = rs.getString("amStatus");
				Date amCreatedAt = rs.getTimestamp("amCreatedAt");
				Date amUpdatedAt = rs.getTimestamp("amUpdatedAt");
				String amClasse = rs.getString("amClasse");
				String amParentIdString = rs.getString("amParentId");

				if (!rs.wasNull()) {
					AddressModel parent = new AddressModel();

					parent.setId(UUID.fromString(amParentIdString));
					am.setParent(parent);
				}

				am.setId(amId);
				am.setMetaname(amMetaname);
				am.setName(amName);
				am.setStatus(amStatus);
				am.setCreatedAt(amCreatedAt);
				am.setUpdatedAt(amUpdatedAt);
				am.setClasse(amClasse);
			}

			String amfIdString = rs.getString("amfId");
			if (!rs.wasNull()) {
				UUID amfId = UUID.fromString(amfIdString);
				String amfMetaname = rs.getString("amfMetaname");
				String amfName = rs.getString("amfName");
				String amfStatus = rs.getString("amfStatus");
				Date amfCreatedAt = rs.getTimestamp("amfCreatedAt");
				Date amfUpdatedAt = rs.getTimestamp("amfUpdatedAt");
				String amfType = rs.getString("amfType");

				amf.setId(amfId);
				amf.setMetaname(amfMetaname);
				amf.setName(amfName);
				amf.setStatus(amfStatus);
				amf.setCreatedAt(amfCreatedAt);
				amf.setUpdatedAt(amfUpdatedAt);
				amf.setType(FieldType.valueOf(amfType));
				amf.setModel(am);

				am.getFields().add(amf);
			}

			String aIdString = rs.getString("aId");
			if (!rs.wasNull()) {
				UUID aId = UUID.fromString(aIdString);
				String aMetaname = rs.getString("aMetaname");
				String aName = rs.getString("aName");
				String aStatus = rs.getString("aStatus");
				Date aCreatedAt = rs.getTimestamp("aCreatedAt");
				Date aUpdatedAt = rs.getTimestamp("aUpdatedAt");
				String aRegion = rs.getString("aRegion");
				String aParentId = rs.getString("aParentId");

				if (!rs.wasNull()) {
					Address aP = new Address();

					aP.setId(UUID.fromString(aParentId));
					a.setParent(aP);
				}
				a.setId(aId);
				a.setMetaname(aMetaname);
				a.setName(aName);
				a.setStatus(aStatus);
				a.setCreatedAt(aCreatedAt);
				a.setUpdatedAt(aUpdatedAt);
				a.setLocation(l);
				a.setModel(am);
				a.setWkt(aRegion);
				if (l.getAddresses().contains(a)) {
					while (l.getAddresses().iterator().hasNext()) {
						Address ad = l.getAddresses().iterator().next();
						if (ad.getId() == aId)
							a = ad;
					}
				}
				l.getAddresses().add(a);
			}

			String afIdString = rs.getString("afId");
			if (!rs.wasNull()) {
				UUID afId = UUID.fromString(afIdString);
				String afMetaname = rs.getString("afMetaname");
				String afName = rs.getString("afName");
				String afStatus = rs.getString("afStatus");
				Date afCreatedAt = rs.getTimestamp("afCreatedAt");
				Date afUpdatedAt = rs.getTimestamp("afUpdatedAt");
				String value = rs.getString("afValue");
				AddressField af = new AddressField();

				af.setId(afId);
				af.setMetaname(afMetaname);
				af.setName(afName);
				af.setStatus(afStatus);
				af.setCreatedAt(afCreatedAt);
				af.setUpdatedAt(afUpdatedAt);
				af.setValue(value);
				af.setModel(amf);
				a.getFields().add(af);
			}

			ret.add(l);
		}

		return ret;
	}

	public List<Location> listChildren(UUID locationId) {
		StringBuilder sql = new StringBuilder(SELECT_HEADER);

		sql.append(SELECT_TABLE);
		sql.append(SELECT_JOINS);
		sql.append(" WHERE l.parent_id = ?");
		return listByStatement(sql.toString(), locationId.toString());
	}
}
