package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SAPProntaEntregaDTO {
	//transporte mãe
	@Expose
	@SerializedName("TKNUMZSPE")
	private String	tknumzspe;

	//transporte filho – pronta entrega
	@Expose
	@SerializedName("TKNUMZVPE")
	private String	tknumzvpe;

	//material
	@Expose
	@SerializedName("MATNR")
	private String	matnr;

	//nome mnaterial
	@Expose
	@SerializedName("ARKTX")
	private String	arktx;

	//qtd venda pronta entrega
	@Expose
	@SerializedName("KBMENG_ZVPE")
	private double	kbmeng_zvpe;

	//qtd saída simples remessa para venda
	@Expose
	@SerializedName("KBMENG_ZSPE")
	private double	kbmeng_zspe;

	//qtd para retorno
	@Expose
	@SerializedName("KBMENG_CHECKIN")
	private double	kbmeng_checkin;

	//data do dia
	@Expose
	@SerializedName("DATASYS")
	private String	dataSys;

	//grupo de material.
	@Expose
	@SerializedName("MATKL")
	private String	mvgr1;

	/**
	 * @return the tknumzspe
	 */
	public String getTknumzspe() {
		return tknumzspe;
	}

	/**
	 * @param tknumzspe the tknumzspe to set
	 */
	public void setTknumzspe(String tknumzspe) {
		this.tknumzspe = tknumzspe;
	}

	/**
	 * @return the tknumzvpe
	 */
	public String getTknumzvpe() {
		return tknumzvpe;
	}

	/**
	 * @param tknumzvpe the tknumzvpe to set
	 */
	public void setTknumzvpe(String tknumzvpe) {
		this.tknumzvpe = tknumzvpe;
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
	 * @return the arktx
	 */
	public String getArktx() {
		return arktx;
	}

	/**
	 * @param arktx the arktx to set
	 */
	public void setArktx(String arktx) {
		this.arktx = arktx;
	}

	/**
	 * @return the kbmeng_zvpe
	 */
	public double getKbmeng_zvpe() {
		return kbmeng_zvpe;
	}

	/**
	 * @param kbmeng_zvpe the kbmeng_zvpe to set
	 */
	public void setKbmeng_zvpe(double kbmeng_zvpe) {
		this.kbmeng_zvpe = kbmeng_zvpe;
	}

	/**
	 * @return the kbmeng_zspe
	 */
	public double getKbmeng_zspe() {
		return kbmeng_zspe;
	}

	/**
	 * @param kbmeng_zspe the kbmeng_zspe to set
	 */
	public void setKbmeng_zspe(double kbmeng_zspe) {
		this.kbmeng_zspe = kbmeng_zspe;
	}

	/**
	 * @return the kbmeng_checkin
	 */
	public double getKbmeng_checkin() {
		return kbmeng_checkin;
	}

	/**
	 * @param kbmeng_checkin the kbmeng_checkin to set
	 */
	public void setKbmeng_checkin(double kbmeng_checkin) {
		this.kbmeng_checkin = kbmeng_checkin;
	}

	/**
	 * @return the dataSys
	 */
	public String getDataSys() {
		return dataSys;
	}

	/**
	 * @param dataSys the dataSys to set
	 */
	public void setDataSys(String dataSys) {
		this.dataSys = dataSys;
	}

	/**
	 * @return the mvgr1
	 */
	public String getMvgr1() {
		return mvgr1;
	}

	/**
	 * @param mvgr1 the mvgr1 to set
	 */
	public void setMvgr1(String mvgr1) {
		this.mvgr1 = mvgr1;
	}
}
