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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.ui.json.ViewTaskStub;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DynTaskDefRepository {

	private static DataSource ds;
	
	public List<ViewTaskStub> listStubsByModelAndItemThingDifference(DocumentModel model) {
		
		List<ViewTaskStub> ret = new ArrayList<ViewTaskStub>();
		
		try(Connection con = initConnection();
				PreparedStatement ps = con.prepareStatement("select id, code as doccode, createdAt, name as docname, sum(qty) - sum(itens) as contents from (select d.id, d.createdAt, d.name, d.code, di.qty,count(t.id) as itens " + "	 from documentitem di " + "	 join document d on di.document_id = d.id " + "	 left join documentthing dt on dt.document_id = di.document_id " + "	 left join thing t on dt.thing_id = t.id and t.product_id = di.product_id and t.status <> 'CANCELADO' " + "	 where d.documentmodel_id = ? and d.status <> 'CANCELADO' " + "	 group by d.id, di.product_id " + "	 having di.qty <> count(t.id)) qry " + "group by id");) {
			
			ps.setString(1, model.getId().toString());
			try(ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					ViewTaskStub vts = new ViewTaskStub();
					vts.setId(UUID.fromString(rs.getString("id")));
					vts.setCreatedAt(rs.getDate("createdAt"));
					vts.setDoccode(rs.getString("doccode"));
					vts.setDocname(rs.getString("docname"));
					vts.setContents(rs.getString("contents"));
					ret.add(vts);
				}
				// ret = (List<ViewTaskStub>) DBUtil.resultSetToList(rs, ViewTaskStub.class);
				rs.close();
			}
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<ViewTaskStub> listStubsByModelAndItemThingDifferenceAndUnit(DocumentModel model, String unit) {
		List<ViewTaskStub> ret = new ArrayList<ViewTaskStub>();
		StringBuilder query = new StringBuilder();

		query.append("SELECT d.id, d.createdAt, d.name AS docname, d.code AS doccode, SUM(d.total) - SUM(d.itens) AS contents FROM (");
		query.append("SELECT d.id, d.createdAt, d.name, d.code, d.unit, d.product_id, d.total, COUNT(t.id) AS itens");
		query.append("  FROM (");
		query.append("	SELECT d.id, d.createdAt, d.name, df.value AS unit, di.product_id, SUM(di.qty) AS total");
		query.append("		FROM document d");
		query.append("		INNER JOIN documentitem di ON di.document_id = d.id");
		query.append("		LEFT JOIN documentfield df ON df.document_id = d.id");
		query.append("		WHERE d.documentmodel_id = ? ");
		query.append("         AND d.status = 'PICKING'");
		query.append("         AND (df.value = ? OR df.value IS NULL)");
		query.append("		GROUP BY d.id, di.product_id");
		query.append("  ) d");
		query.append("  LEFT JOIN (SELECT * FROM documentthing WHERE status <> 'CANCELADO') dt ON dt.document_id = d.id");
		query.append("  LEFT JOIN thing t ON dt.thing_id = t.id AND t.product_id = d.product_id");
		query.append("  GROUP BY d.id, d.product_id");
		query.append("  HAVING d.total <> itens) d");
		query.append("  GROUP BY d.id");
		try (Connection con = initConnection();
				PreparedStatement ps = con.prepareStatement(query.toString());){
			
			
			ps.setString(1, model.getId().toString());
			ps.setString(2, unit);
			try(ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					ViewTaskStub vts = new ViewTaskStub();
					vts.setId(UUID.fromString(rs.getString("id")));
					vts.setCreatedAt(rs.getDate("createdAt"));
					vts.setDoccode(rs.getString("doccode"));
					vts.setDocname(rs.getString("docname"));
					vts.setContents(rs.getString("contents"));
					ret.add(vts);
				}
				// ret = (List<ViewTaskStub>) DBUtil.resultSetToList(rs, ViewTaskStub.class);
				rs.close();
			}
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<ViewTaskStub> listStubsByModelAndStatusAndItemThingDifference(DocumentModel model, String status) {
		List<ViewTaskStub> ret = new ArrayList<ViewTaskStub>();
		StringBuilder query = new StringBuilder();

		query.append("SELECT id, createdAt, name AS docname, code AS doccode, SUM(qty) - SUM(itens) AS contents");
		query.append(" FROM (SELECT d.id, d.createdAt, d.name, d.code, di.qty,count(t.id) AS itens ");
		query.append("        FROM documentitem di");
		query.append("	      JOIN document d ON di.document_id = d.id ");
		query.append("	      LEFT JOIN documentthing dt ON dt.document_id = di.document_id ");
		query.append("	      LEFT JOIN thing t ON dt.thing_id = t.id and t.product_id = di.product_id and t.status <> 'CANCELADO' ");
		query.append("	      WHERE d.documentmodel_id = ? ");
		query.append("          AND d.status = ? ");
		query.append("	      GROUP BY d.id, di.product_id ");
		query.append("	      HAVING di.qty <> count(t.id)) qry ");
		query.append(" GROUP BY id");
		try (Connection con = initConnection();
				PreparedStatement ps = con.prepareStatement(query.toString());) {
			ps.setString(1, model.getId().toString());
			ps.setString(2, status);
			try(ResultSet rs = ps.executeQuery();){
				while (rs.next()) {
					ViewTaskStub vts = new ViewTaskStub();

					vts.setId(UUID.fromString(rs.getString("id")));
					vts.setCreatedAt(rs.getDate("createdAt"));
					vts.setDoccode(rs.getString("doccode"));
					vts.setDocname(rs.getString("docname"));
					vts.setContents(rs.getString("contents"));
					ret.add(vts);
				}
				rs.close();
			}
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	protected Connection initConnection() {
		try {
			if(ds==null) {
				Context init = new InitialContext();
				Context ctx = (Context) init.lookup("java:");
				ds = (DataSource) ctx.lookup("hunter2");
				ctx.close();
			}
			return ds.getConnection();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
