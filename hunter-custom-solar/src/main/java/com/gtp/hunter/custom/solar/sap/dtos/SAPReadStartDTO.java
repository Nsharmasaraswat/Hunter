package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPReadStartDTO {

	@SerializedName("MANDT")
	private String mandt;
	
	@SerializedName("IDENT")
	private String ident;
	
	@SerializedName("CODE")
	private String code;
	
	@SerializedName("CONTROLE")
	private String controle;
	
	@SerializedName("DATA_CADASTRO")
	private String dataCadastro;
	
	@SerializedName("HORA")
	private String hora;
	
	@SerializedName("USUARIO")
	private String usuario;
	
	@SerializedName("DATA_PROC_IN")
	private String dataProcIn;
	
	@SerializedName("HORA_PROC_IN")
	private String horaProcIn;
	
	@SerializedName("HORA_PROC_FM")
	private String horaProcFm;
	
	@SerializedName("MSG")
	private String msg;
	
	@SerializedName("CODE_MSG")
	private String codMsg;
	
	@SerializedName("STATUS")
	private String status;
	
	@SerializedName("RETORNO")
	private String retorno;

	public String getMandt() {
		return mandt;
	}

	public void setMandt(String mandt) {
		this.mandt = mandt;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getControle() {
		return controle;
	}

	public void setControle(String controle) {
		this.controle = controle;
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

	public String getDataProcIn() {
		return dataProcIn;
	}

	public void setDataProcIn(String dataProcIn) {
		this.dataProcIn = dataProcIn;
	}

	public String getHoraProcIn() {
		return horaProcIn;
	}

	public void setHoraProcIn(String horaProcIn) {
		this.horaProcIn = horaProcIn;
	}

	public String getHoraProcFm() {
		return horaProcFm;
	}

	public void setHoraProcFm(String horaProcFm) {
		this.horaProcFm = horaProcFm;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCodMsg() {
		return codMsg;
	}

	public void setCodMsg(String codMsg) {
		this.codMsg = codMsg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRetorno() {
		return retorno;
	}

	public void setRetorno(String retorno) {
		this.retorno = retorno;
	}

}
