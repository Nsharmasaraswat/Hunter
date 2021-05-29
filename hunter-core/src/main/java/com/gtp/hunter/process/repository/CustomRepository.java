package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.common.manager.ConnectionManager;
import com.gtp.hunter.ui.json.RNCProductStub;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class CustomRepository extends ConnectionManager {

	@Inject
	private Logger				logger;

	private SimpleDateFormat	sdf1	= new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	private SimpleDateFormat	sdf2	= new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat	sdf3	= new SimpleDateFormat("dd/MM/yyyy");

	public List<RNCProductStub> getStubsByProductId(UUID pId) {
		List<RNCProductStub> ret = new ArrayList<>();
		StringBuilder query = new StringBuilder();

		query.append("SELECT t.id AS id, p.sku AS sku, p.name AS name, prQty.value AS qty, prLot.value AS lot, prMan.value AS manuf, prExp.value AS exp, rnc.code AS rncCode, t.status AS status, SUBSTR(u.tagId, 7) AS serial, a.name AS address");
		query.append(" FROM thing t ");
		query.append(" INNER JOIN product p ON t.product_id = p.id ");
		query.append(" INNER JOIN address a ON t.address_id = a.id ");
		query.append(" LEFT JOIN thingunits tu ON tu.thing_id = t.id ");
		query.append(" LEFT JOIN unit u ON tu.unit_id = u.id AND u.type = 'EPC96' ");
		query.append(" INNER JOIN (SELECT pr.value, pr.thing_id FROM property pr INNER JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id WHERE prmf.metaname = 'QUANTITY') prQty ON prQty.thing_id = t.id ");
		query.append(" INNER JOIN (SELECT pr.value, pr.thing_id FROM property pr INNER JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id WHERE prmf.metaname = 'MANUFACTURING_BATCH') prMan ON prMan.thing_id = t.id ");
		query.append(" INNER JOIN (SELECT pr.value, pr.thing_id FROM property pr INNER JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id WHERE prmf.metaname = 'LOT_EXPIRE') prExp ON prExp.thing_id = t.id ");
		query.append(" INNER JOIN (SELECT pr.value, pr.thing_id FROM property pr INNER JOIN propertymodelfield prmf ON pr.propertymodelfield_id = prmf.id WHERE prmf.metaname = 'LOT_ID') prLot ON prLot.thing_id = t.id ");
		query.append(" LEFT OUTER JOIN (SELECT d.code, dt.thing_id FROM document d INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id INNER JOIN documentthing dt ON dt.document_id = d.id WHERE dm.metaname = 'APORNC' ORDER BY d.createdAt LIMIT 1) rnc ON rnc.thing_id = t.id ");
		query.append(" WHERE t.status <> 'CONSUMIDO' AND t.status <> 'CANCELADO' AND t.status <> 'EXPEDIDO'");
		query.append("   AND p.id = ? ");
		query.append(" GROUP BY t.id ");
		query.append(" ORDER BY a.metaname, t.createdAt ");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(query.toString());) {
			ps.setString(1, pId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					RNCProductStub rps = new RNCProductStub();
					String qty = rs.getString("qty").replace(",", ".");

					rps.setId(rs.getString("id"));
					rps.setSku(rs.getString("sku"));
					rps.setName(rs.getString("name"));
					if (qty.isEmpty())
						rps.setQty(0d);
					else
						rps.setQty(Double.parseDouble(qty));
					rps.setLot_id(rs.getString("lot"));
					rps.setManuf(convertDate(rs.getString("manuf")));
					rps.setExp(convertDate(rs.getString("exp")));
					rps.setRnc(rs.getString("rncCode"));
					rps.setStatus(rs.getString("status"));
					rps.setSerial(rs.getString("serial"));
					rps.setAddress(rs.getString("address"));
					ret.add(rps);
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

	private Date convertDate(String dt) {
		Date ret = null;

		try {
			ret = sdf1.parse(dt);
		} catch (ParseException pe1) {
			try {
				ret = sdf2.parse(dt);
			} catch (ParseException pe2) {
				try {
					ret = sdf3.parse(dt);
				} catch (ParseException pe3) {
					logger.error("Can't convert " + dt + " to date");
				}
			}
		}
		return ret;
	}

	public String findParentDriver(UUID id) {
		StringBuilder query = new StringBuilder("SELECT ps.name FROM");

		query.append(" document chd ");
		query.append(" INNER JOIN document prt ON chd.parent_id = prt.id");
		query.append(" INNER JOIN person ps ON prt.person_id = ps.id");
		query.append(" WHERE chd.id = ?");
		return executeQuery(query.toString(), id.toString());
	}

	public String findParentLoad(UUID id) {
		StringBuilder query = new StringBuilder("SELECT REPLACE(df.value, 'Carga: ', '') FROM");

		query.append(" document chd ");
		query.append(" INNER JOIN document prt ON chd.parent_id = prt.id");
		query.append(" INNER JOIN documentfield df ON df.document_id = prt.id AND df.documentmodelfield_id = '979ac0a5-6119-11e9-a948-0266c0e70a8c'");
		query.append(" WHERE chd.id = ?");
		return executeQuery(query.toString(), id.toString());
	}

	public String findParentDelivery(UUID id) {
		StringBuilder query = new StringBuilder("SELECT df.value FROM");

		query.append(" document chd ");
		query.append(" INNER JOIN document pck ON chd.parent_id = pck.parent_id AND pck.documentmodel_id = '7f1bb432-94cb-4044-a791-221ee0f92bd0'");
		query.append(" INNER JOIN documentfield df ON df.document_id = pck.id AND df.documentmodelfield_id = '9f2d9e5d-114d-11ea-a56b-005056a19775'");
		query.append(" WHERE chd.id = ?");
		query.append("  AND df.value != ''");
		query.append(" LIMIT 1");
		return executeQuery(query.toString(), id.toString());
	}

	public String findParentPlates(UUID id) {
		StringBuilder query = new StringBuilder("SELECT u.tagId FROM");

		query.append(" document chd ");
		query.append(" INNER JOIN document prt ON chd.parent_id = prt.id");
		query.append(" INNER JOIN documentfield df ON df.document_id = prt.id AND df.documentmodelfield_id = 'cb37d7a3-628e-41f6-92f5-1ff5d0dfa028'");
		query.append(" INNER JOIN thingunits tu ON df.value = tu.thing_id");
		query.append(" INNER JOIN unit u ON tu.unit_id = u.id");
		query.append(" WHERE chd.id = ?");
		query.append(" AND (u.type = 'LICENSEPLATES' OR u.type = 'REALPICKING')");
		query.append(" ORDER BY u.type");
		query.append(" LIMIT 1");

		return executeQuery(query.toString(), id.toString());
	}

	public String findParentCarrier(UUID id) {
		StringBuilder query = new StringBuilder("SELECT pr.value FROM");

		query.append(" document chd ");
		query.append(" INNER JOIN document prt ON chd.parent_id = prt.id");
		query.append(" INNER JOIN documentfield df ON df.document_id = prt.id AND df.documentmodelfield_id = 'cb37d7a3-628e-41f6-92f5-1ff5d0dfa028'");
		query.append(" INNER JOIN property pr ON df.value = pr.thing_id AND pr.propertymodelfield_id = 'bdd1544a-476f-11ea-b9fa-005056a19775'");
		query.append(" WHERE chd.id = ?");
		return executeQuery(query.toString(), id.toString());
	}

	public String findParentSupplierCustomer(UUID id) {
		StringBuilder query = new StringBuilder("SELECT CASE WHEN sup.id IS NOT NULL AND cus.id IS NULL THEN GROUP_CONCAT(DISTINCT CONCAT(INSERT( INSERT( INSERT( INSERT( sup.code, 13, 0, '-' ), 9, 0, '/' ), 6, 0, '.' ), 3, 0, '.'),' - ',sup.name) SEPARATOR ' / ') ");

		query.append(" WHEN cus.id IS NOT NULL AND sup.id IS NULL THEN GROUP_CONCAT(DISTINCT CONCAT(cus.code, ' - ', cus.name) SEPARATOR ' / ') ");
		query.append(" WHEN sup.id IS NOT NULL AND cus.id IS NOT NULL THEN CONCAT(GROUP_CONCAT(DISTINCT CONCAT(INSERT( INSERT( INSERT( INSERT( sup.code, 13, 0, '-' ), 9, 0, '/' ), 6, 0, '.' ), 3, 0, '.'),' - ',sup.name) SEPARATOR ' / '),GROUP_CONCAT(DISTINCT CONCAT(cus.code, ' - ', cus.name) SEPARATOR ' / ')) END AS `pers` ");
		query.append(" FROM document chd ");
		query.append(" INNER JOIN document sib ON chd.parent_id = sib.parent_id AND sib.person_id IS NOT NULL ");
		query.append(" LEFT JOIN person sup ON sib.person_id = sup.id AND sup.personmodel_id = '4c7b825e-36b0-11e9-a948-0266c0e70a8c' ");
		query.append(" LEFT JOIN person cus ON sib.person_id = cus.id AND cus.personmodel_id = '4c7c47f5-36b0-11e9-a948-0266c0e70a8c'");
		query.append(" WHERE chd.id = ?");
		query.append(" GROUP BY sup.id, cus.id;");
		return executeQuery(query.toString(), id.toString());
	}

	private String executeQuery(String query, String id) {
		String ret = "";

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(query);) {
			ps.setString(1, id);
			try (ResultSet rs = ps.executeQuery();) {
				if (rs.next()) {
					ret = rs.getString(1);
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
}
