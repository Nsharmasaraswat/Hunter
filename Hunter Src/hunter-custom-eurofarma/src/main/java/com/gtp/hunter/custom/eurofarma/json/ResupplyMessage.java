package com.gtp.hunter.custom.eurofarma.json;

import com.google.gson.annotations.Expose;

public class ResupplyMessage extends BaseMessage {

	@Expose
	private String	wm;

	@Expose
	private String	codigo;

	@Expose
	private String	endereco;

	public ResupplyMessage() {

	}

	/**
	 * @return the wm
	 */
	public String getWm() {
		return wm;
	}

	/**
	 * @param wm the wm to set
	 */
	public void setWm(String wm) {
		this.wm = wm;
	}

	/**
	 * @return the codigo
	 */
	public String getCodigo() {
		return codigo;
	}

	/**
	 * @param codigo the codigo to set
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	/**
	 * @return the endereco
	 */
	public String getEndereco() {
		return endereco;
	}

	/**
	 * @param endereco the endereco to set
	 */
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}
}
