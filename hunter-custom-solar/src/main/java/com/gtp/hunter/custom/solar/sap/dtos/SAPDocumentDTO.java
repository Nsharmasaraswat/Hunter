package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPDocumentDTO extends HeaderTableSapDTO {

	@SerializedName("NUMERO_NF")
	private String	numeroNf;

	@SerializedName("TIPO_NR")
	private String	tipoNr;

	//N5 = NFSaida Devolucao
	@SerializedName("CATEG_NF")
	private String	categNf;

	//1 = ENTRADA, 2 = SAIDA, 3 = Devolução de Saída de transf de Estoque, 4 = Devolução de entrada de transf de Estoque #5 = Devolucao de Venda
	@SerializedName("DIRECAO_NF")
	private String	direcaoNf;

	@SerializedName("DATA_NF")
	private String	dataNf;

	@SerializedName("SERIE_NF")
	private String	serieNf;

	@SerializedName("CHAVE_NFE")
	private String	chaveNfe;

	@SerializedName("FORNECEDOR")
	private String	fornecedor;

	//Nº transporte
	@SerializedName("TKNUM")
	private String	tkNum;

	//Nº documento de vendas e distribuição
	@SerializedName("VBELN")
	private String	vbeLn;

	//Local De Expedição/Local de Recebimento de Mercadoria
	@SerializedName("VSTEL")
	private String	vsTel;

	//Tipo de Transporte
	@SerializedName("SHTYP")
	private String	shtyp;

	//Faz ZTRANS?
	@SerializedName("ZTRANS")
	private String	ztrans;

	public String getNumeroNf() {
		return numeroNf;
	}

	public void setNumeroNf(String numeroNf) {
		this.numeroNf = numeroNf;
	}

	public String getTipoNr() {
		return tipoNr;
	}

	public void setTipoNr(String tipoNr) {
		this.tipoNr = tipoNr;
	}

	public String getCategNf() {
		return categNf;
	}

	public void setCategNf(String categNf) {
		this.categNf = categNf;
	}

	public String getDirecaoNf() {
		return direcaoNf;
	}

	public void setDirecaoNf(String direcaoNf) {
		this.direcaoNf = direcaoNf;
	}

	public String getDataNf() {
		return dataNf;
	}

	public void setDataNf(String dataNf) {
		this.dataNf = dataNf;
	}

	public String getSerieNf() {
		return serieNf;
	}

	public void setSerieNf(String serieNf) {
		this.serieNf = serieNf;
	}

	public String getChaveNfe() {
		return chaveNfe;
	}

	public void setChaveNfe(String chaveNfe) {
		this.chaveNfe = chaveNfe;
	}

	public String getFornecedor() {
		return fornecedor;
	}

	public void setFornecedor(String fornecedor) {
		this.fornecedor = fornecedor;
	}

	public String getTkNum() {
		return tkNum;
	}

	public void setTkNum(String tkNum) {
		this.tkNum = tkNum;
	}

	public String getVbeLn() {
		return vbeLn;
	}

	public void setVbeLn(String vbeLn) {
		this.vbeLn = vbeLn;
	}

	public String getVsTel() {
		return vsTel;
	}

	public void setVsTel(String vsTel) {
		this.vsTel = vsTel;
	}

	public String getShtyp() {
		return shtyp;
	}

	public void setShtyp(String shtyp) {
		this.shtyp = shtyp;
	}

	public String getZtrans() {
		return ztrans;
	}

	public void setZtrans(String ztrans) {
		this.ztrans = ztrans;
	}

}
