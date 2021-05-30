package com.gtp.hunter.custom.eurofarma.json;

import java.util.List;

import com.google.gson.annotations.Expose;

public class InventoryRequestMessage extends BaseMessage {

	@Expose
	private String			wm;

	@Expose
	private String			documento;

	@Expose
	private List<String>	endereco;

	public InventoryRequestMessage() {

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
	 * @return the endereco
	 */
	public List<String> getEndereco() {
		return endereco;
	}

	/**
	 * @param endereco the endereco to set
	 */
	public void setEndereco(List<String> endereco) {
		this.endereco = endereco;
	}

}
