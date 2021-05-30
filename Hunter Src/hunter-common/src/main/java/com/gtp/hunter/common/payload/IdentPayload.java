package com.gtp.hunter.common.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IdentPayload extends BasePayload {

	@Expose
	@SerializedName(value = "RSSI")
	short	rssi;

	@Expose
	@SerializedName(value = "metaname")
	String	metaname;

	@Expose
	@SerializedName(value = "reader_ts")
	long	readerTs;

	/**
	 * @return the rssi
	 */
	public short getRssi() {
		return rssi;
	}

	/**
	 * @param rssi
	 *            the rssi to set
	 */
	public void setRssi(short rssi) {
		this.rssi = rssi;
	}

	/**
	 * @return the metaname
	 */
	public String getMetaname() {
		return metaname;
	}

	/**
	 * @param metaname
	 *            the metaname to set
	 */
	public void setMetaname(String metaname) {
		this.metaname = metaname;
	}

	/**
	 * @return the readerTs
	 */
	public long getReaderTs() {
		return readerTs;
	}

	/**
	 * @param readerTs
	 *            the readerTs to set
	 */
	public void setReaderTs(long readerTs) {
		this.readerTs = readerTs;
	}
}
