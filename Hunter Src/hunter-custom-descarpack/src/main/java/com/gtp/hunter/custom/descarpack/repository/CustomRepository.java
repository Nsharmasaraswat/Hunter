package com.gtp.hunter.custom.descarpack.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.custom.descarpack.model.DocumentQuantitySummary;
import com.gtp.hunter.custom.descarpack.model.LotPosition;
import com.gtp.hunter.custom.descarpack.model.PrintRestModel;
import com.gtp.hunter.custom.descarpack.model.ThingQuantitySummary;
import com.gtp.hunter.process.model.Thing;

@Stateless
public class CustomRepository extends JPABaseRepository<Thing, UUID> {

	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private Event<Thing> cEvent;

	public CustomRepository() {
		super(Thing.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public Set<PrintRestModel> getPrintModels() {
		Set<PrintRestModel> ret = new HashSet<PrintRestModel>();
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement(
						"select " + "	dm.metaname, " + "    d.code, " + "    p.sku, " + "    t.name, " + "	btc.value as batch, " + "    exp.value as expiry, " + "    man.value as manufacture, " + "    u.tagid as epc " + "from documentmodel dm " + "join document d on d.documentmodel_id = dm.id " + "join documentthing dt on dt.document_id = d.id " + "join thing t on dt.thing_id = t.id " + "join product p on t.product_id = p.id " + "join (select p.thing_id, pmf.metaname, p.value from property p join propertymodelfield pmf on p.propertymodelfield_id = pmf.id where pmf.metaname = 'BATCH') btc on btc.thing_id = t.id " + "join (select p.thing_id, pmf.metaname, p.value from property p join propertymodelfield pmf on p.propertymodelfield_id = pmf.id where pmf.metaname = 'EXPIRY') exp on exp.thing_id = t.id " + "join (select p.thing_id, pmf.metaname, p.value from property p join propertymodelfield pmf on p.propertymodelfield_id = pmf.id where pmf.metaname = 'MANUFACTURE') man on man.thing_id = t.id " + "join thingunits tu on tu.thing_id = t.id " + "join unit u on tu.unit_id = u.id " + "where dm.metaname = 'INVENTORY' " + "and p.sku <> '0000000004' " + "and u.createdAt < '2018-05-09 18:17:32' " + "order by u.createdAt");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PrintRestModel p = new PrintRestModel();
				p.setMetaname(rs.getString("metaname"));
				p.setCode(rs.getString("code"));
				p.setSku(rs.getString("sku"));
				p.setDesc(rs.getString("name"));
				p.setBatch(rs.getString("batch"));
				p.setExpiry(rs.getString("expiry"));
				p.setManufacture(rs.getString("manufacture"));
				p.setEpc(rs.getString("epc"));
				ret.add(p);
			}
			rs.close();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Map<String, Integer> getSupplierDailyTags() {

		Map<String, Integer> ret = new HashMap<String, Integer>();

		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement("select s.name, dt.tags " + "	from supplier s join (select d.supplier_id, count(dt.id) as tags from document d join documentthing dt on dt.document_id = d.id join documentmodel dm on d.documentmodel_id = dm.id where dm.metaname = 'PO' and dt.createdAt between date_sub(now(), interval 1 day) and now() group by d.supplier_id) dt on dt.supplier_id = s.id " + "	where s.hasprinter = 1");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ret.put(rs.getString("name"), rs.getInt("tags"));
			}
			rs.close();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Set<String> getPrintJson() {
		Set<String> ret = new HashSet<String>();
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement(
						"select concat('{\"document-number\":\"',d.code,'\",\"document-type\":\"',dm.metaname,'\",\"items\":[{\"sku\":\"',p.sku,'\",\"epc\":\"',u.tagid,'\",\"batch\":\"',plote.value,'\",\"manufacture\":\"',pfab.value,'\",\"expiry\":\"',IFNULL(pvenc.value,\"\"),'\"}]}') " + "from thing t " + "join documentthing dt on dt.thing_id = t.id " + "join document d on dt.document_id = d.id " + "join documentmodel dm on d.documentmodel_id = dm.id " + "join product p on t.product_id = p.id " + "join thingunits tu on tu.thing_id = t.id " + "join unit u on tu.unit_id = u.id " + "join property plote on plote.thing_id = t.id " + "join propertymodelfield pmflote on plote.propertymodelfield_id = pmflote.id " + "join property pfab on pfab.thing_id = t.id " + "join propertymodelfield pmffab on pfab.propertymodelfield_id = pmffab.id " + "join property pvenc on pvenc.thing_id = t.id " + "join propertymodelfield pmfvenc on pvenc.propertymodelfield_id = pmfvenc.id " + "where t.createdAt > '2018-05-01 00:00:00'  " + "and t.status <> 'SEPARADO'  " + "and pmflote.metaname = 'BATCH' " + "and plote.value not like '%test%' " + "and plote.value not like '%tete%' " + "and pmffab.metaname = 'MANUFACTURE'  " + "and pmfvenc.metaname = 'EXPIRY' " + "and dm.metaname in ('INVENTORY','PO','MO') ");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString(1));
			}
			rs.close();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Set<DocumentQuantitySummary> getDocumentQuantitySummary() {
		Set<DocumentQuantitySummary> ret = new HashSet<>();
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT IF(s.name IS NULL OR s.name = 'COLETOR' OR s.name = 'FRALDA', 'DESCARPACK', s.name) AS Supplier, ");
		sql.append("	   dm.name AS Type,  ");
		sql.append("	   d.code AS Document,  ");
		sql.append("	   IF(dtNOVO.qty IS NULL, 0, dtNOVO.qty) AS 'IMPRESSO',  ");
		sql.append("	   IFNULL(dtEMBARCADO.qty, 0) AS 'EMBARCADO',  ");
		sql.append("	   IFNULL(dtRECEBIDO.qty, 0) AS 'RECEBIDO',  ");
		sql.append("	   IFNULL(dtARMAZENADO.qty, 0) AS 'ARMAZENADO',  ");
		sql.append("	   IFNULL(dtSEPARADO.qty, 0) AS 'SEPARADO',  ");
		sql.append("	   IFNULL(dtEXPEDIDO.qty, 0) AS 'EXPEDIDO',  ");
		sql.append("	   IFNULL(dtCANCELADO.qty, 0) AS 'CANCELADO' FROM document d ");
		sql.append("	INNER JOIN documentmodel dm ON d.documentmodel_id = dm.id ");
		sql.append("    LEFT JOIN supplier s ON d.supplier_id = s.id ");
		sql.append("    LEFT JOIN (SELECT document_id, COUNT(DISTINCT thing_id) AS qty FROM documentthing WHERE status = 'NOVO' GROUP BY document_id) dtNOVO ON dtNOVO.document_id = d.id ");
		sql.append("    LEFT JOIN (SELECT `do`.id AS document_id, COUNT(DISTINCT dt.thing_id) AS qty FROM documentthing dt INNER JOIN document d ON dt.document_id = d.id INNER JOIN document `do` ON d.parent_id = `do`.id WHERE dt.status = 'EMBARCADO' GROUP BY dt.document_id) dtEMBARCADO ON dtEMBARCADO.document_id = d.id ");
		sql.append("    LEFT JOIN (SELECT document_id, COUNT(DISTINCT thing_id) AS qty FROM documentthing WHERE status = 'RECEBIDO' GROUP BY document_id) dtRECEBIDO ON dtRECEBIDO.document_id = d.id ");
		sql.append("    LEFT JOIN (SELECT document_id, COUNT(DISTINCT thing_id) AS qty FROM documentthing WHERE status = 'ARMAZENADO' GROUP BY document_id) dtARMAZENADO ON dtARMAZENADO.document_id = d.id ");
		sql.append("    LEFT JOIN (SELECT document_id, COUNT(DISTINCT thing_id) AS qty FROM documentthing WHERE status = 'SEPARADO' GROUP BY document_id) dtSEPARADO ON dtSEPARADO.document_id = d.id ");
		sql.append("    LEFT JOIN (SELECT `do`.id AS document_id, COUNT(DISTINCT dt.thing_id) AS qty FROM documentthing dt INNER JOIN document d ON dt.document_id = d.id INNER JOIN document `do` ON d.parent_id = `do`.id WHERE dt.status = 'EXPEDIDO' GROUP BY dt.document_id) dtEXPEDIDO ON dtEXPEDIDO.document_id = d.id ");
		sql.append("    LEFT JOIN (SELECT document_id, COUNT(DISTINCT thing_id) AS qty FROM documentthing WHERE status = 'CANCELADO' GROUP BY document_id) dtCANCELADO ON dtCANCELADO.document_id = d.id ");
		sql.append("    HAVING IMPRESSO != 0 OR EMBARCADO != 0 OR RECEBIDO != 0 OR ARMAZENADO != 0 OR SEPARADO != 0 OR EXPEDIDO != 0 OR CANCELADO != 0;");
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement(sql.toString());
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				DocumentQuantitySummary p = new DocumentQuantitySummary();

				p.setSupplierName(rs.getString("Supplier"));
				p.setDocumentType(rs.getString("Type"));
				p.setDocumentCode(rs.getString("Document"));
				p.setImpresso(rs.getInt("IMPRESSO"));
				p.setEmbarcado(rs.getInt("EMBARCADO"));
				p.setRecebido(rs.getInt("RECEBIDO"));
				p.setArmazenado(rs.getInt("ARMAZENADO"));
				p.setSeparado(rs.getInt("SEPARADO"));
				p.setExpedido(rs.getInt("EXPEDIDO"));
				p.setCancelado(rs.getInt("CANCELADO"));
				ret.add(p);
			}
			rs.close();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Set<ThingQuantitySummary> getThingQuantitySummary() {
		Set<ThingQuantitySummary> ret = new HashSet<>();
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT p.sku AS sku, ");
		sql.append("   p.name AS description, ");
		sql.append("   IFNULL(tIMPRESSO.qty,0) AS IMPRESSO, ");
		sql.append("   IFNULL(tEMBARCADO.qty,0) AS EMBARCADO, ");
		sql.append("   IFNULL(tRECEBIDO.qty,0) AS RECEBIDO, ");
		sql.append("   IFNULL(tARMAZENADO.qty,0) AS ARMAZENADO, ");
		sql.append("   IFNULL(tSEPARADO.qty,0) AS SEPARADO, ");
		sql.append("   IFNULL(tEXPEDIDO.qty,0) AS EXPEDIDO, ");
		sql.append("   IFNULL(tCANCELADO.qty,0) AS CANCELADO");
		sql.append(" FROM product p ");
		sql.append(" LEFT JOIN (SELECT product_id, COUNT(DISTINCT id) AS qty FROM thing WHERE status = 'IMPRESSO' GROUP BY product_id) tIMPRESSO ON tIMPRESSO.product_id = p.id");
		sql.append(" LEFT JOIN (SELECT product_id, COUNT(DISTINCT id) AS qty FROM thing WHERE status = 'EMBARCADO' GROUP BY product_id) tEMBARCADO ON tEMBARCADO.product_id = p.id");
		sql.append(" LEFT JOIN (SELECT product_id, COUNT(DISTINCT id) AS qty FROM thing WHERE status = 'RECEBIDO' GROUP BY product_id) tRECEBIDO ON tRECEBIDO.product_id = p.id");
		sql.append(" LEFT JOIN (SELECT product_id, COUNT(DISTINCT id) AS qty FROM thing WHERE status = 'ARMAZENADO' GROUP BY product_id) tARMAZENADO ON tARMAZENADO.product_id = p.id");
		sql.append(" LEFT JOIN (SELECT product_id, COUNT(DISTINCT id) AS qty FROM thing WHERE status = 'SEPARADO' GROUP BY product_id) tSEPARADO ON tSEPARADO.product_id = p.id");
		sql.append(" LEFT JOIN (SELECT product_id, COUNT(DISTINCT id) AS qty FROM thing WHERE status = 'EXPEDIDO' GROUP BY product_id) tEXPEDIDO ON tEXPEDIDO.product_id = p.id");
		sql.append(" LEFT JOIN (SELECT product_id, COUNT(DISTINCT id) AS qty FROM thing WHERE status = 'CANCELADO' GROUP BY product_id) tCANCELADO ON tCANCELADO.product_id = p.id");
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement(sql.toString());
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				ThingQuantitySummary p = new ThingQuantitySummary();

				p.setSku(rs.getString("sku"));
				p.setDescription(rs.getString("description"));
				p.setImpresso(rs.getInt("IMPRESSO"));
				p.setEmbarcado(rs.getInt("EMBARCADO"));
				p.setRecebido(rs.getInt("RECEBIDO"));
				p.setArmazenado(rs.getInt("ARMAZENADO"));
				p.setSeparado(rs.getInt("SEPARADO"));
				p.setExpedido(rs.getInt("EXPEDIDO"));
				p.setCancelado(rs.getInt("CANCELADO"));
				ret.add(p);
			}
			rs.close();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Set<LotPosition> getLotPosition(String lotnumber) {
		Set<LotPosition> ret = new HashSet<LotPosition>();
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement("select p.sku, t.status, u.tagid " + "from property pty " + "join thing t on pty.thing_id = t.id " + "join thingunits tu on tu.thing_id = t.id " + "join unit u on tu.unit_id = u.id " + "join product p on t.product_id = p.id " + "where pty.value = ? order by t.status");
			ps.setString(1, lotnumber);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				LotPosition p = new LotPosition();
				p.setSku(rs.getString("sku"));
				p.setStatus(rs.getString("status"));
				p.setTagid(rs.getString("tagid"));
				ret.add(p);
			}
			rs.close();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public void receiveLot(String lotnumber) {
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement("update thing set status = 'ARMAZENADO' where id in (select thing_id from property where value = ? ) and status = 'EMBARCADO'");
			ps.setString(1, lotnumber);
			ps.executeUpdate();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void storeLot(String lotnumber) {
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement("update thing set status = 'ARMAZENADO' where id in (select thing_id from property where value = ? ) and status = 'RECEBIDO'");
			ps.setString(1, lotnumber);
			ps.executeUpdate();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void blockLot(String lotnumber) {
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement("update thing set status = 'BLOQUEADO' where id in (select thing_id from property where value = ? ) and status = 'ARMAZENADO'");
			ps.setString(1, lotnumber);
			ps.executeUpdate();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void unblockLot(String lotnumber) {
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement("update thing set status = 'ARMAZENADO' where id in (select thing_id from property where value = ? ) and status = 'BLOQUEADO'");
			ps.setString(1, lotnumber);
			ps.executeUpdate();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void unlockDocument(String doc) {
		try {
			Connection con = initConnection();
			PreparedStatement ps = con.prepareStatement("delete from documentfield where document_id in (select id from document where code = ?)");
			ps.setString(1, doc);
			ps.executeUpdate();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void firePersistEvent(Thing obj) {
		cEvent.fire(obj);
	}

}