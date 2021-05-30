package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPCustomerDTO extends HeaderTableSapDTO {

	//Codigo do cliente
	@SerializedName("KUNNR")
	private String kunnr;

	//Nome do cliente
	@SerializedName("NAME1")
	private String name1;

	//CNPJ
	@SerializedName("STCD1")
	private String stcd1;

	//CPF
	@SerializedName("STCD2")
	private String stcd2;

	//Regiao (Estado, Provincia, Condado, ...)
	@SerializedName("REGIO")
	private String regio;

	//Local
	@SerializedName("ORT01")
	private String ort01;

	//Cidade
	@SerializedName("CITY1")
	private String city1;

	//Grupo de contas do cliente
	@SerializedName("KTOKD")
	private String ktokd;

	//Classificaçao dos clientes
	@SerializedName("KUKLA")
	private String kukla;

	//Key Account
	@SerializedName("BRAN1")
	private String bran1;

	//Atividade
	@SerializedName("KATR1")
	private String katr1;

	//Mercado Solar
	@SerializedName("KATR6")
	private String katr6;

	//Sub Canal
	@SerializedName("KATR7")
	private String katr7;

	//Rota Venda
	@SerializedName("RPMKR")
	private String rpmkr;

	//CLuster
	@SerializedName("KATR10")
	private String katr10;

	/**
	 * @return the Codigo do cliente
	 */
	public String getKunnr() {
		return kunnr;
	}

	/**
	 * @param kunnr the Codigo do cliente to set
	 */
	public void setKunnr(String kunnr) {
		this.kunnr = kunnr;
	}

	/**
	 * @return the Nome do cliente
	 */
	public String getName1() {
		return name1;
	}

	/**
	 * @param name1 the Nome do cliente to set
	 */
	public void setName1(String name1) {
		this.name1 = name1;
	}

	/**
	 * @return the CNPJ
	 */
	public String getStcd1() {
		return stcd1;
	}

	/**
	 * @param stcd1 the CNPJ to set
	 */
	public void setStcd1(String stcd1) {
		this.stcd1 = stcd1;
	}

	/**
	 * @return the CPF
	 */
	public String getStcd2() {
		return stcd2;
	}

	/**
	 * @param stcd2 the CPF to set
	 */
	public void setStcd2(String stcd2) {
		this.stcd2 = stcd2;
	}

	/**
	 * @return the Regiao (Estado, Provincia, Condado, ...)
	 */
	public String getRegio() {
		return regio;
	}

	/**
	 * @param regio the Regiao (Estado, Provincia, Condado, ...) to set
	 */
	public void setRegio(String regio) {
		this.regio = regio;
	}

	/**
	 * @return the Local
	 */
	public String getOrt01() {
		return ort01;
	}

	/**
	 * @param ort01 the Local to set
	 */
	public void setOrt01(String ort01) {
		this.ort01 = ort01;
	}

	/**
	 * @return the Cidade
	 */
	public String getCity1() {
		return city1;
	}

	/**
	 * @param city1 the Cidade to set
	 */
	public void setCity1(String city1) {
		this.city1 = city1;
	}

	/**
	 * @return the Grupo de contas do cliente
	 */
	public String getKtokd() {
		return ktokd;
	}

	/**
	 * @param ktokd the Grupo de contas do cliente to set
	 */
	public void setKtokd(String ktokd) {
		this.ktokd = ktokd;
	}

	/**
	 * @return the Classificaçao dos clientes
	 */
	public String getKukla() {
		return kukla;
	}

	/**
	 * @param kukla the Classificaçao dos clientes to set
	 */
	public void setKukla(String kukla) {
		this.kukla = kukla;
	}

	/**
	 * @return the Key Account
	 */
	public String getBran1() {
		return bran1;
	}

	/**
	 * @param bran1 the Key Account to set
	 */
	public void setBran1(String bran1) {
		this.bran1 = bran1;
	}

	/**
	 * @return the Atividade
	 */
	public String getKatr1() {
		return katr1;
	}

	/**
	 * @param katr1 the Atividade to set
	 */
	public void setKatr1(String katr1) {
		this.katr1 = katr1;
	}

	/**
	 * @return the Mercado Solar
	 */
	public String getKatr6() {
		return katr6;
	}

	/**
	 * @param katr6 the Mercado Solar to set
	 */
	public void setKatr6(String katr6) {
		this.katr6 = katr6;
	}

	/**
	 * @return the Sub Canal
	 */
	public String getKatr7() {
		return katr7;
	}

	/**
	 * @param katr7 the Sub Canal to set
	 */
	public void setKatr7(String katr7) {
		this.katr7 = katr7;
	}

	/**
	 * @return the Rota Venda
	 */
	public String getRpmkr() {
		return rpmkr;
	}

	/**
	 * @param rpmkr the Rota Venda to set
	 */
	public void setRpmkr(String rpmkr) {
		this.rpmkr = rpmkr;
	}

	/**
	 * @return the CLuster
	 */
	public String getKatr10() {
		return katr10;
	}

	/**
	 * @param katr10 the CLuster to set
	 */
	public void setKatr10(String katr10) {
		this.katr10 = katr10;
	}
}
