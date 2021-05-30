package com.gtp.hunter.common.devicedata;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class LabelData implements DeviceData {

	@Expose
	private Map<String, String>	labelFields	= new HashMap<>();

	@Expose
	private String				maskName;

	/**
	 * @return the labelFields
	 */
	public Map<String, String> getLabelFields() {
		return labelFields;
	}

	/**
	 * @param labelFields
	 *            the labelFields to set
	 */
	public void setLabelFields(Map<String, String> labelFields) {
		this.labelFields = labelFields;
	}

	/**
	 * @return the maskName
	 */
	public String getMaskName() {
		return maskName;
	}

	/**
	 * @param maskName
	 *            the maskName to set
	 */
	public void setMaskName(String maskName) {
		this.maskName = maskName;
	}

	public boolean addField(String name, String value) {
		return labelFields.putIfAbsent(name, value) == null;
	}

	public boolean removeField(String name) {
		return labelFields.remove(name) != null;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(this);
	}
}
