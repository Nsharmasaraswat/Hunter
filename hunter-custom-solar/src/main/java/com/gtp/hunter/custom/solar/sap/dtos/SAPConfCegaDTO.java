package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPConfCegaDTO extends HeaderTableSapDTO {

	@SerializedName("MANDT")
	private String mandt;
	
	@SerializedName("DATA_NF")
	private String datanf;
	
	@SerializedName("NUMERO_NF")
	private String numeroNf;
	
	@SerializedName("SERIE_NF")
	private String serienf;
	
	@SerializedName("EBELN")
	private String ebeln;
	
	@SerializedName("MATNR")
	private String matnr;
	
	@SerializedName("QTDE_CONTADA")
	private String qtdecontada;
	
	@SerializedName("LGORT")
	private String lgort;
	
	@SerializedName("CENTRO")
	private String centro;

	public String getMandt() {
		return mandt;
	}

	public void setMandt(String mandt) {
		this.mandt = mandt;
	}

	public String getDatanf() {
		return datanf;
	}

	public void setDatanf(String datanf) {
		this.datanf = datanf;
	}

	public String getNumeroNf() {
		return numeroNf;
	}

	public void setNumeroNf(String numeroNf) {
		this.numeroNf = numeroNf;
	}

	public String getSerienf() {
		return serienf;
	}

	public void setSerienf(String serienf) {
		this.serienf = serienf;
	}

	public String getEbeln() {
		return ebeln;
	}

	public void setEbeln(String ebeln) {
		this.ebeln = ebeln;
	}

	public String getMatnr() {
		return matnr;
	}

	public void setMatnr(String matnr) {
		this.matnr = matnr;
	}

	public String getQtdecontada() {
		return qtdecontada;
	}

	public void setQtdecontada(String qtdecontada) {
		this.qtdecontada = qtdecontada;
	}

	public String getLgort() {
		return lgort;
	}

	public void setLgort(String lgort) {
		this.lgort = lgort;
	}

	/**
	 * @return the centro
	 */
	public String getCentro() {
		return centro;
	}

	/**
	 * @param centro the centro to set
	 */
	public void setCentro(String centro) {
		this.centro = centro;
	}	
}
