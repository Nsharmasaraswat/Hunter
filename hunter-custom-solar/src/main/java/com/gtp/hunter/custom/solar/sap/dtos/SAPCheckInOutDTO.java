package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SAPCheckInOutDTO extends HeaderTableSapDTO {

	@Expose
	@SerializedName("TKNUM")//(Nº transporte)
	private String	tknum;

	@Expose
	@SerializedName("FUNCAO")//(Função)
	private String	funcao;

	@Expose
	@SerializedName("ID_CONF")//(ID Conferência)
	private String	idConf;

	@Expose
	@SerializedName("MATNR")//(Nº do material)
	private String	matnr;

	@Expose
	@SerializedName("LFIMG")//(Quantidade fornecida de fato, em UMV)
	private double	lfimg;

	@Expose
	@SerializedName("FINAL")//(Fim de Contagem)
	private String	contfinal;

	/**
	 * @return the tknum
	 */
	public String getTknum() {
		return tknum;
	}

	/**
	 * @param tknum the tknum to set
	 */
	public void setTknum(String tknum) {
		this.tknum = tknum;
	}

	/**
	 * @return the funcao
	 */
	public String getFuncao() {
		return funcao;
	}

	/**
	 * @param funcao the funcao to set
	 */
	public void setFuncao(String funcao) {
		this.funcao = funcao;
	}

	/**
	 * @return the idConf
	 */
	public String getIdConf() {
		return idConf;
	}

	/**
	 * @param idConf the idConf to set
	 */
	public void setIdConf(String idConf) {
		this.idConf = idConf;
	}

	/**
	 * @return the matnr
	 */
	public String getMatnr() {
		return matnr;
	}

	/**
	 * @param matnr the matnr to set
	 */
	public void setMatnr(String matnr) {
		this.matnr = matnr;
	}

	/**
	 * @return the lfimg
	 */
	public double getLfimg() {
		return lfimg;
	}

	/**
	 * @param lfimg the lfimg to set
	 */
	public void setLfimg(double lfimg) {
		this.lfimg = lfimg;
	}

	/**
	 * @return the contfinal
	 */
	public String getContfinal() {
		return contfinal;
	}

	/**
	 * @param contfinal the contfinal to set
	 */
	public void setContfinal(String contfinal) {
		this.contfinal = contfinal;
	}

	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create().toJson(this);
	}
}
