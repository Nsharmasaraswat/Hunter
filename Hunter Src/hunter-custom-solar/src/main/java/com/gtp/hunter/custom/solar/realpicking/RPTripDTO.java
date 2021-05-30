package com.gtp.hunter.custom.solar.realpicking;

import java.util.HashSet;
import java.util.Set;

public class RPTripDTO {

	private String			location_id;
	private String			trip_number;
	private String			delivery_date;
	private String			vehicle_id;
	private String			vehicle_tag_number;
	private String			vehicle_description;
	private String			load_id;
	private int				load_status;
	private double			cases_physical;
	private int				pallets;
	private int				full_pallets;
	private Set<RPPickDTO>	picks	= new HashSet<>();

	public String getLocation_id() {
		return location_id;
	}

	public void setLocation_id(String location_id) {
		this.location_id = location_id;
	}

	public String getTrip_number() {
		return trip_number;
	}

	public void setTrip_number(String trip_number) {
		this.trip_number = trip_number;
	}

	public String getDelivery_date() {
		return delivery_date;
	}

	public void setDelivery_date(String delivery_date) {
		this.delivery_date = delivery_date;
	}

	public String getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(String vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public String getVehicle_tag_number() {
		return vehicle_tag_number;
	}

	public void setVehicle_tag_number(String vehicle_tag_number) {
		this.vehicle_tag_number = vehicle_tag_number;
	}

	public String getVehicle_description() {
		return vehicle_description;
	}

	public void setVehicle_description(String vehicle_description) {
		this.vehicle_description = vehicle_description;
	}

	public String getLoad_id() {
		return load_id;
	}

	public void setLoad_id(String load_id) {
		this.load_id = load_id;
	}

	/**
	 * @return the load_status
	 */
	public int getLoad_status() {
		return load_status;
	}

	/**
	 * @param load_status the load_status to set
	 */
	public void setLoad_status(int load_status) {
		this.load_status = load_status;
	}

	/**
	 * @return the cases_physical
	 */
	public double getCases_physical() {
		return cases_physical;
	}

	/**
	 * @param cases_physical the cases_physical to set
	 */
	public void setCases_physical(double cases_physical) {
		this.cases_physical = cases_physical;
	}

	/**
	 * @return the pallets
	 */
	public int getPallets() {
		return pallets;
	}

	/**
	 * @param pallets the pallets to set
	 */
	public void setPallets(int pallets) {
		this.pallets = pallets;
	}

	/**
	 * @return the full_pallets
	 */
	public int getFull_pallets() {
		return full_pallets;
	}

	/**
	 * @param full_pallets the full_pallets to set
	 */
	public void setFull_pallets(int full_pallets) {
		this.full_pallets = full_pallets;
	}

	/**
	 * @return the picks
	 */
	public Set<RPPickDTO> getPicks() {
		return picks;
	}

	/**
	 * @param picks the picks to set
	 */
	public void setPicks(Set<RPPickDTO> picks) {
		this.picks = picks;
	}
}
