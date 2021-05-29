package com.gtp.hunter.custom.solar.sap.dtos;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class SAPInventoryItem extends HeaderTableSapDTO {

	@SerializedName("NUMDOC")//(Documento de inventário)
	private String	docSap;

	@SerializedName("EXERCICIO")//(Exercício)
	private int		year;

	@SerializedName("DATA")//(Data de realização da última contagem)
	private Date	data;

	@SerializedName("CENTRO")//(Centro)
	private String	centro;

	@SerializedName("DEPOSITO")//(Depósito)
	private String	deposito;

	@SerializedName("ITEM")//(Nº linha)
	private int		linha;

	@SerializedName("MATERIAL")//(Nº do material)
	private String	sku;

	@SerializedName("QUANTIDADE")//(Quantidade de inventário from Hunter)
	private double	qtd;

	@SerializedName("UND")//(Unidade de medida básica)
	private String	measureUnit;

	@SerializedName("PAC")//(Código de uma posição)
	private String	pac;

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
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the data
	 */
	public Date getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Date data) {
		this.data = data;
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
	 * @return the linha
	 */
	public int getLinha() {
		return linha;
	}

	/**
	 * @param linha the linha to set
	 */
	public void setLinha(int linha) {
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
	 * @return the qtd
	 */
	public double getQtd() {
		return qtd;
	}

	/**
	 * @param qtd the qtd to set
	 */
	public void setQtd(double qtd) {
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
	 * @return the pac
	 */
	public String getPac() {
		return pac;
	}

	/**
	 * @param pac the pac to set
	 */
	public void setPac(String pac) {
		this.pac = pac;
	}
}
