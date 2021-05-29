package com.gtp.hunter.custom.solar.sap.dtos;

import java.util.Date;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SAPRecusaDocDTO {

	//Número do Transporte
	@Expose
	@SerializedName("TKNUM")
	private String	tknum;

	//Número do Documento
	@Expose
	@SerializedName("DOCNUM")
	private String	docNum;

	//Número do Documento NFE
	@Expose
	@SerializedName("NFENUM")
	private String	nfeNum;

	//Código do clinete NFE
	@Expose
	@SerializedName("KUNNR")
	private String	kunnr;

	//Código do Produto
	@Expose
	@SerializedName("MATNR")
	private String	matnr;

	//Quantidade
	@Expose
	@SerializedName("MENGE")
	private double	menge;

	//Motivo pelo qual a característica não é exibida
	@Expose
	@SerializedName("REASON")
	private int		reason;

	//Denominação
	@Expose
	@SerializedName("DESCRIPTION")
	private String	description;

	//Data da Recusa
	@Expose
	@SerializedName("DATRECUSA")
	private Date	dataRecusa;

	//Hora da Recusa
	@Expose
	@SerializedName("TIMRECUSA")
	private Date	timeRecusa;

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
	 * @return the docNum
	 */
	public String getDocNum() {
		return docNum;
	}

	/**
	 * @param docNum the docNum to set
	 */
	public void setDocNum(String docNum) {
		this.docNum = docNum;
	}

	/**
	 * @return the nfeNum
	 */
	public String getNfeNum() {
		return nfeNum;
	}

	/**
	 * @param nfeNum the nfeNum to set
	 */
	public void setNfeNum(String nfeNum) {
		this.nfeNum = nfeNum;
	}

	/**
	 * @return the kunnr
	 */
	public String getKunnr() {
		return kunnr;
	}

	/**
	 * @param kunnr the kunnr to set
	 */
	public void setKunnr(String kunnr) {
		this.kunnr = kunnr;
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
	 * @return the menge
	 */
	public double getMenge() {
		return menge;
	}

	/**
	 * @param menge the menge to set
	 */
	public void setMenge(double menge) {
		this.menge = menge;
	}

	/**
	 * @return the reason
	 */
	public int getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(int reason) {
		this.reason = reason;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the dataRecusa
	 */
	public Date getDataRecusa() {
		return dataRecusa;
	}

	/**
	 * @param dataRecusa the dataRecusa to set
	 */
	public void setDataRecusa(Date dataRecusa) {
		this.dataRecusa = dataRecusa;
	}

	/**
	 * @return the timeRecusa
	 */
	public Date getTimeRecusa() {
		return timeRecusa;
	}

	/**
	 * @param timeRecusa the timeRecusa to set
	 */
	public void setTimeRecusa(Date timeRecusa) {
		this.timeRecusa = timeRecusa;
	}
}
