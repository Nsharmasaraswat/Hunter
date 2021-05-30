package com.gtp.hunter.custom.eurofarma.json;

import java.util.UUID;

import com.google.gson.annotations.Expose;

public class PortalMessage extends BaseMessage {

	@Expose
	private UUID portal;

	public PortalMessage() {

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
