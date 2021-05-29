package com.gtp.hunter.process.jsonstubs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLDocTransport extends AGLBase implements Comparable<AGLDocTransport> {

	@Expose
	private String	thing_id;

	@Expose
	private String	address_id;
	
	@Expose
	private String	origin_id;

	@Expose
	private int		seq;

	/**
	 * @return the thing_id
	 */
	public String getThing_id() {
		return thing_id;
	}

	/**
	 * @param thing_id the thing_id to set
	 */
	public void setThing_id(String thing_id) {
		this.thing_id = thing_id;
	}

	/**
	 * @return the address_id
	 */
	public String getAddress_id() {
		return address_id;
	}

	/**
	 * @param address_id the address_id to set
	 */
	public void setAddress_id(String address_id) {
		this.address_id = address_id;
	}

	/**
	 * @return the seq
	 */
	public int getSeq() {
		return seq;
	}

	/**
	 * @param seq the seq to set
	 */
	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	/**
	 * @return the origin_id
	 */
	public String getOrigin_id() {
		return origin_id;
	}

	/**
	 * @param origin_id the origin_id to set
	 */
	public void setOrigin_id(String origin_id) {
		this.origin_id = origin_id;
	}

	@Override
	public int compareTo(AGLDocTransport arg0) {
		if (arg0 != null) {
			if (seq < arg0.getSeq())
				return -1;
			if (seq > arg0.getSeq())
				return 1;
			return 0;
		}
		return -1;
	}
}
