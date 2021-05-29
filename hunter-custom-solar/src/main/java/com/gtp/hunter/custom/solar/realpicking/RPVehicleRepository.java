package com.gtp.hunter.custom.solar.realpicking;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.manager.ConnectionManager;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class RPVehicleRepository extends ConnectionManager {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public RPVehicleDTO getVehicleCapacity(String truckId) {
		RPVehicleDTO ret = null;
		String sql = RealPicking.getVehicleCapacityQuery();

		try (Connection con = initConnection("realPicking");) {
			if (con == null)
				logger.error("Connection is null. Cant get realPickingConnection");
			if (con != null) {
				try (PreparedStatement ps = con.prepareStatement(sql);) {
					ps.setString(1, truckId);
					try (ResultSet rs = ps.executeQuery();) {
						if (rs.next()) {
							ret = new RPVehicleDTO();
							ret.setVehicleId(rs.getString("VEHICLE_ID"));
							ret.setCapacity(rs.getInt("BAY_COUNT"));
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
}
