package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SAPCheckInOutPortariaDTO extends HeaderTableSapDTO {

	@Expose
	@SerializedName("TKNUM")//(Nº transporte)
	private String	tknum;

	@Expose
	@SerializedName("SAIENT")//(Saida Portaria=S; Entrada Portaria = E)
	private String	saient;

	@Expose
	@SerializedName("CARRINHOS")//(Qtde de Carrinhos)
	private int		carrinhos;

	@Expose
	@SerializedName("CONES")//(Qtde de Cones)
	private int		cones;

	@Expose
	@SerializedName("KMSAIENT")//(Km de Saida ou Entrada)
	private int		kmsaient;

	@Expose
	@SerializedName("OBSERVACAO")//(Observação)
	private String	contfinal;

	@Expose
	@SerializedName("ITENSEGUR")//(Itens de Segurança= X)
	private String	itensSegur;

	@Expose
	@SerializedName("EXTINTOR")//(Extintor = X)
	private String	extintor;

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
	 * @return the saient
	 */
	public String getSaient() {
		return saient;
	}

	/**
	 * @param saient the saient to set
	 */
	public void setSaient(String saient) {
		this.saient = saient;
	}

	/**
	 * @return the carrinhos
	 */
	public int getCarrinhos() {
		return carrinhos;
	}

	/**
	 * @param carrinhos the carrinhos to set
	 */
	public void setCarrinhos(int carrinhos) {
		this.carrinhos = carrinhos;
	}

	/**
	 * @return the cones
	 */
	public int getCones() {
		return cones;
	}

	/**
	 * @param cones the cones to set
	 */
	public void setCones(int cones) {
		this.cones = cones;
	}

	/**
	 * @return the kmsaient
	 */
	public int getKmsaient() {
		return kmsaient;
	}

	/**
	 * @param kmsaient the kmsaient to set
	 */
	public void setKmsaient(int kmsaient) {
		this.kmsaient = kmsaient;
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

	/**
	 * @return the itensSegur
	 */
	public String getItensSegur() {
		return itensSegur;
	}

	/**
	 * @param itensSegur the itensSegur to set
	 */
	public void setItensSegur(String itensSegur) {
		this.itensSegur = itensSegur;
	}

	/**
	 * @return the extintor
	 */
	public String getExtintor() {
		return extintor;
	}

	/**
	 * @param extintor the extintor to set
	 */
	public void setExtintor(String extintor) {
		this.extintor = extintor;
	}

	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create().toJson(this);
	}
}
