package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SAPCheckoutFaturadoItemDTO {
	//Centro
	@Expose
	@SerializedName("WERKS")
	private String	werks;

	//transporte
	@Expose
	@SerializedName("TKNUM")
	private String	tknum;

	//material
	@Expose
	@SerializedName("MATNR")
	private String	matnr;

	//qtd faturada
	@Expose
	@SerializedName("LFIMG")
	private double	lfimg;

	//
	@Expose
	@SerializedName("PSTYV")
	private String	pstyv;

	//
	@Expose
	@SerializedName("SHTYP")
	private String	shtyp;

	/**
	 * @return the Werks
	 */
	public String getWerks() {
		return werks;
	}

	/**
	 * @param Werks the Werks to set
	 */
	public void setWerks(String werks) {
		this.werks = werks;
	}

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
	 * @return the pstyv
	 */
	public String getPstyv() {
		return pstyv;
	}

	/**
	 * @param pstyv the pstyv to set
	 */
	public void setPstyv(String pstyv) {
		this.pstyv = pstyv;
	}

	/**
	 * @return the shtyp
	 */
	public String getShtyp() {
		return shtyp;
	}

	/**
	 * @param shtyp the shtyp to set
	 */
	public void setShtyp(String shtyp) {
		this.shtyp = shtyp;
	}
}
