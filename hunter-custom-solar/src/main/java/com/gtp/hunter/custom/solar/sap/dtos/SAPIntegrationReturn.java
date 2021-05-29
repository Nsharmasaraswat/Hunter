package com.gtp.hunter.custom.solar.sap.dtos;

import com.gtp.hunter.ejbcommon.json.IntegrationReturn;

public class SAPIntegrationReturn {

	private Integer code;
	private String code_msg;
	private String controle;
	private String msg;
	
	public SAPIntegrationReturn(Integer code, String code_msg, String controle, String msg) {
		this.code = code;
		this.code_msg = code_msg;
		this.controle = controle;
		this.msg = msg;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getCode_msg() {
		return code_msg;
	}
	public void setCode_msg(String code_msg) {
		this.code_msg = code_msg;
	}
	public String getControle() {
		return controle;
	}
	public void setControle(String controle) {
		this.controle = controle;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
