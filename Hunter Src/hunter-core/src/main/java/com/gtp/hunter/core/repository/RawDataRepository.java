package com.gtp.hunter.core.repository;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;

import com.datastax.driver.core.ResultSet;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.core.model.ComplexData;

@ApplicationScoped
public class RawDataRepository extends CassandraBaseRepository {

	public RawDataRepository() {
		super(ComplexData.class);
	}

	@Asynchronous
	public void persist(ComplexData data) {
		StringBuilder sb = new StringBuilder();

		sb.append("insert into rawdata (source,device,port,tagid,ts,payload,type) values (" + data.getSource() + "," + data.getDevice() + "," + data.getPort() + ",'" + data.getTagId() + "'," + data.getTs() + ",'" + data.getPayload() + "','" + data.getType().toString() + "');");
		getSession().execute(sb.toString());
	}

	public List<ComplexData> getAll() {
		List<ComplexData> ret = new ArrayList<ComplexData>();
		ResultSet rs = getSession().execute("select * from rawdata;");

		rs.forEach(r -> {
			ComplexData rd = new ComplexData();
			rd.setSource(r.getUUID("source"));
			rd.setDevice(r.getUUID("device"));
			rd.setPort(r.getInt("port"));
			rd.setTagId(r.getString("tagid"));
			rd.setTs(r.getLong("ts"));
			rd.setPayload(r.getString("payload"));
			rd.setType(RawDataType.valueOf(r.getString("type")));
			ret.add(rd);
		});
		return ret;
	}

	public List<ComplexData> listInterval(long begin, long end) {
		List<ComplexData> ret = new ArrayList<ComplexData>();
		ResultSet rs = getSession().execute("select * from rawdata WHERE ts >= " + begin + " AND ts <=" + end + " ALLOW FILTERING;");

		rs.forEach(r -> {
			ComplexData rd = new ComplexData();
			rd.setSource(r.getUUID("source"));
			rd.setDevice(r.getUUID("device"));
			rd.setPort(r.getInt("port"));
			rd.setTagId(r.getString("tagid"));
			rd.setTs(r.getLong("ts"));
			rd.setPayload(r.getString("payload"));
			rd.setType(RawDataType.valueOf(r.getString("type")));
			ret.add(rd);
		});
		return ret;
	}

}
