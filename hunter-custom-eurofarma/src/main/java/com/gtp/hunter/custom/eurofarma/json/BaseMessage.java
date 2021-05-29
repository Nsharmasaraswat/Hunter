package com.gtp.hunter.custom.eurofarma.json;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.custom.eurofarma.rest.app.DateDeserializer;
import com.gtp.hunter.custom.eurofarma.rest.app.DateSerializer;

public abstract class BaseMessage {

	@Expose
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
	@JsonDeserialize(using = DateDeserializer.class)
	@JsonSerialize(using = DateSerializer.class)
	private Date data;

	public BaseMessage() {

	}

	/**
	 * @return the data
	 */
	public Date getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Date data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}
}
