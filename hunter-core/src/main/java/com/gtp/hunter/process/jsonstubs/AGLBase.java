package com.gtp.hunter.process.jsonstubs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLBase {

	protected final SimpleDateFormat	sdf	= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	@Expose
	private Date						createdAt;

	@Expose
	private Date						updatedAt;

	@Expose
	private String						created_at;

	@Expose
	private String						updated_at;

	public final Date getCreatedAt() {
		if (createdAt == null)
			try {
				return sdf.parse(this.created_at);
			} catch (ParseException e) {
				return Calendar.getInstance().getTime();
			}
		else
			return createdAt;
	}

	public final Date getUpdatedAt() {
		if (updatedAt == null)
			try {
				return sdf.parse(this.updated_at);
			} catch (ParseException e) {
				return Calendar.getInstance().getTime();
			}
		else
			return updatedAt;
	}

	public final String getCreated_at() {
		return created_at;
	}

	public final void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public final String getUpdated_at() {
		return updated_at;
	}

	public final void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	/**
	 * @param createdAt the createdAt to set
	 */
	public final void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @param updatedAt the updatedAt to set
	 */
	public final void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(this);
	}
}
