package com.gtp.hunter.custom.solar.trixlog.dto;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class TrixlogCoordinate {
	@Expose
	private int						vehicleId;

	@Expose
	private String					vehicleCode;

	@Expose
	private String					vehiclePlate;

	@Expose
	private Date					cDate;//":"2018-06-27T15:30:46.000Z", // date sent by tracker device.

	@Expose
	private Date					rDate;//":"2018-06-27T15:31:01.920Z", // date when coordinate was persisted in Trixlog's server.

	@Expose
	private TrixlogPosition			position;

	@Expose
	private long					odo;//":1750734, // odometer in meters

	@Expose
	private int						speed;//":8, // speed in Km/h

	@Expose
	private int						rpm;//":1502, // absolute value of RPM

	@Expose
	private boolean					ignition;//":true,

	@Expose
	private boolean					panic;//":false,

	@Expose
	private float					eqTemp;//":35.0, // internal temperature of the equipment

	@Expose
	private int						pCount;//":588, // tracker sequencial counter

	@Expose
	private TrixlogEquipmentModel	eqpMdl;//":"CALAMP", // equipment model

	@Expose
	private TrixlogOutput			outputs;

	@Expose
	private TrixlogInput			inputs;

	@Expose
	private double					totalEngineHour;//":107374182.35, // total accumulated engine hours

	@Expose
	private float					engineTemperature;//":81, // engine temperature in celsius degree.

	@Expose
	private boolean					engineFailure;//":false, // boolean that indicates if one or more engine code failure are present.

	@Expose
	private boolean					brakeStatus;//":false, // indicate if break pedal is pressed or not

	@Expose
	private float					accelPosition;//":60.0, // indicate the current position of the accelerator pedal - 0 is not pressed and 100 is totaly pressed.

	@Expose
	private float					fuelLevel;//":80.8, // percentage of fuel level

	@Expose
	private TrixlogDriver			driver;

	@Expose
	private boolean					extPower;//":true, // indicate that tracker is being powered by an external power

	@Expose
	private float					mainVolt;//":27.542, // vehicle battery voltage

	@Expose
	private float					co2emission;//" : 230.2 // it represents the co2 emission - in grams -  from the last coordinate to that one

	@Expose
	private TrixlogAccelerometer	accelerometer;

	/**
	 * @return the vehicleId
	 */
	public int getVehicleId() {
		return vehicleId;
	}

	/**
	 * @param vehicleId the vehicleId to set
	 */
	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
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
	 * @return the cDate
	 */
	public Date getcDate() {
		return cDate;
	}

	/**
	 * @param cDate the cDate to set
	 */
	public void setcDate(Date cDate) {
		this.cDate = cDate;
	}

	/**
	 * @return the rDate
	 */
	public Date getrDate() {
		return rDate;
	}

	/**
	 * @param rDate the rDate to set
	 */
	public void setrDate(Date rDate) {
		this.rDate = rDate;
	}

	/**
	 * @return the position
	 */
	public TrixlogPosition getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(TrixlogPosition position) {
		this.position = position;
	}

	/**
	 * @return the odo
	 */
	public long getOdo() {
		return odo;
	}

	/**
	 * @param odo the odo to set
	 */
	public void setOdo(long odo) {
		this.odo = odo;
	}

	/**
	 * @return the speed
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * @return the rpm
	 */
	public int getRpm() {
		return rpm;
	}

	/**
	 * @param rpm the rpm to set
	 */
	public void setRpm(int rpm) {
		this.rpm = rpm;
	}

	/**
	 * @return the ignition
	 */
	public boolean isIgnition() {
		return ignition;
	}

	/**
	 * @param ignition the ignition to set
	 */
	public void setIgnition(boolean ignition) {
		this.ignition = ignition;
	}

	/**
	 * @return the panic
	 */
	public boolean isPanic() {
		return panic;
	}

	/**
	 * @param panic the panic to set
	 */
	public void setPanic(boolean panic) {
		this.panic = panic;
	}

	/**
	 * @return the eqTemp
	 */
	public float getEqTemp() {
		return eqTemp;
	}

	/**
	 * @param eqTemp the eqTemp to set
	 */
	public void setEqTemp(float eqTemp) {
		this.eqTemp = eqTemp;
	}

	/**
	 * @return the pCount
	 */
	public int getpCount() {
		return pCount;
	}

	/**
	 * @param pCount the pCount to set
	 */
	public void setpCount(int pCount) {
		this.pCount = pCount;
	}

	/**
	 * @return the eqpMdl
	 */
	public TrixlogEquipmentModel getEqpMdl() {
		return eqpMdl;
	}

	/**
	 * @param eqpMdl the eqpMdl to set
	 */
	public void setEqpMdl(TrixlogEquipmentModel eqpMdl) {
		this.eqpMdl = eqpMdl;
	}

	/**
	 * @return the outputs
	 */
	public TrixlogOutput getOutputs() {
		return outputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputs(TrixlogOutput outputs) {
		this.outputs = outputs;
	}

	/**
	 * @return the inputs
	 */
	public TrixlogInput getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(TrixlogInput inputs) {
		this.inputs = inputs;
	}

	/**
	 * @return the totalEngineHour
	 */
	public double getTotalEngineHour() {
		return totalEngineHour;
	}

	/**
	 * @param totalEngineHour the totalEngineHour to set
	 */
	public void setTotalEngineHour(double totalEngineHour) {
		this.totalEngineHour = totalEngineHour;
	}

	/**
	 * @return the engineTemperature
	 */
	public float getEngineTemperature() {
		return engineTemperature;
	}

	/**
	 * @param engineTemperature the engineTemperature to set
	 */
	public void setEngineTemperature(float engineTemperature) {
		this.engineTemperature = engineTemperature;
	}

	/**
	 * @return the engineFailure
	 */
	public boolean isEngineFailure() {
		return engineFailure;
	}

	/**
	 * @param engineFailure the engineFailure to set
	 */
	public void setEngineFailure(boolean engineFailure) {
		this.engineFailure = engineFailure;
	}

	/**
	 * @return the brakeStatus
	 */
	public boolean isBrakeStatus() {
		return brakeStatus;
	}

	/**
	 * @param brakeStatus the brakeStatus to set
	 */
	public void setBrakeStatus(boolean brakeStatus) {
		this.brakeStatus = brakeStatus;
	}

	/**
	 * @return the accelPosition
	 */
	public float getAccelPosition() {
		return accelPosition;
	}

	/**
	 * @param accelPosition the accelPosition to set
	 */
	public void setAccelPosition(float accelPosition) {
		this.accelPosition = accelPosition;
	}

	/**
	 * @return the fuelLevel
	 */
	public float getFuelLevel() {
		return fuelLevel;
	}

	/**
	 * @param fuelLevel the fuelLevel to set
	 */
	public void setFuelLevel(float fuelLevel) {
		this.fuelLevel = fuelLevel;
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
	 * @return the extPower
	 */
	public boolean isExtPower() {
		return extPower;
	}

	/**
	 * @param extPower the extPower to set
	 */
	public void setExtPower(boolean extPower) {
		this.extPower = extPower;
	}

	/**
	 * @return the mainVolt
	 */
	public float getMainVolt() {
		return mainVolt;
	}

	/**
	 * @param mainVolt the mainVolt to set
	 */
	public void setMainVolt(float mainVolt) {
		this.mainVolt = mainVolt;
	}

	/**
	 * @return the co2emission
	 */
	public float getCo2emission() {
		return co2emission;
	}

	/**
	 * @param co2emission the co2emission to set
	 */
	public void setCo2emission(float co2emission) {
		this.co2emission = co2emission;
	}

	/**
	 * @return the accelerometer
	 */
	public TrixlogAccelerometer getAccelerometer() {
		return accelerometer;
	}

	/**
	 * @param accelerometer the accelerometer to set
	 */
	public void setAccelerometer(TrixlogAccelerometer accelerometer) {
		this.accelerometer = accelerometer;
	}
}
