package com.gtp.hunter.custom.solar.trixlog.dto;

import com.google.gson.annotations.Expose;

public class TrixlogInput {
	@Expose
	int	input1;//":1

	@Expose
	int	input2;//":1,

	@Expose
	int	input3;//":0,

	@Expose
	int	input4;//":0,

	/**
	 * @return the input4
	 */
	public int getInput4() {
		return input4;
	}

	/**
	 * @param input4 the input4 to set
	 */
	public void setInput4(int input4) {
		this.input4 = input4;
	}

	/**
	 * @return the input3
	 */
	public int getInput3() {
		return input3;
	}

	/**
	 * @param input3 the input3 to set
	 */
	public void setInput3(int input3) {
		this.input3 = input3;
	}

	/**
	 * @return the input2
	 */
	public int getInput2() {
		return input2;
	}

	/**
	 * @param input2 the input2 to set
	 */
	public void setInput2(int input2) {
		this.input2 = input2;
	}

	/**
	 * @return the input1
	 */
	public int getInput1() {
		return input1;
	}

	/**
	 * @param input1 the input1 to set
	 */
	public void setInput1(int input1) {
		this.input1 = input1;
	}

}
