package com.gtp.hunter.custom.eurofarma.json;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class CPIIntegrationMessage<T extends BaseMessage> {
	@Expose
	private String	command;

	@Expose
	private T		data;

	public CPIIntegrationMessage() {

	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the data
	 */
	public T getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").create().toJson(this);
	}
}
