package com.gtp.hunter.custom.solar.trixlog.dto;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class TrixlogAlert {
	@Expose
	private String				id;//":"5f12100d47ce1400018d26de",

	@Expose
	private String				eventName;//": "Harsh Acceleration",

	@Expose
	private String				vehicleCode;//":"820",

	@Expose
	private String				vehiclePlate;//":"AAA0820",

	@Expose
	private TrixlogOrganization	organization;//": {"id": 40"name": "Demo Organization"},

	@Expose
	private TrixlogDriver		driver;//":{"id":40,"name":"Daniel Alves","registration": "41634453077"},

	@Expose
	private TrixlogPosition		openedPosition;//":{  // tracker position when alert was opened"latitude":-22.9038645,"longitude":-43.3003699	   },

	@Expose
	private String				message;//":"Vehicle 820 has reached a speed above the limit"

	@Expose
	private Date				openedDate;//":"2017-07-01T00:00:04.000Z",

	@Expose
	private double				maxReadedValue;//":14.0,

	@Expose
	private Date				closedDate;//":"2017-07-01T00:01:03.000Z",

	@Expose
	private long				duration;//":59, // alert duration in seconds.

	@Expose
	private TrixlogStatistics	speedStatistics;//":{"min":14,"max":14,"avg":14.0},

	@Expose
	private TrixlogPosition		closedPosition;//":{  // tracker position when alert was closed"latitude":-22.9031605,"longitude":-43.30285}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @param eventName the eventName to set
	 */
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	/**
	 * @return the vehicleCode
	 */
	public String getVehicleCode() {
		return vehicleCode;
	}

	/**
	 * @param vehicleCode the vehicleCode to set
	 */
	public void setVehicleCode(String vehicleCode) {
		this.vehicleCode = vehicleCode;
	}

	/**
	 * @return the vehiclePlate
	 */
	public String getVehiclePlate() {
		return vehiclePlate;
	}

	/**
	 * @param vehiclePlate the vehiclePlate to set
	 */
	public void setVehiclePlate(String vehiclePlate) {
		this.vehiclePlate = vehiclePlate;
	}

	/**
	 * @return the organization
	 */
	public TrixlogOrganization getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(TrixlogOrganization organization) {
		this.organization = organization;
	}

	/**
	 * @return the driver
	 */
	public TrixlogDriver getDriver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
	 */
	public void setDriver(TrixlogDriver driver) {
		this.driver = driver;
	}

	/**
	 * @return the openedPosition
	 */
	public TrixlogPosition getOpenedPosition() {
		return openedPosition;
	}

	/**
	 * @param openedPosition the openedPosition to set
	 */
	public void setOpenedPosition(TrixlogPosition openedPosition) {
		this.openedPosition = openedPosition;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the openedDate
	 */
	public Date getOpenedDate() {
		return openedDate;
	}

	/**
	 * @param openedDate the openedDate to set
	 */
	public void setOpenedDate(Date openedDate) {
		this.openedDate = openedDate;
	}

	/**
	 * @return the maxReadedValue
	 */
	public double getMaxReadedValue() {
		return maxReadedValue;
	}

	/**
	 * @param maxReadedValue the maxReadedValue to set
	 */
	public void setMaxReadedValue(double maxReadedValue) {
		this.maxReadedValue = maxReadedValue;
	}

	/**
	 * @return the closedDate
	 */
	public Date getClosedDate() {
		return closedDate;
	}

	/**
	 * @param closedDate the closedDate to set
	 */
	public void setClosedDate(Date closedDate) {
		this.closedDate = closedDate;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * @return the speedStatistics
	 */
	public TrixlogStatistics getSpeedStatistics() {
		return speedStatistics;
	}

	/**
	 * @param speedStatistics the speedStatistics to set
	 */
	public void setSpeedStatistics(TrixlogStatistics speedStatistics) {
		this.speedStatistics = speedStatistics;
	}

	/**
	 * @return the closedPosition
	 */
	public TrixlogPosition getClosedPosition() {
		return closedPosition;
	}

	/**
	 * @param closedPosition the closedPosition to set
	 */
	public void setClosedPosition(TrixlogPosition closedPosition) {
		this.closedPosition = closedPosition;
	}
}
