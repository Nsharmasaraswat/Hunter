package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPDocumentItemDTO extends HeaderTableSapDTO {

	@SerializedName("NUMERO_NF")
	private String	numeroNf;

	@SerializedName("ITEM_NF")
	private String	itemNf;

	@SerializedName("MATERIAL")
	private String	material;

	@SerializedName("QUANTIDADE")
	private String	quantidade;

	@SerializedName("QTDE_CONTADA")
	private String	qtdeContada;

	@SerializedName("UNID_MED")
	private String	unidMed;

	@SerializedName("CENTRO")
	private String	centro;

	@SerializedName("DOC_COMPRAS")
	private String	docCompras;

	@SerializedName("DOCUMENTO_SD")
	private String	documentoSd;

	@SerializedName("ITEM_XML")
	private String	itemXml;

	@SerializedName("CFOP")
	private String	cfop;

	public String getNumeroNf() {
		return numeroNf;
	}

	public void setNumeroNf(String numeroNf) {
		this.numeroNf = numeroNf;
	}

	public String getItemNf() {
		return itemNf;
	}

	public void setItemNf(String itemNf) {
		this.itemNf = itemNf;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public String getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(String quantidade) {
		this.quantidade = quantidade;
	}

	public String getQtdeContada() {
		return qtdeContada;
	}

	public void setQtdeContada(String qtdeContada) {
		this.qtdeContada = qtdeContada;
	}

	public String getUnidMed() {
		return unidMed;
	}

	public void setUnidMed(String unidMed) {
		this.unidMed = unidMed;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public String getDocCompras() {
		return docCompras;
	}

	public void setDocCompras(String docCompras) {
		this.docCompras = docCompras;
	}

	public String getDocumentoSd() {
		return documentoSd;
	}

	public void setDocumentoSd(String documentoSd) {
		this.documentoSd = documentoSd;
	}

	public String getItemXml() {
		return itemXml;
	}

	public void setItemXml(String itemXml) {
		this.itemXml = itemXml;
	}

	public String getCfop() {
		return cfop;
	}

	public void setCfop(String cfop) {
		this.cfop = cfop;
	}
}
