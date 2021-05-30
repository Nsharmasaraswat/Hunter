package com.gtp.hunter.common.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocatePayload extends BasePayload {

	@Expose
	@SerializedName(value = "latitude")
	private double	latitude;

	@Expose
	@SerializedName(value = "longitude")
	private double	longitude;

	@Expose
	@SerializedName(value = "altitude")
	private double	altitude;

	@Expose
	@SerializedName(value = "x")
	private double	x;

	@Expose
	@SerializedName(value = "y")
	private double	y;

	@Expose
	@SerializedName(value = "z")
	private double	z;

	@Expose
	@SerializedName(value = "nearby")
	private String	nearbyAddress;

	@Expose
	@SerializedName(value = "nearby-name")
	private String	nearbyAddressName;

	@Expose
	@SerializedName(value = "relative-altitude")
	private double	relativeAltitude;

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude the altitude to set
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	/**
	 * @return the relativeAltitude
	 */
	public double getRelativeAltitude() {
		return relativeAltitude;
	}

	/**
	 * @param relativeAltitude the relativeAltitude to set
	 */
	public void setRelativeAltitude(double relativeAltitude) {
		this.relativeAltitude = relativeAltitude;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(double z) {
		this.z = z;
	}

	/**
	 * @return the nearbyAddress
	 */
	public String getNearbyAddress() {
		return nearbyAddress;
	}

	/**
	 * @param nearbyAddress the nearbyAddress to set
	 */
	public void setNearbyAddress(String nearbyAddress) {
		this.nearbyAddress = nearbyAddress;
	}

	/**
	 * @return the nearbyAddressName
	 */
	public String getNearbyAddressName() {
		return nearbyAddressName;
	}

	/**
	 * @param nearbyAddressName the nearbyAddressName to set
	 */
	public void setNearbyAddressName(String nearbyAddressName) {
		this.nearbyAddressName = nearbyAddressName;
	}

}