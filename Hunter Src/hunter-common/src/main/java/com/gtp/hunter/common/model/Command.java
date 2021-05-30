package com.gtp.hunter.common.model;

import java.util.UUID;

import com.google.gson.annotations.Expose;

public class Command {

	@Expose
	private UUID	id;

	@Expose
	private UUID	source;

	@Expose
	private UUID	device;

	@Expose
	private int		port;

	@Expose
	private String	method;

	@Expose
	private String	payload;

	@Expose
	private String	returnValue;

	public Command() {
		this.id = UUID.randomUUID();
	}

	public Command(UUID source, UUID device, int port, String method, String payload) {
		this.setId(UUID.randomUUID());
		this.setSource(source);
		this.setDevice(device);
		this.setPort(port);

	}

	public UUID getSource() {
		return source;
	}

	public void setSource(UUID source) {
		this.source = source;
	}

	public UUID getDevice() {
		return device;
	}

	public void setDevice(UUID device) {
		this.device = device;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}

	public UUID getId() {
		return id;
	}

	private void setId(UUID id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
