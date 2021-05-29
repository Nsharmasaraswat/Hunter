package com.gtp.hunter.custom.eurofarma.json;

import java.util.List;

import com.google.gson.annotations.Expose;

public class InventoryMessage extends BaseMessage {

	@Expose
	private String				wm;

	@Expose
	private String				documento;

	@Expose
	private List<InventoryItem>	itens;

	public InventoryMessage() {

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
	 * @return the itens
	 */
	public List<InventoryItem> getItens() {
		return itens;
	}

	/**
	 * @param itens the itens to set
	 */
	public void setItens(List<InventoryItem> itens) {
		this.itens = itens;
	}
}
