package com.gtp.hunter.custom.eurofarma.json;

import com.google.gson.annotations.Expose;

public class ProductCheckingMessage extends BaseMessage {

	@Expose
	private String	codigo;

	@Expose
	private String	tipo;

	@Expose
	private String	wm;

	public ProductCheckingMessage() {

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
	 * @return the tipo
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * @param tipo the tipo to set
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
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
}
