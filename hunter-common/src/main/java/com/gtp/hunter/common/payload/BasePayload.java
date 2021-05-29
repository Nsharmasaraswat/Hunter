package com.gtp.hunter.common.payload;

import com.google.gson.GsonBuilder;

public abstract class BasePayload {
	
	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create().toJson(this);
	}
}
