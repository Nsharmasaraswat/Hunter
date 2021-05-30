package com.gtp.hunter.custom.eurofarma.json;

import com.google.gson.annotations.Expose;

public class InventoryItem {

	@Expose
	private String	codigo;

	@Expose
	private String	posicao;

	public InventoryItem() {

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
	 * @return the posicao
	 */
	public String getPosicao() {
		return posicao;
	}

	/**
	 * @param posicao the posicao to set
	 */
	public void setPosicao(String posicao) {
		this.posicao = posicao;
	}
}
