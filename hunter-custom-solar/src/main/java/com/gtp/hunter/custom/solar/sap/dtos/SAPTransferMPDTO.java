package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SAPTransferMPDTO extends HeaderTableSapDTO {

	@Expose
	@SerializedName("CENTRO")
	private String	centro;

	@Expose
	@SerializedName("MATERIAL")
	private String	material;

	@Expose
	@SerializedName("DOCUMENTO")
	private String	documento;

	@Expose
	@SerializedName("ANO")
	private int		ano;

	@Expose
	@SerializedName("TIPOMOV")
	private int		tipoMov;

	@Expose
	@SerializedName("DEPORIGEM")
	private String	origem;

	@Expose
	@SerializedName("DEPDESTINO")
	private String	destino;

	@Expose
	@SerializedName("QUANTIDADE")
	private double	quantidade;

	@Expose
	@SerializedName("UNID_MED")
	private String	unidMed;

	@Expose
	@SerializedName("DEBCRED")
	private String	debCred;

	@Expose
	@SerializedName("TIPO_NRHUNTER")
	private String	tipoNrHunter;

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
	 * @return the material
	 */
	public String getMaterial() {
		return material;
	}

	/**
	 * @param material the material to set
	 */
	public void setMaterial(String material) {
		this.material = material;
	}

	/**
	 * @return the documento
	 */
	public String getDocumento() {
		return documento;
	}

	/**
	 * @param documento the documento to set
	 */
	public void setDocumento(String documento) {
		this.documento = documento;
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
	 * @return the tipoMov
	 */
	public int getTipoMov() {
		return tipoMov;
	}

	/**
	 * @param tipoMov the tipoMov to set
	 */
	public void setTipoMov(int tipoMov) {
		this.tipoMov = tipoMov;
	}

	/**
	 * @return the origem
	 */
	public String getOrigem() {
		return origem;
	}

	/**
	 * @param origem the origem to set
	 */
	public void setOrigem(String origem) {
		this.origem = origem;
	}

	/**
	 * @return the destino
	 */
	public String getDestino() {
		return destino;
	}

	/**
	 * @param destino the destino to set
	 */
	public void setDestino(String destino) {
		this.destino = destino;
	}

	/**
	 * @return the quantidade
	 */
	public double getQuantidade() {
		return quantidade;
	}

	/**
	 * @param quantidade the quantidade to set
	 */
	public void setQuantidade(double quantidade) {
		this.quantidade = quantidade;
	}

	/**
	 * @return the unidMed
	 */
	public String getUnidMed() {
		return unidMed;
	}

	/**
	 * @param unidMed the unidMed to set
	 */
	public void setUnidMed(String unidMed) {
		this.unidMed = unidMed;
	}

	/**
	 * @return the debCred
	 */
	public String getDebCred() {
		return debCred;
	}

	/**
	 * @param debCred the debCred to set
	 */
	public void setDebCred(String debCred) {
		this.debCred = debCred;
	}

	/**
	 * @return the tipoNrHunter
	 */
	public String getTipoNrHunter() {
		return tipoNrHunter;
	}

	/**
	 * @param tipoNrHunter the tipoNrHunter to set
	 */
	public void setTipoNrHunter(String tipoNrHunter) {
		this.tipoNrHunter = tipoNrHunter;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create().toJson(this);
	}
}
