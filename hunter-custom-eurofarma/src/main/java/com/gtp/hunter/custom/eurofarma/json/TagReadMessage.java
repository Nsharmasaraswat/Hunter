package com.gtp.hunter.custom.eurofarma.json;

import java.util.UUID;

import com.google.gson.annotations.Expose;

public class TagReadMessage extends BaseMessage {
	@Expose
	private String	codigo;

	@Expose
	private UUID	portal;

	public TagReadMessage() {

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
	 * @return the portal
	 */
	public UUID getPortal() {
		return portal;
	}

	/**
	 * @param portal the portal to set
	 */
	public void setPortal(UUID portal) {
		this.portal = portal;
	}

}
