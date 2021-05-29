package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPProductPropertyDTO extends HeaderTableSapDTO {

	@SerializedName("MATNR")
	private String matNr;
	
	@SerializedName("MEINH")
    private String meiNh;
	
	@SerializedName("UMREZ")
    private String umRez;
	
	@SerializedName("UMREN")
    private String umRen;

	public String getMatNr() {
		return matNr;
	}

	public void setMatNr(String matNr) {
		this.matNr = matNr;
	}

	public String getMeiNh() {
		return meiNh;
	}

	public void setMeiNh(String meiNh) {
		this.meiNh = meiNh;
	}

	public String getUmRez() {
		return umRez;
	}

	public void setUmRez(String umRez) {
		this.umRez = umRez;
	}

	public String getUmRen() {
		return umRen;
	}

	public void setUmRen(String umRen) {
		this.umRen = umRen;
	}

	
}
