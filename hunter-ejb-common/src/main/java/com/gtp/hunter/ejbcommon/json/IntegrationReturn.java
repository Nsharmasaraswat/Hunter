package com.gtp.hunter.ejbcommon.json;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class IntegrationReturn {

	public static final IntegrationReturn	OK	= new IntegrationReturn(true, null);

	@Expose
	private boolean							result;
	@Expose
	private String							message;

	public IntegrationReturn() {
	}

	public IntegrationReturn(boolean result, String message) {
		this.setResult(result);
		this.setMessage(message);
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}
}
