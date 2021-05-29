package com.gtp.hunter.custom.solar.realpicking;

public class RPProductDTO {

	private String	product_id;
	private String	product_description_long;
	private String	product_description_short;
	private String	package_id;
	private String	picking_quantity_description;
	private String	picking_unit_description;
	private Integer	number_of_subunits;
	private String	subunit_description;
	private Double	package_larger_side;
	private Double	package_smaller_side;
	private Double	package_height;
	private Double	package_weight;
	private Integer	quantity_standard;
	private Double	pallet_height;

	/**
	 * @return the product_id
	 */
	public String getProduct_id() {
		return product_id;
	}

	/**
	 * @param product_id the product_id to set
	 */
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	/**
	 * @return the product_description_long
	 */
	public String getProduct_description_long() {
		return product_description_long;
	}

	/**
	 * @param product_description_long the product_description_long to set
	 */
	public void setProduct_description_long(String product_description_long) {
		this.product_description_long = product_description_long;
	}

	/**
	 * @return the product_description_short
	 */
	public String getProduct_description_short() {
		return product_description_short;
	}

	/**
	 * @param product_description_short the product_description_short to set
	 */
	public void setProduct_description_short(String product_description_short) {
		this.product_description_short = product_description_short;
	}

	/**
	 * @return the package_id
	 */
	public String getPackage_id() {
		return package_id;
	}

	/**
	 * @param package_id the package_id to set
	 */
	public void setPackage_id(String package_id) {
		this.package_id = package_id;
	}

	/**
	 * @return the picking_quantity_description
	 */
	public String getPicking_quantity_description() {
		return picking_quantity_description;
	}

	/**
	 * @param picking_quantity_description the picking_quantity_description to set
	 */
	public void setPicking_quantity_description(String picking_quantity_description) {
		this.picking_quantity_description = picking_quantity_description;
	}

	/**
	 * @return the picking_unit_description
	 */
	public String getPicking_unit_description() {
		return picking_unit_description;
	}

	/**
	 * @param picking_unit_description the picking_unit_description to set
	 */
	public void setPicking_unit_description(String picking_unit_description) {
		this.picking_unit_description = picking_unit_description;
	}

	/**
	 * @return the number_of_subunits
	 */
	public int getNumber_of_subunits() {
		return number_of_subunits;
	}

	/**
	 * @param number_of_subunits the number_of_subunits to set
	 */
	public void setNumber_of_subunits(int number_of_subunits) {
		this.number_of_subunits = number_of_subunits;
	}

	/**
	 * @return the subunit_description
	 */
	public String getSubunit_description() {
		return subunit_description;
	}

	/**
	 * @param subunit_description the subunit_description to set
	 */
	public void setSubunit_description(String subunit_description) {
		this.subunit_description = subunit_description;
	}

	/**
	 * @return the package_larger_side
	 */
	public Double getPackage_larger_side() {
		return package_larger_side;
	}

	/**
	 * @param package_larger_side the package_larger_side to set
	 */
	public void setPackage_larger_side(Double package_larger_side) {
		this.package_larger_side = package_larger_side;
	}

	/**
	 * @return the package_smaller_side
	 */
	public Double getPackage_smaller_side() {
		return package_smaller_side;
	}

	/**
	 * @param package_smaller_side the package_smaller_side to set
	 */
	public void setPackage_smaller_side(Double package_smaller_side) {
		this.package_smaller_side = package_smaller_side;
	}

	/**
	 * @return the package_height
	 */
	public Double getPackage_height() {
		return package_height;
	}

	/**
	 * @param package_height the package_height to set
	 */
	public void setPackage_height(Double package_height) {
		this.package_height = package_height;
	}

	/**
	 * @return the package_weight
	 */
	public Double getPackage_weight() {
		return package_weight;
	}

	/**
	 * @param package_weight the package_weight to set
	 */
	public void setPackage_weight(Double package_weight) {
		this.package_weight = package_weight;
	}

	/**
	 * @return the quantity_standard
	 */
	public Integer getQuantity_standard() {
		return quantity_standard;
	}

	/**
	 * @param quantity_standard the quantity_standard to set
	 */
	public void setQuantity_standard(Integer quantity_standard) {
		this.quantity_standard = quantity_standard;
	}

	/**
	 * @return the pallet_height
	 */
	public Double getPallet_height() {
		return pallet_height;
	}

	/**
	 * @param pallet_height the pallet_height to set
	 */
	public void setPallet_height(Double pallet_height) {
		this.pallet_height = pallet_height;
	}
}
