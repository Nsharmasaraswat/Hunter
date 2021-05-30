package com.gtp.hunter.core.repository;

import java.util.HashMap;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

public abstract class CassandraBaseRepository {

	private static Cluster cluster;

	private Class persistedClass;
	
	private Session session;

	public CassandraBaseRepository(Class cls) {
		this.persistedClass = cls;
		initDB();
		createRawData();
	}

	protected void initDB() {
		//if(cluster==null) cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		if(cluster==null) cluster = Cluster.builder().addContactPoint(ConfigUtil.get("hunter-core", "cassandra-address", "127.0.0.1")).build();
		if(session==null) session = cluster.connect("hunter");
	}
	
	protected void close() {
		session.close();
		session = null;
	}
	
	protected Session getSession() {
		if(session==null) initDB();
		return session;
	}

	private void createKeyspace(String keyspaceName, String replicationStrategy, int replicationFactor) {
		StringBuilder sb = new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ").append(keyspaceName)
				.append(" WITH replication = {").append("'class':'").append(replicationStrategy)
				.append("','replication_factor':").append(replicationFactor).append("};");

		String query = sb.toString();
		session.execute(query);
	}
	
	private void checkKeySpaces() {
	}
	
	private void createRawData() {
		Map<String,String> campos = new HashMap<String,String>();
		campos.put("ts", "bigint");
		campos.put("type", "text");
		campos.put("source", "uuid");
		campos.put("device", "uuid");
		campos.put("port", "int");
		campos.put("tagid", "text");
		campos.put("payload", "text");
		createTable("rawdata", campos, "PRIMARY KEY (ts,type,source,device,port,tagid)","with compact storage");
		//session.execute("CREATE INDEX IF NOT EXISTS rawdata_ts ON hunter.rawdata ( KEYS ( ts ) )");
	}
	
	public void createTable(String table, Map<String,String> fields, String pk, String with) {
	    StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
	    sb.append(table).append(" ( ");
	    fields.keySet().stream().forEach(f -> sb.append(f + " " + fields.get(f) + ","));
	    sb.append(pk);
	    sb.append(") ");
	    if(with != null) {
	    	sb.append(with);
	    }
	    sb.append(";");	      
	 
	    String query = sb.toString();
	    session.execute(query);
	}
	
	private void checkTables() {
		cluster.getMetadata().getKeyspace("hunter").getTables().stream().forEach(t -> {
		});
	}
	
}
