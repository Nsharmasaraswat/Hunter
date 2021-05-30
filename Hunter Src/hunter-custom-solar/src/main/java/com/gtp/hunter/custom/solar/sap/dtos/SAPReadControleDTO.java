package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPReadControleDTO {

	@SerializedName("MANDT")
	private String mandt;
	
	@SerializedName("CODE")
	private String code;
	
	@SerializedName("INTER_CODE")
	private String interCode;
	
	@SerializedName("DES_INTER")
	private String desInter;
	
	@SerializedName("NOME_RFC")
	private String nomeRfc;
	
	@SerializedName("DATA_CADASTRO")
	private String dataCadastro;
	
	@SerializedName("HORA")
	private String hora;
	
	@SerializedName("USUARIO")
	private String usuario;
	
	@SerializedName("IDENT")
	private String ident;
	
	@SerializedName("ORIGEM")
	private String origem;

	public String getMandt() {
		return mandt;
	}

	public void setMandt(String mandt) {
		this.mandt = mandt;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getInterCode() {
		return interCode;
	}

	public void setInterCode(String interCode) {
		this.interCode = interCode;
	}

	public String getDesInter() {
		return desInter;
	}

	public void setDesInter(String desInter) {
		this.desInter = desInter;
	}

	public String getNomeRfc() {
		return nomeRfc;
	}

	public void setNomeRfc(String nomeRfc) {
		this.nomeRfc = nomeRfc;
	}

	public String getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(String dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public String getHora() {
		return hora;
	}

	public void setHora(String hora) {
		this.hora = hora;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}
	
}
