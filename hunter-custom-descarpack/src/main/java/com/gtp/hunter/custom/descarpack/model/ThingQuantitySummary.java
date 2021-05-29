package com.gtp.hunter.custom.descarpack.model;

import com.google.gson.annotations.Expose;

public class ThingQuantitySummary {

	@Expose
	int		armazenado;

	@Expose
	int		cancelado;

	@Expose
	String	description;

	@Expose
	int		embarcado;

	@Expose
	int		expedido;

	@Expose
	int		impresso;

	@Expose
	int		recebido;

	@Expose
	int		separado;

	@Expose
	String	sku;

	/**
	 * @return the armazenado
	 */
	public int getArmazenado() {
		return armazenado;
	}

	/**
	 * @return the cancelado
	 */
	public int getCancelado() {
		return cancelado;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the embarcado
	 */
	public int getEmbarcado() {
		return embarcado;
	}

	/**
	 * @return the expedido
	 */
	public int getExpedido() {
		return expedido;
	}

	/**
	 * @return the impresso
	 */
	public int getImpresso() {
		return impresso;
	}

	/**
	 * @return the recebido
	 */
	public int getRecebido() {
		return recebido;
	}

	/**
	 * @return the separado
	 */
	public int getSeparado() {
		return separado;
	}

	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param armazenado
	 *            the armazenado to set
	 */
	public void setArmazenado(int armazenado) {
		this.armazenado = armazenado;
	}

	/**
	 * @param cancelado
	 *            the cancelado to set
	 */
	public void setCancelado(int cancelado) {
		this.cancelado = cancelado;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param embarcado
	 *            the embarcado to set
	 */
	public void setEmbarcado(int embarcado) {
		this.embarcado = embarcado;
	}

	/**
	 * @param expedido
	 *            the expedido to set
	 */
	public void setExpedido(int expedido) {
		this.expedido = expedido;
	}

	/**
	 * @param impresso
	 *            the impresso to set
	 */
	public void setImpresso(int impresso) {
		this.impresso = impresso;
	}

	/**
	 * @param recebido
	 *            the recebido to set
	 */
	public void setRecebido(int recebido) {
		this.recebido = recebido;
	}

	/**
	 * @param separado
	 *            the separado to set
	 */
	public void setSeparado(int separado) {
		this.separado = separado;
	}

	/**
	 * @param sku
	 *            the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	@Override
	public String toString() {
		return sku + ", " + description + impresso + "," + embarcado + "," + recebido + "," + armazenado + "," + separado + "," + cancelado;
	}

}
