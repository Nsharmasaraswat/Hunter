package com.gtp.hunter.ui.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.devices.BaseDevice;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.process.model.Feature;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.wf.origin.DeviceOrigin;
import com.gtp.hunter.process.wf.origin.PortOrigin;

public class ViewFullOrigin {
	@Expose
	@SerializedName("purposes")
	private List<Purpose>		purposeList;

	@Expose
	@SerializedName("features")
	private List<Feature>		featureList;

	@Expose
	@SerializedName("sources")
	private List<Source>		sourceList;

	@Expose
	@SerializedName("devices")
	private List<BaseDevice>	deviceList;

	@Expose
	@SerializedName("ports")
	private List<Port>			portList;

	public ViewFullOrigin(DeviceOrigin dOrigin) {
		this.purposeList = new ArrayList<>(dOrigin.getParams().getPurposes());
		this.featureList = new ArrayList<>(dOrigin.getParams().getFeatures());
		this.sourceList = new ArrayList<>(dOrigin.getSources().values());
		this.deviceList = new ArrayList<>(dOrigin.getDevices().values());
		this.portList = new ArrayList<>(dOrigin.getPorts().values());
	}

	public ViewFullOrigin(PortOrigin dOrigin) {
		this.purposeList = new ArrayList<>(dOrigin.getParams().getPurposes());
		this.featureList = new ArrayList<>(dOrigin.getParams().getFeatures());
		this.sourceList = new ArrayList<>(dOrigin.getSources().values());
		this.deviceList = new ArrayList<>(dOrigin.getDevices().values());
		this.portList = new ArrayList<>(dOrigin.getPorts().values());
	}

	/**
	 * @return the purposeList
	 */
	public List<Purpose> getPurposeList() {
		return purposeList;
	}

	/**
	 * @param purposeList the purposeList to set
	 */
	public void setPurposeList(List<Purpose> purposeList) {
		this.purposeList = purposeList;
	}

	/**
	 * @return the featureList
	 */
	public List<Feature> getFeatureList() {
		return featureList;
	}

	/**
	 * @param featureList the featureList to set
	 */
	public void setFeatureList(List<Feature> featureList) {
		this.featureList = featureList;
	}

	/**
	 * @return the sourceList
	 */
	public List<Source> getSourceList() {
		return sourceList;
	}

	/**
	 * @param sourceList the sourceList to set
	 */
	public void setSourceList(List<Source> sourceList) {
		this.sourceList = sourceList;
	}

	/**
	 * @return the deviceList
	 */
	public List<BaseDevice> getDeviceList() {
		return deviceList;
	}

	/**
	 * @param deviceList the deviceList to set
	 */
	public void setDeviceList(List<BaseDevice> deviceList) {
		this.deviceList = deviceList;
	}

	/**
	 * @return the portList
	 */
	public List<Port> getPortList() {
		return portList;
	}

	/**
	 * @param portList the portList to set
	 */
	public void setPortList(List<Port> portList) {
		this.portList = portList;
	}
}
