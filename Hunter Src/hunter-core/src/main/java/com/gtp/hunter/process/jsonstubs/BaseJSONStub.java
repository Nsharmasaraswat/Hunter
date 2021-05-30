package com.gtp.hunter.process.jsonstubs;

import com.google.gson.GsonBuilder;

public class BaseJSONStub {

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create().toJson(this);
	}

}
