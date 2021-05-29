package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class HeaderTableSapDTO {

	@SerializedName("MANDT")
	private String	mandt;

	@SerializedName("CODE")
	private String	code;

	@SerializedName("IDENT")
	private String	ident;

	@SerializedName("CONTROLE")
	private String	controle;

	public String getMandt() {
		return mandt;
	}

	public void setMandt(String mandt) {
		this.mandt = mandt;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getControle() {
		return controle;
	}

	public void setControle(String controle) {
		this.controle = controle;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
	}
}
