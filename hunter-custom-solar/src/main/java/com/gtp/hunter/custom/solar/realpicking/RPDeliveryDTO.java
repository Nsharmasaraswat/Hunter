package com.gtp.hunter.custom.solar.realpicking;

public class RPDeliveryDTO {

	private String	location_id;
	private String	trip_number;
	private String	delivery_date;
	private String	vehicle_id;
	private String	vehicle_tag_number;
	private String	vehicle_description;
	private String	load_id;
	private int		load_status;
	private int		bay_id;
	private String	bay_label;
	private String	picking_ticket_message;
	private int		cases_physical;
	private int		number_of_skus;
	private int		sequence_ticket;
	private String	product_id;
	private String	product_description_long;
	private String	product_description_short;
	private int		quantity;
	private boolean	separator_after_ticket;
	private String	layer_description;
	private boolean	highlight;
	private boolean	is_full_pallet;

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

	public int getLoad_status() {
		return load_status;
	}

	public void setLoad_status(int load_status) {
		this.load_status = load_status;
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

	public int getSequence_ticket() {
		return sequence_ticket;
	}

	public void setSequence_ticket(int sequence_ticket) {
		this.sequence_ticket = sequence_ticket;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public String getProduct_description_long() {
		return product_description_long;
	}

	public void setProduct_description_long(String product_description_long) {
		this.product_description_long = product_description_long;
	}

	public String getProduct_description_short() {
		return product_description_short;
	}

	public void setProduct_description_short(String product_description_short) {
		this.product_description_short = product_description_short;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public boolean isSeparator_after_ticket() {
		return separator_after_ticket;
	}

	public void setSeparator_after_ticket(boolean separator_after_ticket) {
		this.separator_after_ticket = separator_after_ticket;
	}

	public String getLayer_description() {
		return layer_description;
	}

	public void setLayer_description(String layer_description) {
		this.layer_description = layer_description;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	public boolean is_full_pallet() {
		return is_full_pallet;
	}

	public void setIs_full_pallet(boolean is_full_pallet) {
		this.is_full_pallet = is_full_pallet;
	}

}
