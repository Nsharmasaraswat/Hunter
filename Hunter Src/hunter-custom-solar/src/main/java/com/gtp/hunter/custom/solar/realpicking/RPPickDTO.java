package com.gtp.hunter.custom.solar.realpicking;

public class RPPickDTO {

	private String	location_id;
	private String	trip_number;
	private String	delivery_date;
	private String	load_id;
	private int		bay_id;
	private String	bay_label;
	private String	picking_ticket_message;
	private int		cases_physical;
	private int		number_of_skus;
	private boolean	is_full_pallet;
	private String	conatiner_id;
	private int		container_levels;

	/**
	 * @return the location_id
	 */
	public String getLocation_id() {
		return location_id;
	}

	/**
	 * @param location_id the location_id to set
	 */
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

	public String getLoad_id() {
		return load_id;
	}

	public void setLoad_id(String load_id) {
		this.load_id = load_id;
	}

	public int getBay_id() {
		return bay_id;
	}

	public void setBay_id(int bay_id) {
		this.bay_id = bay_id;
	}

	public String getBay_label() {
		return bay_label;
	}

	public void setBay_label(String bay_label) {
		this.bay_label = bay_label;
	}

	public String getPicking_ticket_message() {
		return picking_ticket_message;
	}

	public void setPicking_ticket_message(String picking_ticket_message) {
		this.picking_ticket_message = picking_ticket_message;
	}

	public int getCases_physical() {
		return cases_physical;
	}

	public void setCases_physical(int cases_physical) {
		this.cases_physical = cases_physical;
	}

	public int getNumber_of_skus() {
		return number_of_skus;
	}

	public void setNumber_of_skus(int number_of_skus) {
		this.number_of_skus = number_of_skus;
	}

	/**
	 * @return the is_full_pallet
	 */
	public boolean is_full_pallet() {
		return is_full_pallet;
	}

	/**
	 * @param is_full_pallet the is_full_pallet to set
	 */
	public void setIs_full_pallet(boolean is_full_pallet) {
		this.is_full_pallet = is_full_pallet;
	}

	/**
	 * @return the conatiner_id
	 */
	public String getConatiner_id() {
		return conatiner_id;
	}

	/**
	 * @param conatiner_id the conatiner_id to set
	 */
	public void setConatiner_id(String conatiner_id) {
		this.conatiner_id = conatiner_id;
	}

	/**
	 * @return the container_levels
	 */
	public int getContainer_levels() {
		return container_levels;
	}

	/**
	 * @param container_levels the container_levels to set
	 */
	public void setContainer_levels(int container_levels) {
		this.container_levels = container_levels;
	}

	/**
	 * @return the is_full_pallet
	 */
	public boolean isIs_full_pallet() {
		return is_full_pallet;
	}
}
