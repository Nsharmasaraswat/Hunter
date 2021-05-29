package com.gtp.hunter.process.jsonstubs;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.common.model.RawData;
import com.gtp.hunter.common.model.RawData.RawDataType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLRawData<T> {
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
	private T			payload;

	public AGLRawData() {

	}

	public AGLRawData(RawData rd) {
		this.tagId = rd.getTagId();
		this.type = rd.getType();
		this.port = rd.getPort();
		this.device = rd.getDevice();
		this.source = rd.getSource();
		this.ts = rd.getTs();
	}

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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}
}
