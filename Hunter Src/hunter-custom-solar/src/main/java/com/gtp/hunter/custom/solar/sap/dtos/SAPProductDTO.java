package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPProductDTO extends HeaderTableSapDTO {

	@SerializedName("MATNR")
    private String matNr;
    
    @SerializedName("MAKTX")
    private String makTx;
    
    @SerializedName("MEINS")
    private String meins;
    
    @SerializedName("MVGR1")
    private String mvGr1;
    
    @SerializedName("MVGR2")
    private String mvGr2;
    
    @SerializedName("MVGR3")
    private String mvGr3;
    
    @SerializedName("MVGR4")
    private String mvGr4;
    
    @SerializedName("MVGR5")
    private String mvGr5;
    
    @SerializedName("BSTME")
    private String bsTme;
    
    @SerializedName("FATCONVUNI")
    private String fatConvUni;
    
    @SerializedName("FATCONVLIT")
    private String fatConvLit;
    
    @SerializedName("EAN")
    private String ean;
    
    @SerializedName("DUN")
    private String dun;
    
    @SerializedName("BRGEW")
    private String brGew;
    
    @SerializedName("NTGEW")
    private String ntGew;
    
    @SerializedName("MEGRU")
    private String meGru;
    
    @SerializedName("KTGRM")
    private String ktGrm;
    
    @SerializedName("GEWEI")
    private String geWei;
    
    @SerializedName("TIPO_KIT")
    private String kit;
    
    @SerializedName("MATNR_KIT")
    private String parent_sku;
    
    @SerializedName("MENGE_KIT")
    private double kitQuantity;

	public String getMaterial() {
		return matNr;
	}

	public void setMatNr(String matNr) {
		this.matNr = matNr;
	}

	public String getMakTx() {
		return makTx;
	}

	public void setMakTx(String makTx) {
		this.makTx = makTx;
	}

	public String getMeins() {
		return meins;
	}

	public void setMeins(String meins) {
		this.meins = meins;
	}

	public String getMvGr1() {
		return mvGr1;
	}

	public void setMvGr1(String mvGr1) {
		this.mvGr1 = mvGr1;
	}

	public String getMvGr2() {
		return mvGr2;
	}

	public void setMvGr2(String mvGr2) {
		this.mvGr2 = mvGr2;
	}

	public String getMvGr3() {
		return mvGr3;
	}

	public void setMvGr3(String mvGr3) {
		this.mvGr3 = mvGr3;
	}

	public String getMvGr4() {
		return mvGr4;
	}

	public void setMvGr4(String mvGr4) {
		this.mvGr4 = mvGr4;
	}

	public String getMvGr5() {
		return mvGr5;
	}

	public void setMvGr5(String mvGr5) {
		this.mvGr5 = mvGr5;
	}

	public String getBsTme() {
		return bsTme;
	}

	public void setBsTme(String bsTme) {
		this.bsTme = bsTme;
	}

	public String getFatConvUni() {
		return fatConvUni;
	}

	public void setFatConvUni(String fatConvUni) {
		this.fatConvUni = fatConvUni;
	}

	public String getFatConvLit() {
		return fatConvLit;
	}

	public void setFatConvLit(String fatConvLit) {
		this.fatConvLit = fatConvLit;
	}

	public String getEan() {
		return ean;
	}

	public void setEan(String ean) {
		this.ean = ean;
	}

	public String getDun() {
		return dun;
	}

	public void setDun(String dun) {
		this.dun = dun;
	}

	public String getBrGew() {
		return brGew;
	}

	public void setBrGew(String brGew) {
		this.brGew = brGew;
	}

	public String getNtGew() {
		return ntGew;
	}

	public void setNtGew(String ntGew) {
		this.ntGew = ntGew;
	}

	public String getMeGru() {
		return meGru;
	}

	public void setMeGru(String meGru) {
		this.meGru = meGru;
	}

	public String getKtGrm() {
		return ktGrm;
	}

	public void setKtGrm(String ktGrm) {
		this.ktGrm = ktGrm;
	}

	public String getGeWei() {
		return geWei;
	}

	public void setGeWei(String geWei) {
		this.geWei = geWei;
	}

	/**
	 * @return the kit
	 */
	public String getKit() {
		return kit;
	}

	/**
	 * @param kit the kit to set
	 */
	public void setKit(String kit) {
		this.kit = kit;
	}

	/**
	 * @return the parent_sku
	 */
	public String getParent_sku() {
		return parent_sku;
	}

	/**
	 * @param parent_sku the parent_sku to set
	 */
	public void setParent_sku(String parent_sku) {
		this.parent_sku = parent_sku;
	}

	/**
	 * @return the kitQuantity
	 */
	public double getKitQuantity() {
		return kitQuantity;
	}

	/**
	 * @param kitQuantity the kitQuantity to set
	 */
	public void setKitQuantity(double kitQuantity) {
		this.kitQuantity = kitQuantity;
	}
	
}
