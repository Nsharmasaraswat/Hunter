package com.gtp.hunter.core.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.NamingException;

import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.process.repository.GenericRepository;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class PortNewRepository extends GenericRepository<Port, UUID> {

	public PortNewRepository() throws NamingException {
		super(PortNewRepository.class);
		this.SELECT_HEADER.append("SELECT pt.id AS ptId, pt.metaname AS ptMetaname, pt.name AS ptName, pt.status AS ptStatus, pt.createdAt AS ptCreatedAt, pt.updatedAt AS ptUpdatedAt, pt.portid AS ptPortId, pt.device_id AS ptDeviceId");
		this.SELECT_HEADER.append(", s.id AS sId, s.metaname AS sMetaname, s.name AS sName, s.status AS sStatus, s.createdAt AS sCreatedAt, s.updatedAt AS sUpdatedAt, s.uselocal as sUseLocal");
		this.SELECT_HEADER.append(", dv.id AS dvId, dv.metaname AS dvMetaname, dv.name AS dvName, dv.status AS dvStatus, dv.createdAt AS dvCreatedAt, dv.updatedAt AS dvUpdatedAt, dv.address AS dvAddress, dv.connectiontype AS dvConnectionType, dv.dstport AS dvDstPort, dv.enableonstart AS dvEnableOnStart, dv.model AS dvModel, dv.srvclass AS dvSrvClass, dv.vendor AS dvVendor, dv.src_id AS dvSrcId");
		this.SELECT_HEADER.append(", dp.device_id AS dpId, dp.value AS dpValue, dp.property AS dpProperty");
		this.SELECT_HEADER.append(", pp.port_id AS ppId, pp.value AS ppValue, pp.property AS ppProperty");
		this.SELECT_TABLE.append(" FROM port pt");
		this.SELECT_JOINS.append(" LEFT JOIN device dv ON dv.id = pt.device_id");
		this.SELECT_JOINS.append(" LEFT JOIN source s ON dv.src_id = s.id");
		this.SELECT_JOINS.append(" LEFT JOIN deviceproperty dp ON dv.id = dp.device_id");
		this.SELECT_JOINS.append(" LEFT JOIN portproperty pp ON pt.id = pp.port_id");
	}

	public List<Port> getAll() {
		return listAll();
	}

	public Port findById(UUID id) {
		StringBuilder sql = new StringBuilder(SELECT_HEADER);

		sql.append(SELECT_TABLE);
		sql.append(SELECT_JOINS);
		sql.append(" WHERE pt.id = ?");
		return getByStatement(sql.toString(), id.toString());
	}

	public Port findByMetaname(UUID src, UUID dev, String metaname) {
		StringBuilder sql = new StringBuilder(SELECT_HEADER);

		sql.append(SELECT_TABLE);
		sql.append(SELECT_JOINS);
		sql.append(" WHERE s.id = ? AND dv.id = ? AND pt.metaname = ?");
		return getByStatement(sql.toString(), src.toString(), dev.toString(), metaname);
	}

	@Override
	public Port makePersistent(Port p) {
		return null;
	}

	@Override
	protected Port buildObject(ResultSet rs) throws SQLException {
		List<Port> ret = buildObjectList(rs);

		return ret.isEmpty() ? null : ret.get(0);
	}

	@Override
	protected List<Port> buildObjectList(ResultSet rs) throws SQLException {
		List<Port> ret = new ArrayList<>();

		while (rs.next()) {
			UUID ptId = UUID.fromString(rs.getString("ptId"));
			String ptMetaname = rs.getString("ptMetaname");
			String ptName = rs.getString("ptName");
			String ptStatus = rs.getString("ptStatus");
			Date ptCreatedAt = rs.getTimestamp("ptCreatedAt");
			Date ptUpdatedAt = rs.getTimestamp("ptUpdatedAt");
			Integer pId = rs.getInt("ptPortId");
			Port pt = new Port();

			pt.setId(ptId);
			pt.setMetaname(ptMetaname);
			pt.setName(ptName);
			pt.setStatus(ptStatus);
			pt.setCreatedAt(ptCreatedAt);
			pt.setUpdatedAt(ptUpdatedAt);
			pt.setPortId(pId);
			if (ret.contains(pt))
				pt = ret.remove(ret.indexOf(pt));
			else
				pt.setProperties(new HashMap<String, String>());

			String dvIdString = rs.getString("ptDeviceId");
			if (!rs.wasNull()) {
				UUID dvId = UUID.fromString(dvIdString);
				String dvMetaname = rs.getString("dvMetaname");
				String dvName = rs.getString("dvName");
				String dvStatus = rs.getString("dvStatus");
				Date dvCreatedAt = rs.getTimestamp("dvCreatedAt");
				Date dvUpdatedAt = rs.getTimestamp("dvUpdatedAt");
				String dvAddress = rs.getString("dvAddress");
				String dvConnectiontype = rs.getString("dvConnectionType");
				Integer dstPort = rs.getInt("dvDstPort");
				Boolean dvEnableOnStart = rs.getBoolean("dvEnableOnStart");
				String dvModel = rs.getString("dvModel");
				String srvClass = rs.getString("dvSrvClass");
				String dvVendor = rs.getString("dvVendor");
				Device dv = new Device();
				String sIdString = rs.getString("sId");

				dv.setPorts(new HashSet<Port>());
				dv.setProperties(new HashMap<String, String>());
				if (!rs.wasNull()) {
					UUID sId = UUID.fromString(sIdString);
					String sMetaname = rs.getString("sMetaname");
					String sName = rs.getString("sName");
					String sStatus = rs.getString("sStatus");
					Date sCreatedAt = rs.getTimestamp("sCreatedAt");
					Date sUpdatedAt = rs.getTimestamp("sUpdatedAt");
					Boolean sUseLocal = rs.getBoolean("sUseLocal");
					Source s = new Source();

					s.setId(sId);
					s.setMetaname(sMetaname);
					s.setName(sName);
					s.setStatus(sStatus);
					s.setCreatedAt(sCreatedAt);
					s.setUpdatedAt(sUpdatedAt);
					s.setUselocal(sUseLocal);
					dv.setSource(s);
				}

				String dpIdString = rs.getString("dpId");
				if (!rs.wasNull()) {
					UUID dpId = UUID.fromString(dpIdString);
					String value = rs.getString("dpValue");
					String property = rs.getString("dpProperty");

					dv.setId(dpId);
					dv.getProperties().put(property, value);
				}
				dv.setId(dvId);
				dv.setMetaname(dvMetaname);
				dv.setName(dvName);
				dv.setStatus(dvStatus);
				dv.setCreatedAt(dvCreatedAt);
				dv.setUpdatedAt(dvUpdatedAt);
				dv.setAddress(dvAddress);
				dv.setConnectionType(dvConnectiontype);
				dv.setDstport(dstPort);
				dv.setEnableOnStart(dvEnableOnStart);
				dv.setModel(dvModel);
				dv.setSrvClass(srvClass);
				dv.setVendor(dvVendor);
				dv.getPorts().add(pt);
				pt.setDevice(dv);
			}

			rs.getString("ppId");
			if (!rs.wasNull()) {
				String value = rs.getString("ppValue");
				String property = rs.getString("ppProperty");

				pt.getProperties().put(property, value);
			}

			ret.add(pt);
		}
		return ret;
	}
}
