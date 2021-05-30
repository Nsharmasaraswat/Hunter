package com.gtp.hunter.custom.solar.trixlog.dto;

import com.google.gson.annotations.Expose;

public class TrixlogOutput {
	@Expose
	boolean	output0;//":false

	@Expose
	boolean	output1;//":false,

	@Expose
	boolean	output2;//":false,

	/**
	 * @return the output0
	 */
	public boolean isOutput0() {
		return output0;
	}

	/**
	 * @param output0 the output0 to set
	 */
	public void setOutput0(boolean output0) {
		this.output0 = output0;
	}

	/**
	 * @return the output1
	 */
	public boolean isOutput1() {
		return output1;
	}

	/**
	 * @param output1 the output1 to set
	 */
	public void setOutput1(boolean output1) {
		this.output1 = output1;
	}

	/**
	 * @return the output2
	 */
	public boolean isOutput2() {
		return output2;
	}

	/**
	 * @param output2 the output2 to set
	 */
	public void setOutput2(boolean output2) {
		this.output2 = output2;
	}
}
