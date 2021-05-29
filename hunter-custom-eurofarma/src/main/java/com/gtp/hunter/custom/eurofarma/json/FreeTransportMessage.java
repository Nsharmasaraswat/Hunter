package com.gtp.hunter.custom.eurofarma.json;

import com.google.gson.annotations.Expose;

public class FreeTransportMessage extends BaseMessage {

	@Expose
	private String	wm;

	@Expose
	private String	codigo;

	@Expose
	private String	origem;

	@Expose
	private String	destino;

	@Expose
	private String	documento;

	public FreeTransportMessage() {

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
}
