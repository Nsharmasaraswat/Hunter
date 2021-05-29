package com.gtp.hunter.ui.json.process;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class BaseProcessMessage {

	@Expose
	@SerializedName("command")
	protected String	command;

	@Expose
	@SerializedName("data")
	private Object		data;

	protected BaseProcessMessage(String command) {
		this();
		this.command = command;
	}

	private BaseProcessMessage() {
		this.command = "NO-OP";
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}
}
