package com.gtp.hunter.ui.json.action;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class BaseActionMessage {

	@Expose
	@SerializedName("command")
	protected String	command;

	@Expose
	@SerializedName("data")
	private Object		data;

	protected BaseActionMessage(String command) {
		this();//warning maldito
		this.command = command;
	}

	private BaseActionMessage() {
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
