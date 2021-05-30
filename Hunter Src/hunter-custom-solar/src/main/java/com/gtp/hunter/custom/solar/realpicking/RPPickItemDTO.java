package com.gtp.hunter.custom.solar.realpicking;

public class RPPickItemDTO {

	private String	trip_number;
	private String	load_id;
	private int		bay_id;
	private int		sequence_ticket;
	private String	product_id;
	private String	product_description_long;
	private String	product_description_short;
	private int		quantity;
	private boolean	separator_after_ticket;
	private String	layer_description;
	private boolean	highlight;
	private boolean	is_full_pallet;

	public String getTrip_number() {
		return trip_number;
	}

	public void setTrip_number(String trip_number) {
		this.trip_number = trip_number;
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
