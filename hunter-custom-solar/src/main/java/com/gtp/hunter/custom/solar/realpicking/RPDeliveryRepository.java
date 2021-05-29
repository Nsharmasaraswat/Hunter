package com.gtp.hunter.custom.solar.realpicking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import com.gtp.hunter.common.manager.ConnectionManager;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class RPDeliveryRepository extends ConnectionManager {

	public List<RPDeliveryDTO> getDeliveryList(Date dt, String plant) throws SQLException {
		List<RPDeliveryDTO> ret = new ArrayList<RPDeliveryDTO>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat frm = new SimpleDateFormat("dd/MM/yyyy");

		try (Connection con = initConnection("realPicking"); PreparedStatement ps = con.prepareStatement(RealPicking.getDeliveryQuery());) {
			ps.setString(1, sdf.format(dt));
			ps.setString(2, plant);
			try (ResultSet rs = ps.executeQuery();) {

				while (rs.next()) {
					RPDeliveryDTO dtqr = new RPDeliveryDTO();

					dtqr.setLocation_id(rs.getString("LOCATION_ID"));
					dtqr.setTrip_number(rs.getString("TRIP_NUMBER"));
					dtqr.setDelivery_date(frm.format(rs.getTimestamp("DELIVERY_DATE")));
					dtqr.setVehicle_id(rs.getString("VEHICLE_ID"));
					dtqr.setVehicle_tag_number(rs.getString("VEHICLE_TAG_NUMBER"));
					dtqr.setVehicle_description(rs.getString("VEHICLE_DESCRIPTION"));
					dtqr.setLoad_id(rs.getString("LOAD_ID"));
					dtqr.setLoad_status(rs.getInt("LOAD_STATUS"));
					dtqr.setBay_id(rs.getInt("BAY_ID"));
					dtqr.setBay_label(rs.getString("BAY_LABEL"));
					dtqr.setPicking_ticket_message(rs.getString("PICKING_TICKET_MESSAGE"));
					dtqr.setCases_physical(rs.getInt("CASES_PHYSICAL"));
					dtqr.setNumber_of_skus(rs.getInt("NUMBER_OF_SKUS"));
					dtqr.setSequence_ticket(rs.getInt("SEQUENCE_TICKET"));
					dtqr.setProduct_id(rs.getString("PRODUCT_ID"));
					dtqr.setProduct_description_long(rs.getString("PRODUCT_DESCRIPTION_LONG"));
					dtqr.setProduct_description_short(rs.getString("PRODUCT_DESCRIPTION_SHORT"));
					dtqr.setQuantity(rs.getInt("QUANTITY"));
					dtqr.setSeparator_after_ticket(rs.getBoolean("SEPARATOR_AFTER_TICKET"));
					dtqr.setLayer_description(rs.getString("LAYER_DESCRIPTION"));
					dtqr.setHighlight(rs.getBoolean("HIGHLIGHT"));
					dtqr.setIs_full_pallet(rs.getBoolean("IS_FULL_PALLET"));
					ret.add(dtqr);
				}
			}
		}

		return ret;
	}

	public List<RPTripDTO> getTripList(Date dt, String plant) throws SQLException {
		List<RPTripDTO> ret = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat frm = new SimpleDateFormat("dd/MM/yyyy");

		try (Connection con = initConnection("realPicking"); PreparedStatement ps = con.prepareStatement(RealPicking.getTripQuery());) {
			ps.setString(1, sdf.format(dt));
			ps.setString(2, plant);
			try (ResultSet rs = ps.executeQuery();) {

				while (rs.next()) {
					RPTripDTO dtqr = new RPTripDTO();

					dtqr.setLocation_id(rs.getString("LOCATION_ID"));
					dtqr.setTrip_number(rs.getString("TRIP_NUMBER"));
					dtqr.setDelivery_date(frm.format(rs.getTimestamp("DELIVERY_DATE")));
					dtqr.setVehicle_id(rs.getString("VEHICLE_ID"));
					dtqr.setVehicle_tag_number(rs.getString("VEHICLE_TAG_NUMBER"));
					dtqr.setVehicle_description(rs.getString("VEHICLE_DESCRIPTION"));
					dtqr.setLoad_id(rs.getString("LOAD_ID"));
					dtqr.setLoad_status(rs.getInt("LOAD_STATUS"));
					dtqr.setCases_physical(rs.getDouble("CASES_PHYSICAL"));
					dtqr.setPallets(rs.getInt("PALLETS"));
					dtqr.setFull_pallets(rs.getInt("FULL_PALLETS"));
					ret.add(dtqr);
				}
			}
		}

		return ret;
	}

	public List<RPPickDTO> listPicksByTrip(String trip, String plant) throws SQLException {
		List<RPPickDTO> ret = new ArrayList<>();
		SimpleDateFormat frm = new SimpleDateFormat("dd/MM/yyyy");

		try (Connection con = initConnection("realPicking"); PreparedStatement ps = con.prepareStatement(RealPicking.getPickQuery());) {
			ps.setString(1, trip);
			ps.setString(2, plant);
			try (ResultSet rs = ps.executeQuery();) {

				while (rs.next()) {
					RPPickDTO dtqr = new RPPickDTO();

					dtqr.setLocation_id(rs.getString("LOCATION_ID"));
					dtqr.setTrip_number(rs.getString("TRIP_NUMBER"));
					dtqr.setDelivery_date(frm.format(rs.getTimestamp("DELIVERY_DATE")));
					dtqr.setLoad_id(rs.getString("LOAD_ID"));
					dtqr.setBay_id(rs.getInt("BAY_ID"));
					dtqr.setBay_label(rs.getString("BAY_LABEL"));
					dtqr.setPicking_ticket_message(rs.getString("PICKING_TICKET_MESSAGE"));
					dtqr.setCases_physical(rs.getInt("CASES_PHYSICAL"));
					dtqr.setNumber_of_skus(rs.getInt("NUMBER_OF_SKUS"));
					dtqr.setIs_full_pallet(rs.getBoolean("IS_FULL_PALLET"));
					dtqr.setConatiner_id(rs.getString("CONTAINER_ID"));
					dtqr.setContainer_levels(rs.getInt("CONTAINER_LEVELS"));
					ret.add(dtqr);
				}
			}
		}

		return ret;
	}

	public List<RPPickItemDTO> listPickingItems(String trip, String plant, String bayLabel) throws SQLException {
		List<RPPickItemDTO> ret = new ArrayList<RPPickItemDTO>();

		try (Connection con = initConnection("realPicking"); PreparedStatement ps = con.prepareStatement(RealPicking.getPickItemsQuery());) {
			ps.setString(1, trip);
			ps.setString(2, bayLabel);
			ps.setString(3, plant);
			try (ResultSet rs = ps.executeQuery();) {

				while (rs.next()) {
					RPPickItemDTO dtqr = new RPPickItemDTO();

					dtqr.setTrip_number(rs.getString("TRIP_NUMBER"));
					dtqr.setLoad_id(rs.getString("LOAD_ID"));
					dtqr.setBay_id(rs.getInt("BAY_ID"));
					dtqr.setSequence_ticket(rs.getInt("SEQUENCE_TICKET"));
					dtqr.setProduct_id(rs.getString("PRODUCT_ID"));
					dtqr.setProduct_description_long(rs.getString("PRODUCT_DESCRIPTION_LONG"));
					dtqr.setProduct_description_short(rs.getString("PRODUCT_DESCRIPTION_SHORT"));
					dtqr.setQuantity(rs.getInt("QUANTITY"));
					dtqr.setSeparator_after_ticket(rs.getBoolean("SEPARATOR_AFTER_TICKET"));
					dtqr.setLayer_description(rs.getString("LAYER_DESCRIPTION"));
					dtqr.setHighlight(rs.getBoolean("HIGHLIGHT"));
					dtqr.setIs_full_pallet(rs.getBoolean("IS_FULL_PALLET"));
					ret.add(dtqr);
				}
			}
		}

		return ret;
	}

	public List<RPContainerItemDTO> listContainerItems(String trip, String plant, String bayLabel) throws SQLException {
		List<RPContainerItemDTO> ret = new ArrayList<RPContainerItemDTO>();

		try (Connection con = initConnection("realPicking"); PreparedStatement ps = con.prepareStatement(RealPicking.getContainerItemsQuery());) {
			ps.setString(1, trip);
			ps.setString(2, bayLabel);
			ps.setString(3, plant);
			try (ResultSet rs = ps.executeQuery();) {

				while (rs.next()) {
					RPContainerItemDTO dtqr = new RPContainerItemDTO();

					dtqr.setTrip_number(rs.getString("TRIP_NUMBER"));
					dtqr.setLoad_id(rs.getString("LOAD_ID"));
					dtqr.setBay_id(rs.getInt("BAY_ID"));
					dtqr.setSequence_ticket(rs.getInt("SEQUENCE_TICKET"));
					dtqr.setProduct_id(rs.getString("PRODUCT_ID"));
					dtqr.setProduct_description_long(rs.getString("PRODUCT_DESCRIPTION_LONG"));
					dtqr.setProduct_description_short(rs.getString("PRODUCT_DESCRIPTION_SHORT"));
					dtqr.setQuantity(rs.getInt("QUANTITY"));
					dtqr.setSeparator_after_ticket(rs.getBoolean("SEPARATOR_AFTER_TICKET"));
					dtqr.setLayer_description(rs.getString("CONTAINER_LEVEL"));
					dtqr.setHighlight(rs.getBoolean("HIGHLIGHT"));
					dtqr.setIs_full_pallet(rs.getBoolean("IS_FULL_PALLET"));
					ret.add(dtqr);
				}
			}
		}
		return ret;
	}
}
