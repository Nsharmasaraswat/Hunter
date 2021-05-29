package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPConfCegaMsgDTO extends HeaderTableSapDTO {
	
	@SerializedName("SEQ")
	private String seq;
	
	@SerializedName("TIPO")
	private String tipo;
	
	@SerializedName("MENSAGEM")
	private String mensagem;

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

}
