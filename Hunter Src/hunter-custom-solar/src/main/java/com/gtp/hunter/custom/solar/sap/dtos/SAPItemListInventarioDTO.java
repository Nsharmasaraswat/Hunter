package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPItemListInventarioDTO extends HeaderTableSapDTO {
	@SerializedName("WERKS")//Centro
	private String	centro;

	@SerializedName("LGORT")//Depósito
	private String	deposito;

	@SerializedName("GJAHR")//Exercício
	private int		ano;

	@SerializedName("IBLNR")//Documento de inventário
	private String	docSap;

	@SerializedName("ZEILI")//Nº linha
	private String	linha;

	@SerializedName("MATNR")//Nº do material
	private String	sku;

	@SerializedName("MAKTX")//Texto breve de material
	private String	prdDesc;

	@SerializedName("ERFMG")//Quantidade na unidade de medida do registro
	private double	qtd;

	@SerializedName("MEINS")
	private String	measureUnit;//Unidade de medida básica

	@SerializedName("XLOEK")
	private String	itemEliminado;//Item eliminado (inventário)

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

	/**
	 * @return the deposito
	 */
	public String getDeposito() {
		return deposito;
	}

	/**
	 * @param deposito the deposito to set
	 */
	public void setDeposito(String deposito) {
		this.deposito = deposito;
	}

	/**
	 * @return the ano
	 */
	public int getAno() {
		return ano;
	}

	/**
	 * @param ano the ano to set
	 */
	public void setAno(int ano) {
		this.ano = ano;
	}

	/**
	 * @return the docSap
	 */
	public String getDocSap() {
		return docSap;
	}

	/**
	 * @param docSap the docSap to set
	 */
	public void setDocSap(String docSap) {
		this.docSap = docSap;
	}

	/**
	 * @return the linha
	 */
	public String getLinha() {
		return linha;
	}

	/**
	 * @param linha the linha to set
	 */
	public void setLinha(String linha) {
		this.linha = linha;
	}

	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return the prdDesc
	 */
	public String getPrdDesc() {
		return prdDesc;
	}

	/**
	 * @param prdDesc the prdDesc to set
	 */
	public void setPrdDesc(String prdDesc) {
		this.prdDesc = prdDesc;
	}

	/**
	 * @return the qtd
	 */
	public Double getQtd() {
		return qtd;
	}

	/**
	 * @param qtd the qtd to set
	 */
	public void setQtd(Double qtd) {
		this.qtd = qtd;
	}

	/**
	 * @return the measureUnit
	 */
	public String getMeasureUnit() {
		return measureUnit;
	}

	/**
	 * @param measureUnit the measureUnit to set
	 */
	public void setMeasureUnit(String measureUnit) {
		this.measureUnit = measureUnit;
	}

	/**
	 * @return the itemEliminado
	 */
	public String getItemEliminado() {
		return itemEliminado;
	}

	/**
	 * @param itemEliminado the itemEliminado to set
	 */
	public void setItemEliminado(String itemEliminado) {
		this.itemEliminado = itemEliminado;
	}
}
