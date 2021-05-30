package com.gtp.hunter.core.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.NamingException;

import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.process.repository.GenericRepository;

@ApplicationScoped
public class SourceNewRepository extends GenericRepository<Source, UUID> {

	public SourceNewRepository() throws NamingException {
		super(SourceNewRepository.class);
		this.SELECT_HEADER.append("SELECT s.id AS sId, s.metaname AS sMetaname, s.name AS sName, s.status AS sStatus, s.createdAt AS sCreatedAt, s.updatedAt AS sUpdatedAt, s.uselocal as sUseLocal");
		this.SELECT_HEADER.append(", dv.id AS dvId, dv.metaname AS dvMetaname, dv.name AS dvName, dv.status AS dvStatus, dv.createdAt AS dvCreatedAt, dv.updatedAt AS dvUpdatedAt, dv.address AS dvAddress, dv.connectiontype AS dvConnectionType, dv.dstport AS dvDstPort, dv.enableonstart AS dvEnableOnStart, dv.model AS dvModel, dv.srvclass AS dvSrvClass, dv.vendor AS dvVendor, dv.src_id AS dvSrcId");
		this.SELECT_HEADER.append(", pt.id AS ptId, pt.metaname AS ptMetaname, pt.name AS ptName, pt.status AS ptStatus, pt.createdAt AS ptCreatedAt, pt.updatedAt AS ptUpdatedAt, pt.portid AS ptPortId, pt.device_id AS ptDeviceId");
		this.SELECT_HEADER.append(", dp.device_id AS dpId, dp.value AS dpValue, dp.property AS dpProperty");
		this.SELECT_HEADER.append(", pp.port_id AS ppId, pp.value AS ppValue, pp.property AS ppProperty");
		this.SELECT_TABLE.append(" FROM source s ");
		this.SELECT_JOINS.append(" LEFT JOIN device dv ON s.id = dv.src_id");
		this.SELECT_JOINS.append(" LEFT JOIN port pt ON dv.id = pt.device_id");
		this.SELECT_JOINS.append(" LEFT JOIN deviceproperty dp ON dv.id = dp.device_id");
		this.SELECT_JOINS.append(" LEFT JOIN portproperty pp ON pt.id = pp.port_id");
	}

	public List<Source> getAll() {
		return listAll();
	}

	public Source findById(UUID id) {
		StringBuilder sql = new StringBuilder(SELECT_HEADER);

		sql.append(SELECT_TABLE);
		sql.append(SELECT_JOINS);
		sql.append(" WHERE s.id = ?");
		return getByStatement(sql.toString(), id.toString());
	}

	public Source findByMetaname(String metaname) {
		StringBuilder sql = new StringBuilder(SELECT_HEADER);

		sql.append(SELECT_TABLE);
		sql.append(SELECT_JOINS);
		sql.append(" WHERE s.metaname = ?");
		return getByStatement(sql.toString(), metaname);
	}

	@Override
	public Source makePersistent(Source s) {
		return null;
	}

	@Override
	protected Source buildObject(ResultSet rs) throws SQLException {
		List<Source> ret = buildObjectList(rs);

		return ret.isEmpty() ? null : ret.get(0);
	}

	@Override
	protected List<Source> buildObjectList(ResultSet rs) throws SQLException {
		List<Source> ret = new ArrayList<>();

		while (rs.next()) {
			UUID sId = UUID.fromString(rs.getString("sId"));
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
			if (ret.contains(s))
				s = ret.remove(ret.indexOf(s));
			else
				s.setDevices(new HashSet<Device>());

			String dvIdString = rs.getString("dvId");
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
				Optional<Device> opDev = s.getDevices().stream().filter(dv -> dv.getId().equals(dvId)).findAny();
				Device dv = opDev.isPresent() ? opDev.get() : new Device();

				if (!opDev.isPresent()) {
					dv.setPorts(new HashSet<Port>());
					dv.setProperties(new HashMap<String, String>());
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
				dv.setSource(s);
				s.getDevices().add(dv);
			}

			String ptIdString = rs.getString("ptId");
			if (!rs.wasNull()) {
				UUID ptId = UUID.fromString(ptIdString);
				String ptMetaname = rs.getString("ptMetaname");
				String ptName = rs.getString("ptName");
				String ptStatus = rs.getString("ptStatus");
				Date ptCreatedAt = rs.getTimestamp("ptCreatedAt");
				Date ptUpdatedAt = rs.getTimestamp("ptUpdatedAt");
				Integer pId = rs.getInt("ptPortId");
				UUID ptDeviceId = UUID.fromString(rs.getString("ptDeviceId"));
				Optional<Device> opDev = s.getDevices().stream().filter(dv -> dv.getId().equals(ptDeviceId)).findAny();
				Device dv = opDev.isPresent() ? opDev.get() : new Device();
				Port pt = new Port();

				dv.setId(ptDeviceId);
				pt.setId(ptId);
				pt.setMetaname(ptMetaname);
				pt.setName(ptName);
				pt.setStatus(ptStatus);
				pt.setCreatedAt(ptCreatedAt);
				pt.setUpdatedAt(ptUpdatedAt);
				pt.setPortId(pId);
				pt.setDevice(dv);
				dv.getPorts().add(pt);
				s.getDevices().add(dv);
			}

			String dpIdString = rs.getString("dpId");
			if (!rs.wasNull()) {
				UUID dpId = UUID.fromString(dpIdString);
				String value = rs.getString("dpValue");
				String property = rs.getString("dpProperty");
				Optional<Device> opDev = s.getDevices().stream().filter(dv -> dv.getId().equals(dpId)).findAny();
				Device dv = opDev.isPresent() ? opDev.get() : new Device();

				dv.setId(dpId);
				dv.getProperties().put(property, value);
				s.getDevices().add(dv);
			}

			String ppIdString = rs.getString("ppId");
			if (!rs.wasNull()) {
				UUID ppId = UUID.fromString(ppIdString);
				String value = rs.getString("ppValue");
				String property = rs.getString("ppProperty");

				for (Device dv : s.getDevices()) {
					Optional<Port> opPort = dv.getPorts().stream().filter(p -> p.getId().equals(ppId)).findAny();

					if (opPort.isPresent())
						opPort.get().getProperties().put(property, value);
				}
			}

			ret.add(s);
		}

		return ret;
	}

}
