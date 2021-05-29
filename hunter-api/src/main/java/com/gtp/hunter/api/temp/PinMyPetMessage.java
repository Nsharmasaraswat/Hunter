package com.gtp.hunter.api.temp;

import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PinMyPetMessage {

	@Expose
	@SerializedName("protocol-version")
	String							protocolVersion;

	@Expose
	@SerializedName("device-imei")
	String							deviceIMEI;

	@Expose
	@SerializedName("firmware-version")
	String							firmwareVersion;

	@Expose
	@SerializedName("hardware-version")
	String							hardwareVersion;

	@Expose
	@SerializedName("location-data")
	String							locationData;

	@Expose
	@SerializedName("accelerometer-data")
	String							accelerometerData;

	@Expose
	@SerializedName("satellite-count")
	String							satelliteCount;

	@Expose
	@SerializedName("battery-voltage")
	String							batteryVoltage;

	@Expose
	@SerializedName("charge")
	int								charge;

	@Expose
	@SerializedName("gprs-rssi")
	int								gprsRSSI;

	@Expose
	@SerializedName("sim-iccid")
	String							simICCID;

	@Expose
	@SerializedName("connection-type")
	int								connectionType;
	@Expose
	@SerializedName("last-command")
	String							lastCommand;

	@Expose
	@SerializedName("safe-area")
	int								safeArea;

	@Inject
	private static transient Logger	logger;

	boolean							valid;

	@SuppressWarnings("unused")
	private PinMyPetMessage() {
	}

	public PinMyPetMessage(String data) {
		try {
			String[] dataArray = data.split(Pattern.quote("|"));

			setProtocolVersion(dataArray[0]);
			setDeviceIMEI(dataArray[1]);
			setFirmwareVersion(dataArray[2]);
			setHardwareVersion(dataArray[3]);
			setSatelliteCount(dataArray[4]);
			setLocationData(dataArray[5]);
			setAccelerometerData(dataArray[6]);
			setBatteryVoltage(dataArray[7]);
			setCharge(Integer.parseInt(dataArray[8]));
			setSimICCID(dataArray[9]);
			setGprsRSSI(Integer.parseInt(dataArray[10].isEmpty() ? "0" : dataArray[0]));
			setConnectionType(Integer.parseInt(dataArray[11]));
			setLastCommand(dataArray[12]);
			setSafeArea(Integer.parseInt(dataArray[13].isEmpty() ? "0" : dataArray[13]));
			setValid(true);
		} catch (Exception e) {
			logger.debug(data == null ? "Null Data" : "Invalid Data: " + data);
			setProtocolVersion("");
			setDeviceIMEI("");
			setFirmwareVersion("");
			setHardwareVersion("");
			setLocationData("");
			setAccelerometerData("");
			setSatelliteCount("");
			setBatteryVoltage("");
			setCharge(0);
			setGprsRSSI(0);
			setSimICCID("");
			setConnectionType(0);
			setLastCommand("");
			setSafeArea(0);
			setValid(false);
		}
	}

	/**
	 * @return the protocolVersion
	 */
	public String getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * @param protocolVersion
	 *            the protocolVersion to set
	 */
	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	/**
	 * @return the deviceIMEI
	 */
	public String getDeviceIMEI() {
		return deviceIMEI;
	}

	/**
	 * @param deviceIMEI
	 *            the deviceIMEI to set
	 */
	public void setDeviceIMEI(String deviceIMEI) {
		this.deviceIMEI = deviceIMEI;
	}

	/**
	 * @return the firmwareVersion
	 */
	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	/**
	 * @param firmwareVersion
	 *            the firmwareVersion to set
	 */
	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	/**
	 * @return the hardwareVersion
	 */
	public String getHardwareVersion() {
		return hardwareVersion;
	}

	/**
	 * @param hardwareVersion
	 *            the hardwareVersion to set
	 */
	public void setHardwareVersion(String hardwareVersion) {
		this.hardwareVersion = hardwareVersion;
	}

	/**
	 * @return the locationData
	 */
	public String getLocationData() {
		return locationData;
	}

	/**
	 * @param locationData
	 *            the locationData to set
	 */
	public void setLocationData(String locationData) {
		this.locationData = locationData;
	}

	/**
	 * @return the accelerometerData
	 */
	public String getAccelerometerData() {
		return accelerometerData;
	}

	/**
	 * @param accelerometerData
	 *            the accelerometerData to set
	 */
	public void setAccelerometerData(String accelerometerData) {
		this.accelerometerData = accelerometerData;
	}

	/**
	 * @return the satelliteCount
	 */
	public String getSatelliteCount() {
		return satelliteCount;
	}

	/**
	 * @param satelliteCount
	 *            the satelliteCount to set
	 */
	public void setSatelliteCount(String satelliteCount) {
		this.satelliteCount = satelliteCount;
	}

	/**
	 * @return the batteryVoltage
	 */
	public String getBatteryVoltage() {
		return batteryVoltage;
	}

	/**
	 * @param batteryVoltage
	 *            the batteryVoltage to set
	 */
	public void setBatteryVoltage(String batteryVoltage) {
		this.batteryVoltage = batteryVoltage;
	}

	/**
	 * @return the charge
	 */
	public int getCharge() {
		return charge;
	}

	/**
	 * @param charge
	 *            the charge to set
	 */
	public void setCharge(int charge) {
		this.charge = charge;
	}

	/**
	 * @return the gprsRSSI
	 */
	public int getGprsRSSI() {
		return gprsRSSI;
	}

	/**
	 * @param gprsRSSI
	 *            the gprsRSSI to set
	 */
	public void setGprsRSSI(int gprsRSSI) {
		this.gprsRSSI = gprsRSSI;
	}

	/**
	 * @return the simICCID
	 */
	public String getSimICCID() {
		return simICCID;
	}

	/**
	 * @param simICCID
	 *            the simICCID to set
	 */
	public void setSimICCID(String simICCID) {
		this.simICCID = simICCID;
	}

	/**
	 * @return the connection
	 */
	public int getConnectionType() {
		return connectionType;
	}

	/**
	 * @param connection
	 *            the connection to set
	 */
	public void setConnectionType(int connectionType) {
		this.connectionType = connectionType;
	}

	/**
	 * @return the lastCommand
	 */
	public String getLastCommand() {
		return lastCommand;
	}

	/**
	 * @param lastCommand
	 *            the lastCommand to set
	 */
	public void setLastCommand(String lastCommand) {
		this.lastCommand = lastCommand;
	}

	/**
	 * @return the safeArea
	 */
	public int getSafeArea() {
		return safeArea;
	}

	/**
	 * @param safeArea
	 *            the safeArea to set
	 */
	public void setSafeArea(int safeArea) {
		this.safeArea = safeArea;
	}

	@Override
	public String toString() {
		return new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @param valid
	 *            the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

}
