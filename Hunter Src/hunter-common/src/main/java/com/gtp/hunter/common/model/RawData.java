package com.gtp.hunter.common.model;

import java.util.UUID;

import com.google.gson.annotations.Expose;

public class RawData {

	public enum RawDataType {
		SENSOR, LOCATION, IDENT, STATUS;
	}

	@Expose
	private String		tagId;
	@Expose
	private RawDataType	type;
	@Expose
	private int			port;
	@Expose
	private UUID		device;
	@Expose
	private UUID		source;
	@Expose
	private Long		ts;
	@Expose
	private String		payload;

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public RawDataType getType() {
		return type;
	}

	public void setType(RawDataType type) {
		this.type = type;
	}

	public UUID getDevice() {
		return device;
	}

	public void setDevice(UUID device) {
		this.device = device;
	}

	public UUID getSource() {
		return source;
	}

	public void setSource(UUID source) {
		this.source = source;
	}

	public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
