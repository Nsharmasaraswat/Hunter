package com.gtp.hunter.common.model;

import java.util.UUID;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DroneStatus {
	//ALL FIELDS ARE OBJECTS TO NULLIFY UNUSED ONES ON JSON
	@Expose
	@SerializedName("drone-id")
	private UUID	droneId;

	@Expose
	@SerializedName("vcc")
	private Integer	vcc;

	@Expose
	@SerializedName("servo-voltage")
	private Integer	vServo;

	@Expose
	@SerializedName("power-status")
	private String	powerStatus;

	@Expose
	@SerializedName("roll")
	private Float	roll;

	@Expose
	@SerializedName("roll-speed")
	private Float	rollSpeed;

	@Expose
	@SerializedName("pitch")
	private Float	pitch;

	@Expose
	@SerializedName("pitch-speed")
	private Float	pitchSpeed;

	@Expose
	@SerializedName("yaw")
	private Float	yaw;

	@Expose
	@SerializedName("yaw-speed")
	private Float	yawSpeed;

	@Expose
	@SerializedName("latitude")
	private Double	latitude;

	@Expose
	@SerializedName("ground-speed-x")
	private Double	groundSpeedX;

	@Expose
	@SerializedName("longitude")
	private Double	longitude;

	@Expose
	@SerializedName("ground-speed-y")
	private Double	groundSpeedY;

	@Expose
	@SerializedName("altitude")
	private Double	altitude;

	@Expose
	@SerializedName("ground-speed-z")
	private Double	groundSpeedZ;

	@Expose
	@SerializedName("relative-altitude")
	private Double	relativeAltitude;

	@Expose
	@SerializedName("text")
	private String	text;

	@Expose
	@SerializedName("severity")
	private String	severity;

	public DroneStatus(UUID id) {
		this.droneId = id;
	}

	/**
	 * @return the droneId
	 */
	public UUID getDroneId() {
		return droneId;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}

	/**
	 * @return the vcc
	 */
	public int getVcc() {
		return vcc;
	}

	/**
	 * @param vcc the vcc to set
	 */
	public void setVcc(int vcc) {
		this.vcc = vcc;
	}

	/**
	 * @return the vServo
	 */
	public int getvServo() {
		return vServo;
	}

	/**
	 * @param vServo the vServo to set
	 */
	public void setvServo(int vServo) {
		this.vServo = vServo;
	}

	/**
	 * @return the powerStatus
	 */
	public String getPowerStatus() {
		return powerStatus;
	}

	/**
	 * @param powerStatus the powerStatus to set
	 */
	public void setPowerStatus(String powerStatus) {
		this.powerStatus = powerStatus;
	}

	/**
	 * @return the roll
	 */
	public float getRoll() {
		return roll;
	}

	/**
	 * @param roll the roll to set
	 */
	public void setRoll(float roll) {
		this.roll = roll;
	}

	/**
	 * @return the rollSpeed
	 */
	public float getRollSpeed() {
		return rollSpeed;
	}

	/**
	 * @param rollSpeed the rollSpeed to set
	 */
	public void setRollSpeed(float rollSpeed) {
		this.rollSpeed = rollSpeed;
	}

	/**
	 * @return the pitch
	 */
	public float getPitch() {
		return pitch;
	}

	/**
	 * @param pitch the pitch to set
	 */
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	/**
	 * @return the pitchSpeed
	 */
	public float getPitchSpeed() {
		return pitchSpeed;
	}

	/**
	 * @param pitchSpeed the pitchSpeed to set
	 */
	public void setPitchSpeed(float pitchSpeed) {
		this.pitchSpeed = pitchSpeed;
	}

	/**
	 * @return the yaw
	 */
	public float getYaw() {
		return yaw;
	}

	/**
	 * @param yaw the yaw to set
	 */
	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	/**
	 * @return the yawSpeed
	 */
	public float getYawSpeed() {
		return yawSpeed;
	}

	/**
	 * @param yawSpeed the yawSpeed to set
	 */
	public void setYawSpeed(float yawSpeed) {
		this.yawSpeed = yawSpeed;
	}

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
	 * @return the groundSpeedX
	 */
	public double getGroundSpeedX() {
		return groundSpeedX;
	}

	/**
	 * @param groundSpeedX the groundSpeedX to set
	 */
	public void setGroundSpeedX(double groundSpeedX) {
		this.groundSpeedX = groundSpeedX;
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
	 * @return the groundSpeedY
	 */
	public double getGroundSpeedY() {
		return groundSpeedY;
	}

	/**
	 * @param groundSpeedY the groundSpeedY to set
	 */
	public void setGroundSpeedY(double groundSpeedY) {
		this.groundSpeedY = groundSpeedY;
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
	 * @return the groundSpeedZ
	 */
	public double getGroundSpeedZ() {
		return groundSpeedZ;
	}

	/**
	 * @param groundSpeedZ the groundSpeedZ to set
	 */
	public void setGroundSpeedZ(double groundSpeedZ) {
		this.groundSpeedZ = groundSpeedZ;
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
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the severity
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * @param droneId the droneId to set
	 */
	public void setDroneId(UUID droneId) {
		this.droneId = droneId;
	}
}
