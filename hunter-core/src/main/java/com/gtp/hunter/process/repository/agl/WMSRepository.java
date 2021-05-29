package com.gtp.hunter.process.repository.agl;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.manager.ConnectionManager;
import com.gtp.hunter.process.jsonstubs.StockDueDateJson;
import com.gtp.hunter.process.jsonstubs.WMSRule;
import com.gtp.hunter.process.jsonstubs.WMSStkSnapshot;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.ui.json.AddressOcupationStub;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class WMSRepository extends ConnectionManager {

	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String				WMS_DATASOURCE	= "wms1";

	public void updateAddressCode(UUID addressId, String code) {
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE armlocaddress SET address_code = ? WHERE address_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, code);
			st.setString(2, addressId.toString());
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void updateThingStatus(UUID thingId, String status) {
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE things SET things_status = ? WHERE things_id = ? OR things_parent_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, status);
			st.setString(2, thingId.toString());
			st.setString(3, thingId.toString());
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void updateThingLote(UUID thingId, String lote) {
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE things SET things_lote = ? WHERE things_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, lote);
			st.setString(2, thingId.toString());
			st.setString(3, thingId.toString());
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void updateThingStkLteStatus(UUID thingId, String lte, String status) {
		StringBuilder sql = new StringBuilder("UPDATE things wt");

		sql.append(" INNER JOIN stk s ON s.things_id = wt.things_id ");
		sql.append(" INNER JOIN things wts ON wts.things_parent_id = wt.things_id ");
		sql.append(" SET wt.things_lote = ?, ");
		sql.append("  wts.things_lote = ?, ");
		sql.append("  s.lte_id = ?,");
		sql.append("  wt.things_status = ?,");
		sql.append("  wts.things_status = ?");
		sql.append(" WHERE s.things_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, lte);
			st.setString(2, lte);
			st.setString(3, lte);
			st.setString(4, status);
			st.setString(5, status);
			st.setString(6, thingId.toString());
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void updateThingStatusQuantityLot(UUID id, String status, BigDecimal qty, String lot) {
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE things SET things_status = ?, things_qtd = ?, things_lote = ? WHERE things_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, status);
			st.setBigDecimal(2, qty);
			st.setString(3, lot);
			st.setString(4, id.toString());
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void updateThingQuantity(UUID thingId, BigDecimal qty) {
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE things SET things_qtd = ? WHERE things_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setBigDecimal(1, qty);
			st.setString(2, thingId.toString());
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void updateThingAddress(UUID thingId, UUID addressId) {
		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE things SET things_address_id = ? WHERE things_id = ? OR things_parent_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, addressId.toString());
			st.setString(2, thingId.toString());
			st.setString(3, thingId.toString());
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}
	
	public void updateLte(String oldLte, String newLte) {
		StringBuilder sql = new StringBuilder("INSERT IGNORE INTO lte ");

		sql.append(" (lte_id, lte_nome, lte_fabricacao, lte_validade, lte_totfabricado)");
		sql.append(" SELECT * FROM ");
		sql.append(" (SELECT ?, lte_nome, lte_fabricacao, lte_validade, '1'");
		sql.append("    FROM lte ");
		sql.append("    WHERE lte_id = ?) AS old");
		sql.append(" ON DUPLICATE KEY UPDATE lte_totfabricado = lte_totfabricado + 1");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, newLte);
			st.setString(2, oldLte);
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public List<WMSRule> listRules(int prgid) {
		StringBuilder sql = new StringBuilder("SELECT ");
		List<WMSRule> ret = new ArrayList<>();

		sql.append(" tpl.tpl_id AS `id`,");
		sql.append(" tpl.tpl_descricao AS `desc`,");
		sql.append(" tbltplatt.coltab_tabdesc AS `lefttabdesc`,");
		sql.append(" coltplatt.coltabitm_coldesc AS `leftcoldesc`,");
		sql.append(" tplitm.tplitm_operador AS `operator`,");
		sql.append(" tbltplcnd.coltab_tabdesc AS `righttabdesc`,");
		sql.append(" coltplcnd.coltabitm_coldesc AS `rightcoldesc`,");
		sql.append(" tplitm.tplitm_constante AS `value`,");
		sql.append(" tplitm.tplitm_prior AS `priority`");
		sql.append("	FROM tpl ");
		sql.append("	INNER JOIN tplitm ON tpl.tpl_id = tplitm.tpl_id");
		sql.append("    INNER JOIN coltab tbltplatt ON tplitm_tblid = tbltplatt.coltab_id");
		sql.append("    INNER JOIN coltabitm coltplatt ON tbltplatt.coltab_id = coltplatt.coltab_id AND tplitm.tplitm_tblattid = coltplatt.coltabitm_id");
		sql.append("    INNER JOIN coltab tbltplcnd ON tplitm_tblcndid = tbltplcnd.coltab_id");
		sql.append("    INNER JOIN coltabitm coltplcnd ON tbltplcnd.coltab_id = coltplcnd.coltab_id AND tplitm.tplitm_tblattcndid = coltplcnd.coltabitm_id");
		sql.append("    WHERE tpl_flagprg = ?");
		sql.append("    ORDER BY tpl.tpl_id, tplitm.tplitm_prior");

		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setInt(1, prgid);
			try (ResultSet rs = st.executeQuery();) {
				WMSRule rule = new WMSRule();
				int lastPriority = -1;

				while (rs.next()) {
					int id = rs.getInt("id");
					int priority = rs.getInt("priority");
					String strPriority = "";
					if (rule.getId() != id) {
						rule = new WMSRule();
						rule.setId(id);
						rule.setName(rs.getString("desc"));
						rule.setConds(new ArrayList<String>());
						lastPriority = -1;
						ret.add(rule);
					}

					if (lastPriority == -1 || lastPriority != priority) {
						strPriority = priority + " - ";
						lastPriority = priority;
					} else {
						strPriority = rule.getConds().remove(rule.getConds().size() - 1);
						if (lastPriority != priority)
							strPriority += " OU ";
						else if (lastPriority == priority)
							strPriority += " E ";
					}
					//String cond = strPriority + rs.getString("lefttabdesc") + ".";
					String cond = strPriority;

					cond += rs.getString("leftcoldesc");
					cond += " " + rs.getString("operator") + " ";
					if (!rs.getString("righttabdesc").equals("Constante")) {
						//cond += rs.getString("righttabdesc") + ".";
						cond += rs.getString("rightcoldesc");
					} else
						cond += rs.getString("value");
					rule.getConds().add(cond);
				}
			}
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return ret;
	}

	public int insertRule(int baseId, String newName, List<Product> products) {
		//executeStatementParam("INSERT INTO tpl (tpl_descricao) VALUES (?);",rule.getName());
		int newId = executeStatementInt("SELECT MAX(tpl_id) FROM tpl;");
		executeStatementParam("INSERT INTO `tpl` (`tpl_id`,`tpl_descricao`,`tpl_itmserial`,`tpl_flagprg`)VALUES(" + newId + ", ?, 0, 2);", newName);
		executeStatementParam("INSERT INTO `tplitm` (SELECT " + newId + ", `tplitm_id`, `tplitm_descricao`, `tplitm_tblid`, `tplitm_tblattid`, `tplitm_operador`, `tplitm_tblcndid`, `tplitm_tblattcndid`, `tplitm_constante`, `tplitm_prior`, now(), now() FROM `tplitm` WHERE tpl_id = ?);", baseId + "");
		Iterator<Product> iter = products.iterator();
		for (int i = 0; iter.hasNext(); i++) {
			Product p = iter.next();
			int prio = (i + 1) * 10;
			int fabDays = executeStatementInt("SELECT MIN(l.vwlte_diasfabricado) FROM vwlte l INNER JOIN armloc_stk s ON s.lte_id = l.vwlte_id WHERE s.prd_id = '" + p.getId().toString() + "';");

			executeStatementParam("UPDATE `tpl` SET `tpl_itmserial` = (SELECT MAX(`tplitm_id`) FROM `tplitm` WHERE tpl_id = ?);", newId + "");
			executeStatementParam("INSERT INTO `tplitm` (`tpl_id`,`tplitm_id`,`tplitm_descricao`, `tplitm_tblid`, `tplitm_tblattid`, `tplitm_operador`, `tplitm_tblcndid`, `tplitm_tblattcndid`, `tplitm_constante`, `tplitm_prior`, `tplitm_insdt`, `tplitm_udpdt`) VALUES (" + newId + ",(SELECT IF(tpl_itmserial IS NULL,0,MAX(tpl_itmserial)) + 1 FROM tpl WHERE tpl_id = " + newId + "),'SKU', '29', '17', '=', '1', '1', ?, " + prio + ", now(), now());", p.getSku());
			executeStatementParam("UPDATE `tpl` SET `tpl_itmserial` = (SELECT MAX(`tplitm_id`) FROM `tplitm` WHERE tpl_id = ?);", newId + "");
			executeStatementParam("INSERT INTO `tplitm` (`tpl_id`,`tplitm_id`,`tplitm_descricao`, `tplitm_tblid`, `tplitm_tblattid`, `tplitm_operador`, `tplitm_tblcndid`, `tplitm_tblattcndid`, `tplitm_constante`, `tplitm_prior`, `tplitm_insdt`, `tplitm_udpdt`) VALUES (" + newId + ",(SELECT IF(tpl_itmserial IS NULL,0,MAX(tpl_itmserial)) + 1 FROM tpl WHERE tpl_id = " + newId + "),'FAB', '33', '10', '<=', '1', '2', ?, " + prio + ", now(), now());", fabDays + "");
		}

		return newId;
	}

	private int executeStatementInt(String sql) {
		int ret = 0;

		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			try (ResultSet rs = st.executeQuery();) {
				if (rs.next()) {
					ret = rs.getInt(1) + 1;
				}
			}
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public WMSRule findRule(int prgid, int ruleId) {
		StringBuilder sql = new StringBuilder("SELECT ");
		WMSRule ret = new WMSRule();

		sql.append(" tpl.tpl_id AS `id`,");
		sql.append(" tpl.tpl_descricao AS `desc`,");
		sql.append(" tbltplatt.coltab_tabdesc AS `lefttabdesc`,");
		sql.append(" coltplatt.coltabitm_coldesc AS `leftcoldesc`,");
		sql.append(" tplitm.tplitm_operador AS `operator`,");
		sql.append(" tbltplcnd.coltab_tabdesc AS `righttabdesc`,");
		sql.append(" coltplcnd.coltabitm_coldesc AS `rightcoldesc`,");
		sql.append(" tplitm.tplitm_constante AS `value`,");
		sql.append(" tplitm.tplitm_prior AS `priority`");
		sql.append("	FROM tpl ");
		sql.append("	INNER JOIN tplitm ON tpl.tpl_id = tplitm.tpl_id");
		sql.append("    INNER JOIN coltab tbltplatt ON tplitm_tblid = tbltplatt.coltab_id");
		sql.append("    INNER JOIN coltabitm coltplatt ON tbltplatt.coltab_id = coltplatt.coltab_id AND tplitm.tplitm_tblattid = coltplatt.coltabitm_id");
		sql.append("    INNER JOIN coltab tbltplcnd ON tplitm_tblcndid = tbltplcnd.coltab_id");
		sql.append("    INNER JOIN coltabitm coltplcnd ON tbltplcnd.coltab_id = coltplcnd.coltab_id AND tplitm.tplitm_tblattcndid = coltplcnd.coltabitm_id");
		sql.append("    WHERE tpl_flagprg = ?");
		sql.append("        AND tpl.tpl_id = ?");
		sql.append("    ORDER BY tpl.tpl_id, tplitm.tplitm_prior");

		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setInt(1, prgid);
			st.setInt(2, ruleId);
			try (ResultSet rs = st.executeQuery();) {
				int lastPriority = -1;

				while (rs.next()) {
					int priority = rs.getInt("priority");
					String strPriority = "";

					if (ret.getId() == 0) {
						int id = rs.getInt("id");
						ret.setId(id);
						ret.setName(rs.getString("desc"));
						ret.setConds(new ArrayList<String>());
						lastPriority = -1;
					}

					if (lastPriority == -1 || lastPriority != priority) {
						strPriority = priority + " - ";
						lastPriority = priority;
					} else {
						strPriority = ret.getConds().remove(ret.getConds().size() - 1);
						if (lastPriority != priority)
							strPriority += " OU ";
						else if (lastPriority == priority)
							strPriority += " E ";
					}
					//String cond = strPriority + rs.getString("lefttabdesc") + ".";
					String cond = strPriority;

					cond += rs.getString("leftcoldesc");
					cond += " " + rs.getString("operator") + " ";
					if (!rs.getString("righttabdesc").equals("Constante")) {
						//cond += rs.getString("righttabdesc") + ".";
						cond += rs.getString("rightcoldesc");
					} else
						cond += rs.getString("value");
					ret.getConds().add(cond);
				}
			}
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return ret;
	}

	public void clearTnpSibs(String tnpId, boolean updateEnt, boolean updateExt, boolean resetEnt, boolean resetExt) {
		if (updateEnt || updateExt) {
			StringBuilder toDel = new StringBuilder("DELETE tt, mtr, mt, ts FROM tnp");

			toDel.append("	LEFT JOIN movord m ON m.movord_parentid = tnp.tnp_id");
			toDel.append("	LEFT JOIN movordtransport mtr ON mtr.movord_id = m.movord_id");
			toDel.append("	LEFT JOIN movordthings mt ON mtr.movordtransport_thingid = mt.movordthings_id AND mtr.movord_id = mt.movord_id");
			toDel.append("  LEFT JOIN tnpthings tt ON tt.tnpthings_idref = tnp.tnp_id AND mtr.movordtransport_thingid = tt.tnpthings_id");
			toDel.append("  LEFT JOIN tnpsib ts ON ts.tnp_id = tnp.tnp_id AND ts.tnp_siblingsid = m.movord_id");
			toDel.append("  WHERE movord_status = 'ATIVO'");
			if (updateEnt && updateExt)
				toDel.append(" AND (movord_entsai = 'E' OR movord_entsai = 'S')");
			else if (updateEnt)
				toDel.append(" AND movord_entsai = 'E'");
			else if (updateExt)
				toDel.append(" AND movord_entsai = 'S'");
			toDel.append(" AND tnp.tnp_id = ?;");
			executeStatementParam(toDel.toString(), tnpId);

			toDel = new StringBuilder("DELETE m FROM movord m");

			toDel.append("  WHERE movord_status = 'ATIVO'");
			if (updateEnt && updateExt)
				toDel.append(" AND (movord_entsai = 'E' OR movord_entsai = 'S')");
			else if (updateEnt)
				toDel.append(" AND movord_entsai = 'E'");
			else if (updateExt)
				toDel.append(" AND movord_entsai = 'S'");
			toDel.append(" AND movord_parentid = ?;");
			System.out.println("Remove Movimentacoes" + toDel.toString() + " - " + tnpId);
			executeStatementParam(toDel.toString(), tnpId);
		}

		if (resetEnt || resetExt) {
			StringBuilder toDel = new StringBuilder("DELETE FROM tnpsib WHERE tnp_id = ?");

			if (resetEnt && resetExt)
				toDel.append(" AND (tnp_siblingsnfent = 1 OR tnp_siblingsnfsaida = 1) ");
			else if (resetEnt)
				toDel.append(" AND tnp_siblingsnfent = 1 ");
			else if (resetExt)
				toDel.append(" AND tnp_siblingsnfsaida = 1 ");
			System.out.println("Remove NFs" + toDel.toString() + " - " + tnpId);
			executeStatementParam(toDel.toString(), tnpId);
			if (resetEnt) executeStatementParam("UPDATE tnp SET tnp_nfent = 1 WHERE tnp.tnp_id = ?;", tnpId);
			if (resetExt) executeStatementParam("UPDATE tnp SET tnp_nfsaida = 1 WHERE tnp.tnp_id = ?;", tnpId);
		}
	}

	public void removePallet(Thing ts) {
		logger.debug("osgthings " + executeStatementParam("DELETE FROM osgthings WHERE osgthings_thingsid = ?;", ts.getId().toString()));
		logger.debug("crioirdthings " + executeStatementParam("DELETE FROM criordthings WHERE criordthings_id = ?;", ts.getId().toString()));
		logger.debug("aporncthings " + executeStatementParam("DELETE FROM aporncthings WHERE aporn_things_id = ?;", ts.getId().toString()));
		logger.debug("apoavariathings " + executeStatementParam("DELETE FROM apoavariathings WHERE things_id = ?;", ts.getId().toString()));
		logger.debug("movordtransport " + executeStatementParam("DELETE FROM movordtransport WHERE movordtransport_thingid = ?;", ts.getId().toString()));
		logger.debug("conordconord_items " + executeStatementParam("DELETE FROM conordconord_items WHERE conorditems_thingid = ?;", ts.getId().toString()));
		logger.debug("tnpthings " + executeStatementParam("DELETE FROM tnpthings WHERE tnpthings_id = ?;", ts.getId().toString()));
		logger.debug("thingssib " + executeStatementParam("DELETE FROM thingssib WHERE things_id = ?;", ts.getId().toString()));
		logger.debug("thingsunits " + executeStatementParam("DELETE FROM thingsunits WHERE things_id = ?;", ts.getId().toString()));
		logger.debug("stk " + executeStatementParam("DELETE FROM stk WHERE things_id = ?;", ts.getId().toString()));
		logger.debug("things " + executeStatementParam("DELETE FROM things WHERE things_id = ?;", ts.getId().toString()));
	}

	private int executeStatementParam(String sql, String param) {
		int ret = 0;

		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, param);
			ret = st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public List<UUID> findAllocation(UUID destinationId) {
		StringBuilder sql = new StringBuilder("SELECT ");
		List<UUID> ret = new ArrayList<>();

		sql.append(" address_id FROM stk WHERE armloc_id = ? AND stk_alocacao = 1");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, destinationId.toString());
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next())
					ret.add(UUID.fromString(rs.getString(1)));
			}
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return ret;
	}

	public List<UUID> listAllocationsByAddress(UUID address_id) {
		StringBuilder sql = new StringBuilder("SELECT ");
		List<UUID> ret = new ArrayList<>();

		sql.append(" things_id FROM stk WHERE armloc_id = ? AND stk_alocacao = 1");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, address_id.toString());
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next())
					ret.add(UUID.fromString(rs.getString(1)));
			}
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return ret;
	}

	public List<UUID> findFIFOByProduct(UUID productId, int quantity, int minResupply) {
		List<UUID> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT s.things_id ");

		sql.append("FROM stk s");
		sql.append(" INNER JOIN armlocaddress a ON s.address_id = a.address_id");
		sql.append(" INNER JOIN armloc l ON s.armloc_id = l.armloc_id");
		sql.append(" INNER JOIN arm w ON l.arm_id = w.arm_id");
		sql.append(" INNER JOIN lte ON s.lte_id = lte.lte_id ");
		sql.append(" INNER JOIN things t ON s.things_id = t.things_id");
		sql.append(" INNER JOIN (SELECT `armloc_id`, COUNT(DISTINCT `address_id`) AS `ocup` FROM `stk` GROUP BY `armloc_id`) o ON o.armloc_id = l.armloc_id");
		sql.append(" WHERE s.prd_id = ?");
		sql.append(" AND w.arm_metaname = 'WAREHOUSE'");
		sql.append(" AND s.stk_alocacao = 3 ");
		sql.append(" AND t.things_status != 'BLOQUEADO'");
		sql.append(" AND lte.lte_validade >= (CURRENT_TIMESTAMP + INTERVAL ? DAY)");
		sql.append(" ORDER BY lte.lte_validade ASC, CAST((l.armloc_capacit - o.ocup) / l.armloc_capacit * 100 AS SIGNED INTEGER) DESC, l.armloc_capacit ASC, l.armloc_end DESC, a.address_ordenacao DESC");
		sql.append("	LIMIT ?;");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, productId.toString());
			st.setInt(2, minResupply);
			st.setInt(3, quantity);
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next())
					ret.add(UUID.fromString(rs.getString(1)));
			}
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return ret;
	}

	public List<AddressOcupationStub> listStock() {
		List<AddressOcupationStub> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT ");

		sql.append(" MAX(a.armloc_id) AS address_id, MAX(a.armloc_status) AS status, MAX(a.armloc_end) AS name, IF(s.stk_id IS NULL, '', GROUP_CONCAT(DISTINCT CONCAT(p.prd_sku, ' - ', p.prd_name) ORDER BY p.prd_name SEPARATOR ',')) AS products, GROUP_CONCAT(DISTINCT t.things_status ORDER BY t.things_status SEPARATOR ',') AS prdstatus, MAX(a.armloc_capacit) AS capacity, IF(s.stk_id IS NULL, MAX(a.armloc_capacit), MIN(a.armloc_capacit - o.ocup)) AS free ");
		sql.append("	FROM armloc a ");
		sql.append("    LEFT JOIN stk s ON s.armloc_id = a.armloc_id");
		sql.append("    LEFT JOIN (SELECT armloc_id, COUNT(DISTINCT things_id) AS ocup FROM stk GROUP BY armloc_id) o ON a.armloc_id = o.armloc_id");
		sql.append("    LEFT JOIN things t ON s.things_id = t.things_id");
		sql.append("    LEFT JOIN prd p ON s.prd_id = p.prd_id");
		sql.append("    GROUP BY a.armloc_id");
		sql.append("	ORDER BY a.armloc_end; ");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					AddressOcupationStub aos = new AddressOcupationStub();

					aos.setAddress_id(UUID.fromString(rs.getString("address_id")));
					aos.setCapacity(rs.getInt("capacity"));
					aos.setFree(rs.getInt("free"));
					aos.setName(rs.getString("name"));
					aos.setProducts(rs.getString("products"));
					aos.setStatus(rs.getString("status"));
					aos.setProductStatus(rs.getString("prdstatus"));
					ret.add(aos);
				}
			}
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return ret;
	}

	public Map<Integer, List<UUID>> listAddresStock(UUID address_id) {
		Map<Integer, List<UUID>> ret = new HashMap<>();
		StringBuilder sql = new StringBuilder("SELECT ");

		sql.append(" stk_alocacao, things_id FROM stk WHERE armloc_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, address_id.toString());
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					int alloc = rs.getInt(1);
					List<UUID> thList = ret.containsKey(alloc) ? ret.get(alloc) : new ArrayList<>();

					thList.add(UUID.fromString(rs.getString(2)));
					ret.put(alloc, thList);
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return ret;
	}

	public Map<Integer, List<UUID>> listDockStock() {
		Map<Integer, List<UUID>> ret = new HashMap<>();
		StringBuilder sql = new StringBuilder("SELECT ");

		sql.append(" s.stk_alocacao, s.things_id FROM stk s ");
		sql.append(" INNER JOIN armloc a ON s.armloc_id = a.armloc_id ");
		sql.append(" INNER JOIN arm w ON a.arm_id = w.arm_id");
		sql.append(" INNER JOIN things wt ON s.things_id = wt.things_parent_id ");
		sql.append(" WHERE w.arm_metaname = 'DOCK_WH' AND s.stk_alocacao = 3 AND wt.things_status NOT IN ('SEPARADO','VARIADO','TEMPORARIO','PROVISORIO');");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					int alloc = rs.getInt(1);
					List<UUID> thList = ret.containsKey(alloc) ? ret.get(alloc) : new ArrayList<>();

					thList.add(UUID.fromString(rs.getString(2)));
					ret.put(alloc, thList);
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return ret;
	}

	public List<UUID> listNewestPalletsProduct(UUID prdId, int quantity) {
		List<UUID> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT ");

		sql.append(" things_id FROM stk s");
		sql.append(" INNER JOIN armlocaddress a ON s.address_id = a.address_id");
		sql.append(" INNER JOIN armloc r ON s.armloc_id = r.armloc_id");
		sql.append(" INNER JOIN lte l ON s.lte_id = l.lte_id");
		sql.append(" WHERE s.prd_id = ?");
		sql.append(" ORDER BY l.lte_fabricacao DESC, r.armloc_end ASC, a.address_ordenacao DESC");
		sql.append(" LIMIT ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, prdId.toString());
			st.setInt(2, quantity);
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					ret.add(UUID.fromString(rs.getString(1)));
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public UUID getNextAddress(String addressId) {
		UUID ret = null;
		StringBuilder sql = new StringBuilder("SELECT ");

		sql.append(" next.address_id FROM armlocaddress curr");
		sql.append(" INNER JOIN armlocaddress next ON curr.armloc_id = next.armloc_id");
		sql.append(" WHERE curr.address_id = ?");
		sql.append("  AND next.address_side = curr.address_side");
		sql.append("  AND next.address_ordenacao > curr.address_ordenacao");
		sql.append(" ORDER BY next.address_ordenacao");
		sql.append(" LIMIT 1");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, addressId);
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					ret = UUID.fromString(rs.getString(1));
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public void updateOrdMov(UUID movId, int seq, UUID thingId, UUID originId, UUID destId) {
		StringBuilder sql = new StringBuilder("INSERT INTO movordtransport");

		sql.append(" (movord_id, movordtransport_id, movordtransport_originid, movordtransport_addressid, movordtransport_thingid)");
		sql.append(" VALUES (?,?,?,?,?)");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, movId.toString());
			st.setInt(2, seq);
			st.setString(3, thingId.toString());
			st.setString(4, originId.toString());
			st.setString(5, destId.toString());
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void updateStkMov(UUID thingId, UUID destParentId, UUID destId, String lte_id, UUID productId, int qtd) {
		executeStatementParam("UPDATE stk SET stk_alocacao = 2, stk_qtd_reservado = stk_qtd_fisica, stk_qtd_fisica = 0 WHERE things_id = ?", thingId.toString());
		StringBuilder sql = new StringBuilder("INSERT INTO stk");

		sql.append(" (armloc_id, address_id, things_id, lte_id, prd_id, stk_alocacao, stk_qtd_reservado, stk_qtd_alocada, stk_qtd_fisica, stk_insdt, stk_udpdt)");
		sql.append(" VALUES (?,?,?,?,?,1,0,0,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, destParentId.toString());
			st.setString(2, destId.toString());
			st.setString(3, thingId.toString());
			st.setString(4, lte_id);
			st.setString(5, productId.toString());
			st.setInt(6, qtd);
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void insertLte(String lte_id, Date man, Date exp) {
		StringBuilder sql = new StringBuilder("INSERT IGNORE INTO lte ");

		sql.append(" (lte_id, lte_nome, lte_fabricacao, lte_validade, lte_totfabricado)");
		sql.append(" VALUES (?, 'LOTE',?,?,1)");
		sql.append(" ON DUPLICATE KEY UPDATE lte_totfabricado = lte_totfabricado + 1");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, lte_id);
			st.setTimestamp(2, new Timestamp(man.getTime()));
			st.setTimestamp(3, new Timestamp(exp.getTime()));
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void insertStk(UUID thingId, UUID destParentId, UUID destId, String lte_id, UUID productId, BigDecimal qtd, Date man, Date exp) {
		insertLte(lte_id, man, exp);
		StringBuilder sql = new StringBuilder("INSERT INTO stk");

		sql.append(" (armloc_id, address_id, things_id, lte_id, prd_id, stk_alocacao, stk_qtd_reservado, stk_qtd_alocada, stk_qtd_fisica, stk_insdt, stk_udpdt)");
		sql.append(" VALUES (?,?,?,?,?,3,0,0,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, destParentId.toString());
			st.setString(2, destId.toString());
			st.setString(3, thingId.toString());
			st.setString(4, lte_id);
			st.setString(5, productId.toString());
			st.setBigDecimal(6, qtd);
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public List<StockDueDateJson> listStockDateByProduct(String prd_id) {
		List<StockDueDateJson> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT `p`.`prd_sku` AS `SKU`, `p`.`prd_name` AS `PRODUCT`, `inv`.`status` AS `STATUS`, GROUP_CONCAT(DISTINCT `r`.`armloc_end` ORDER BY `r`.`armloc_end` SEPARATOR ', ') AS `ROADS`, `inv`.`fabricacao` AS `MAN`, `inv`.`vencimento` AS `EXP`, `inv`.`diasfabricado` AS `FAB`, `inv`.`validade` AS `DAYS`, COUNT(`inv`.`pallet`) AS `PALLETS`, SUM(`inv`.`quantidade`) AS `CAIXAS`");

		sql.append(" FROM ( SELECT `m`.`arm_id` AS `armazem`, `a`.`armloc_id` AS `rua`, `l`.`address_id` AS `endereco`, `s`.`things_id` AS `pallet`, `l`.`address_ordenacao` AS `address_ordenacao`, `s`.`lte_id` AS `lote`, `s`.`prd_id` AS `produto`, `v`.`vwlte_fabricacao` AS `fabricacao`, `v`.`vwlte_validade` AS `vencimento`, `v`.`vwlte_diasvalidade` AS `validade`, `v`.`vwlte_diasfabricado` AS `diasfabricado`, SUM((`s`.`stk_qtd_reservado` + `s`.`stk_qtd_fisica`)) AS `quantidade`, `t`.`things_status` AS `status` FROM ((`armloc` `a` JOIN `arm` `m`) JOIN (`stk` `s` LEFT JOIN `armlocaddress` `l` ON ((`s`.`address_id` = `l`.`address_id`)))) JOIN `vwlte` `v` ON `s`.`lte_id` = `v`.`vwlte_id` JOIN `things` `t` ON `s`.`things_id` = `t`.`things_id` WHERE ((`a`.`armloc_id` = `s`.`armloc_id`) AND (`m`.`arm_id` = `a`.`arm_id`)) AND `s`.`stk_alocacao` <> 1 AND LENGTH(`v`.`vwlte_id`) < 20 AND `s`.`stk_alocacao` = 3 GROUP BY `a`.`armloc_id` , `s`.`address_id` ORDER BY `l`.`address_ordenacao` ) `inv`");
		sql.append(" INNER JOIN `arm` `a` ON `inv`.`armazem` = `a`.`arm_id` ");
		sql.append(" INNER JOIN `armloc` `r` ON `inv`.`rua` = `r`.`armloc_id` ");
		sql.append(" INNER JOIN `prd` `p` ON `inv`.`produto` = `p`.`prd_id` ");
		sql.append(" INNER JOIN `modprd` `tp` ON `p`.`prd_modprd_id` = `tp`.`modprd_id` ");
		sql.append(" WHERE `tp`.`modprd_namerecursive` LIKE '%|#PA#|%' ");
		sql.append("  AND `a`.`arm_metaname` = 'WAREHOUSE' ");
		sql.append("  AND `p`.`prd_id` = ? ");
		sql.append(" GROUP BY `p`.`prd_id`, `inv`.`lote`, `inv`.`status`");
		sql.append(" ORDER BY `p`.`prd_sku`, `inv`.`validade`");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, prd_id);
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					StockDueDateJson sdd = new StockDueDateJson();

					sdd.setSku(rs.getString("SKU"));
					sdd.setName(rs.getString("PRODUCT"));
					sdd.setAddr(rs.getString("ROADS"));
					sdd.setMan(rs.getTimestamp("MAN"));
					sdd.setExp(rs.getTimestamp("EXP"));
					sdd.setFab(rs.getInt("FAB"));
					sdd.setDue(rs.getInt("DAYS"));
					sdd.setCount(rs.getInt("PALLETS"));
					sdd.setStatus(rs.getString("STATUS"));
					ret.add(sdd);
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public Map<String, Integer> createStkSnapshot(String doc_id, String whereAddress) {
		StringBuilder sql = new StringBuilder("INSERT INTO stk_snapshot ");

		sql.append("(SELECT ?, s.armloc_id, s.prd_id, COUNT(DISTINCT s.things_id) AS `qtd`, now(), 'SNAPSHOT' FROM stk s");
		sql.append(" WHERE stk_alocacao != 1");
		if (whereAddress != null)
			sql.append(" AND s.armloc_id IN (" + whereAddress + ") ");
		sql.append("   GROUP BY s.armloc_id)");
		sql.append(" ON DUPLICATE KEY UPDATE `things_count` = values(`things_count`);");
		executeStatementParam(sql.toString(), doc_id);
		return listStkSnapshotByDocument(doc_id).stream().collect(Collectors.groupingBy(WMSStkSnapshot::getProduct_id, Collectors.summingInt(WMSStkSnapshot::getCount)));
	}

	public void insertStkInventoryCount(String doc_id, String armloc_id, String prd_id, int qtd) {
		StringBuilder sql = new StringBuilder("INSERT INTO stk_snapshot (doc_id, armloc_id, prd_id, things_count, insdt, snapshot_type)");

		sql.append("VALUES(?, ?, ?, ?, now(), 'INVENTORY')");
		sql.append(" ON DUPLICATE KEY UPDATE `things_count` = values(`things_count`);");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, doc_id);
			st.setString(2, armloc_id);
			st.setString(3, prd_id);
			st.setInt(4, qtd);
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public List<WMSStkSnapshot> listStkSnapshotByDocument(String doc_id) {
		List<WMSStkSnapshot> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT a.armloc_end AS `ADDR`, a.armloc_id AS `ADDRID`, p.prd_sku AS `SKU`, p.prd_name AS `PRD`, p.prd_id AS `PRDID`, ss.things_count AS `CNT`, ss.insdt AS `INSDT`, ss.snapshot_type AS `TYPE` FROM stk_snapshot ss ");

		sql.append(" INNER JOIN armloc a ON ss.armloc_id = a.armloc_id ");
		sql.append(" INNER JOIN prd p ON ss.prd_id = p.prd_id ");
		sql.append(" WHERE ss.doc_id = ? ");
		sql.append(" ORDER BY a.armloc_end;");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, doc_id);
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					WMSStkSnapshot ss = new WMSStkSnapshot();

					ss.setAddress(rs.getString("ADDR"));
					ss.setAddress_id(rs.getString("ADDRID"));
					ss.setSku(rs.getString("SKU"));
					ss.setProduct(rs.getString("PRD"));
					ss.setProduct_id(rs.getString("PRDID"));
					ss.setCount(rs.getInt("CNT"));
					ss.setDate(rs.getTimestamp("INSDT"));
					ss.setType(rs.getString("TYPE"));
					ret.add(ss);
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public List<String> listPickingByProduct(UUID prdId) {
		List<String> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT s.address_id FROM stk s");

		sql.append("	INNER JOIN armlocaddress d ON s.address_id = d.address_id");
		sql.append("	INNER JOIN armloc l ON s.armloc_id = l.armloc_id");
		sql.append("	INNER JOIN arm a ON l.arm_id = a.arm_id");
		sql.append("	WHERE a.arm_metaname = 'PICKING' AND s.prd_id = ?");
		sql.append("	ORDER BY s.stk_qtd_fisica DESC, d.address_ordenacao DESC, l.armloc_end ASC");
		sql.append("	LIMIT 1;");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, prdId.toString());
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					ret.add(rs.getString(1));
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public List<String> listPickingStages(int qtdPallets) {
		List<String> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT a.address_id FROM armlocaddress a");

		sql.append(" INNER JOIN armloc l ON a.armloc_id = l.armloc_id");
		sql.append(" LEFT JOIN stk s ON s.address_id = a.address_id");
		sql.append(" WHERE l.armloc_metaname = 'DOCK' ");
		sql.append("  AND s.stk_id IS NULL");
		sql.append(" ORDER BY address_ordenacao");
		sql.append(" LIMIT ?;");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setInt(1, qtdPallets);
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					ret.add(rs.getString(1));
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public List<String> findDestResupply(UUID prdId) {
		List<String> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT address_id FROM armloc_stk");

		sql.append(" WHERE arm_metaname = 'PICKING' ");
		sql.append("   AND prd_id = ? ");
		sql.append(" ORDER BY stk_qtd_fisica ASC, armloc_end ASC");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, prdId.toString());
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					ret.add(rs.getString(1));
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public List<String> findOngoingResupply(UUID prdId) {
		List<String> ret = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT s.address_id FROM stk s");

		sql.append(" INNER JOIN armloc l ON s.armloc_id = l.armloc_id");
		sql.append(" INNER JOIN arm w ON l.arm_id = w.arm_id");
		sql.append(" WHERE w.arm_metaname = 'PICKING' ");
		sql.append(" AND s.prd_id = ?");
		sql.append(" AND s.stk_alocacao = 1");
		sql.append(" ORDER BY s.stk_qtd_fisica ASC, l.armloc_end ASC;");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setString(1, prdId.toString());
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					ret.add(rs.getString(1));
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public Map<String, Double> needsResupply(UUID prdId, double qty) {
		Map<String, Double> ret = new HashMap<>();
		StringBuilder sql = new StringBuilder("SELECT s.stk_qtd_fisica <= ?, s.address_id, s.stk_qtd_fisica");

		sql.append("	FROM stk s");
		sql.append("	INNER JOIN armloc l ON s.armloc_id = l.armloc_id");
		sql.append("	INNER JOIN arm w ON l.arm_id = w.arm_id");
		sql.append("	INNER JOIN prd ON s.prd_id = prd.prd_id");
		sql.append("		WHERE w.arm_metaname = 'PICKING'");
		sql.append("			AND s.prd_id = ?");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.FETCH_FORWARD);) {
			st.setDouble(1, qty);
			st.setString(2, prdId.toString());
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next()) {
					if (rs.getBoolean(1))
						ret.put(rs.getString(2), rs.getBigDecimal(3).doubleValue());
				}
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());

			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public void updatePickingResupply(UUID originId, UUID destId, String quantity, String lote) {
		StringBuilder sql = new StringBuilder("UPDATE stk ");

		sql.append(" SET things_id = '").append(destId).append("'");
		sql.append(" , lte_id = '").append(lote).append("'");
		sql.append(" , stk_qtd_fisica = ?");
		sql.append(" WHERE things_id = '").append(originId).append("'");
		sql.append(" AND stk_alocacao = 3");
		executeStatementParam(sql.toString(), quantity);
	}

	public void updateStkPicking(String thingId, BigDecimal qty) {
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement("UPDATE stk SET stk_qtd_fisica = ? WHERE things_id = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setBigDecimal(1, qty);
			st.setString(2, thingId);
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public int removeWmsMov(String movordId) {
		String delStkDest = "DELETE FROM stk WHERE stk_alocacao = 1 AND things_id IN (SELECT movordtransport_thingid FROM movordtransport WHERE movord_id = ?)";
		String updStkOri = "UPDATE stk SET stk_alocacao = 3, stk_qtd_fisica = stk_qtd_reservado, stk_qtd_reservado = 0 WHERE stk_alocacao = 2 AND things_id IN (SELECT movordtransport_thingid FROM movordtransport WHERE movord_id = ?)";
		String delMovordTransp = "DELETE FROM movordtransport WHERE movord_id = ?";
		String delMovordThings = "DELETE FROM movordthings WHERE movord_id = ?";
		String delMovord = "DELETE FROM movord WHERE movord_id = ?";

		System.out.println("Delete Stk1 =" + executeStatementParam(delStkDest, movordId));
		System.out.println("Update Stk2 =" + executeStatementParam(updStkOri, movordId));
		System.out.println("Delete movordtransprot =" + executeStatementParam(delMovordTransp, movordId));
		System.out.println("Delete movordthings =" + executeStatementParam(delMovordThings, movordId));
		return executeStatementParam(delMovord, movordId);
	}

	public int cancelMovByThing(String thingId) {
		String delStkDest = "DELETE FROM stk WHERE stk_alocacao = 1 AND things_id = ?";
		String updStkOri = "UPDATE stk SET stk_alocacao = 3, stk_qtd_fisica = stk_qtd_reservado, stk_qtd_reservado = 0 WHERE stk_alocacao = 2 AND things_id = ?";

		executeStatementParam(updStkOri, thingId);
		return executeStatementParam(delStkDest, thingId);
	}

	public void deleteStkThingAlocacao(String thingId, int alocacao) {
		String delStk = "DELETE FROM stk WHERE things_id = ?";

		if (alocacao >= 0) {
			delStk += " AND stk_alocacao = " + alocacao;
		}
		executeStatementParam(delStk, thingId);
	}

	public void resupplyStk(String thingId, String lot, String qty) {
		String delStk = "UPDATE stk ";

		delStk += " SET stk_alocacao = 3, ";
		delStk += "  lte_id = '" + lot + "',";
		delStk += "  stk_qtd_alocada = 0, ";
		delStk += "  stk_qtd_reservado = 0, ";
		delStk += "  stk_qtd_fisica = " + qty;
		delStk += " WHERE things_id = ?";
		delStk += " AND stk_alocacao = 1";
		executeStatementParam(delStk, thingId);
	}

	public void changeStkAlocacao(String thingId, int alocacao, int alocacaoTo) {
		if (alocacao != alocacaoTo) {
			String delStk = "UPDATE stk ";

			delStk += " SET stk_alocacao = " + alocacaoTo;
			if (alocacao == 1 && alocacaoTo == 2)
				delStk += ", stk_qtd_reservado = stk_qtd_alocada, stk_qtd_alocada = 0";
			else if (alocacao == 1 && alocacaoTo == 3)
				delStk += ", stk_qtd_fisica = stk_qtd_alocada, stk_qtd_alocada = 0";
			else if (alocacao == 2 && alocacaoTo == 3)
				delStk += ", stk_qtd_fisica = stk_qtd_reservado, stk_qtd_reservado = 0";
			else if (alocacao == 3 && alocacaoTo == 2)
				delStk += ", stk_qtd_reservado = stk_qtd_fisica, stk_qtd_fisica = 0";
			else if (alocacao == 3 && alocacaoTo == 1)
				delStk += ", stk_qtd_alocada = stk_qtd_fisica, stk_qtd_fisica = 0";
			else if (alocacao == 2 && alocacaoTo == 1)
				delStk += ", stk_qtd_alocada = stk_qtd_reservado, stk_qtd_reservado = 0";
			delStk += " WHERE things_id = ?";
			delStk += " AND stk_alocacao = " + alocacao;
			executeStatementParam(delStk, thingId);
		}
	}

	public void deleteStkThing(String thingId) {
		deleteStkThingAlocacao(thingId, -1);
	}

	public void clearStkByTnp(String tnpId) {
		StringBuilder delStk = new StringBuilder("DELETE s ");

		delStk.append(" FROM stk s ");
		delStk.append(" INNER JOIN tnpthings ON tnpthings.tnpthings_id = s.things_id");
		delStk.append(" WHERE tnpthings.tnpthings_docid = ?");
		delStk.append(" AND tnpthings.tnpthings_entsai = 'S'");
		StringBuilder delts = new StringBuilder("DELETE wts ");

		delts.append(" FROM thingssib wts ");
		delts.append(" INNER JOIN tnpthings ON tnpthings.tnpthings_id = wts.things_id");
		delts.append(" WHERE tnpthings.tnpthings_docid = ?");
		delts.append(" AND tnpthings.tnpthings_entsai = 'S'");

		StringBuilder delth = new StringBuilder("DELETE wt ");

		delth.append(" FROM things wt ");
		delth.append(" INNER JOIN tnpthings ON tnpthings.tnpthings_id = wt.things_id OR tnpthings.tnpthings_id = wt.things_parent_id");
		delth.append(" WHERE tnpthings.tnpthings_docid = ?");
		delth.append(" AND tnpthings.tnpthings_entsai = 'S'");

		StringBuilder delTnpTh = new StringBuilder("DELETE FROM tnpthings");
		delTnpTh.append(" WHERE tnpthings.tnpthings_docid = ?");

		executeStatementParam(delStk.toString(), tnpId);
		executeStatementParam(delts.toString(), tnpId);
		executeStatementParam(delth.toString(), tnpId);
		executeStatementParam(delTnpTh.toString(), tnpId);
	}

	public double getAddressQuantity(String dst) {
		double ret = 0d;
		String sql = "SELECT SUM(stk_qtd_fisica) AS `qt` FROM stk WHERE address_id = ? GROUP BY address_id";

		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, dst);
			try (ResultSet rs = st.executeQuery();) {
				if (rs.next()) ret = rs.getDouble("qt");
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
		return ret;
	}

	public List<String> checkMinResupply() {
		List<String> prdList = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT p.prd_id AS `prd`, MIN(p.prd_resupply_qtd) AS `min`, MIN(s.stk_qtd_fisica) AS `qtd` FROM stk s ");

		sql.append("  INNER JOIN armloc r ON s.armloc_id = r.armloc_id");
		sql.append("  INNER JOIN arm w ON r.arm_id = w.arm_id");
		sql.append("  INNER JOIN prd p ON s.prd_id = p.prd_id");
		sql.append("  WHERE s.stk_qtd_fisica < p.prd_resupply_qtd");
		sql.append("    AND s.stk_alocacao = 3");
		sql.append("    AND w.arm_metaname = 'PICKING'");
		sql.append("  GROUP BY s.address_id;");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			try (ResultSet rs = st.executeQuery();) {
				while (rs.next())
					prdList.add(rs.getString("prd"));
				rs.close();
			}
			st.close();
			closeConnection(con);
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}

		return prdList;
	}

	public void insertOrUpdateThing(UUID thingId, UUID parentId, String name, Date createdAt, Date updatedAt, String status, UUID addId, UUID prdId, String lte_id, BigDecimal qtd, Date man, Date exp, BigDecimal strWeight, BigDecimal actWeight) {
		insertLte(lte_id, man, exp);
		StringBuilder sql = new StringBuilder("INSERT INTO things");
		//	'00069aa1-7e0c-4ce9-8742-3365a2883ee9', 'THINGS', '2020-12-17 17:37:27', '2020-12-17 17:37:27', 'ARMAZENADO', 'PALLET PBR RETORNAVEL', '', '95b564e9-ea5a-4caa-adbe-06fc7dd0b966', 'HWEB1712201000212', '2020-12-17 03:00:00', '2021-03-21 03:00:00', '6f35a7d4-98f1-11e9-815b-005056a19775', '1.0000', '0', '', '0'

		sql.append(" (things_id, things_metaname, things_created_at, things_updated_at, things_status, things_name, things_parent_id, things_prd_id, things_lote, things_lotefabric, things_lotevalidade, things_address_id, things_qtd, things_starting_weigth, things_user_id, things_actual_weight)");
		sql.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		sql.append(" ON DUPLICATE KEY UPDATE `things_status` = values(`things_status`), `things_address_id` = values(`things_address_id`), `things_qtd` = values(`things_qtd`), `things_actual_weight` = values(`things_actual_weight`);");
		try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
			st.setString(1, thingId.toString());
			st.setString(2, "THINGS");
			st.setTimestamp(3, new Timestamp(createdAt.getTime()));
			st.setTimestamp(4, new Timestamp(updatedAt.getTime()));
			st.setString(5, status);
			st.setString(6, name);
			st.setString(7, parentId == null ? "" : parentId.toString());
			st.setString(8, prdId == null ? "" : prdId.toString());
			st.setString(9, lte_id);
			st.setTimestamp(10, new Timestamp(man.getTime()));
			st.setTimestamp(11, new Timestamp(exp.getTime()));
			st.setString(12, addId == null ? "" : addId.toString());
			st.setBigDecimal(13, qtd);
			st.setBigDecimal(14, strWeight);
			st.setString(15, "");
			st.setBigDecimal(16, actWeight);
			st.executeUpdate();
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(e.getLocalizedMessage());
			re.initCause(e.getCause());
			re.setStackTrace(e.getStackTrace());
			e.printStackTrace();
			throw re;
		}
	}

	public void clearArmloc(String armlocId) {
		executeStatementParam("DELETE FROM thingssib WHERE things_id IN (SELECT things_id FROM stk WHERE armloc_id = ?);", armlocId);
		executeStatementParam("DELETE FROM thingsunits WHERE things_id IN (SELECT things_id FROM stk WHERE armloc_id = ?);", armlocId);
		executeStatementParam("DELETE FROM stk WHERE armloc_id = ?;", armlocId);
		executeStatementParam("DELETE wt FROM things wt INNER JOIN armlocaddress wa ON wt.things_address_id = wa.address_id WHERE wa.armloc_id = ?;", armlocId);
	}

	public boolean isMultiExiry(UUID armlocId) {
		if (armlocId != null) {
			StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT lte_validade) > 1 AS `multi` FROM stk s ");

			sql.append("  INNER JOIN lte l ON s.lte_id = l.lte_id ");
			sql.append("  WHERE armloc_id = ? ");
			sql.append("  GROUP BY armloc_id;");
			try (Connection con = initConnection(WMS_DATASOURCE); PreparedStatement st = con.prepareStatement(sql.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);) {
				boolean ret = false;

				st.setString(1, armlocId.toString());
				try (ResultSet rs = st.executeQuery();) {
					if (rs.next())
						ret = rs.getBoolean("multi");
					rs.close();
				}
				st.close();
				closeConnection(con);
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
