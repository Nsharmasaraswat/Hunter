package com.gtp.hunter.custom.solar.realpicking;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.manager.ConnectionManager;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class RPProductRepository extends ConnectionManager {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public RPProductDTO getBySku(String sku) {
		return getByPlantAndSku(ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"), sku);
	}

	public RPProductDTO getByPlantAndSku(String plant, String sku) {
		RPProductDTO ret = null;
		String sql = "SELECT * FROM (" + RealPicking.getProductQuery() + ") dt WHERE pPRODUCT_ID = ?";

		try (Connection con = initConnection("realPicking");) {
			if (con == null)
				logger.error("Connection is null. Cant get realPickingConnection");
			if (con != null) {
				try (PreparedStatement ps = con.prepareStatement(sql);) {
					ps.setString(1, plant);
					ps.setString(2, plant);
					ps.setString(3, sku);
					try (ResultSet rs = ps.executeQuery();) {
						if (rs.next()) {
							ret = buildObject(rs);
						}
						rs.close();
					}
					ps.close();
				}
				con.close();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return ret;
	}

	public List<RPProductDTO> listProducts() {
		List<RPProductDTO> ret = new ArrayList<RPProductDTO>();

		try (Connection con = initConnection("realPicking"); PreparedStatement ps = con.prepareStatement(RealPicking.getProductQuery());) {
			ps.setString(1, ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
			ps.setString(2, ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					ret.add(buildObject(rs));
				}
				rs.close();
			}
			ps.close();
			con.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return ret;
	}

	private RPProductDTO buildObject(ResultSet rs) throws SQLException {
		RPProductDTO ret = new RPProductDTO();

		ret.setProduct_id(rs.getString("pPRODUCT_ID"));
		ret.setProduct_description_long(rs.getString("pPRODUCT_DESCRIPTION_LONG"));
		ret.setProduct_description_short(rs.getString("pPRODUCT_DESCRIPTION_SHORT"));
		ret.setPicking_quantity_description(rs.getString("pPICKING_QUANTITY_DESCRIPTION"));
		ret.setPackage_id(rs.getString("pmPACKAGE_ID"));
		ret.setPicking_unit_description(rs.getString("pmPICKING_UNIT_DESCRIPTION"));
		ret.setNumber_of_subunits(rs.getInt("pmNUMBER_OF_SUBUNITS"));
		ret.setSubunit_description(rs.getString("pmSUBUNIT_DESCRIPTION"));
		ret.setPackage_larger_side(rs.getDouble("pmPACKAGE_LARGER_SIDE"));
		ret.setPackage_smaller_side(rs.getDouble("pmPACKAGE_SMALLER_SIDE"));
		ret.setPackage_height(rs.getDouble("pmPACKAGE_HEIGHT"));
		ret.setPackage_weight(rs.getDouble("pmPACKAGE_WEIGHT"));
		ret.setQuantity_standard(rs.getInt("plQUANTITY_STANDARD"));
		ret.setPallet_height(rs.getDouble("plPALLET_HEIGHT"));
		return ret;
	}
}
