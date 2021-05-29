package com.gtp.hunter.custom.descarpack.model;

import com.google.gson.annotations.Expose;

public class DocumentQuantitySummary {

	@Expose
	String	supplierName;

	@Expose
	String	documentType;

	@Expose
	String	documentCode;

	@Expose
	int		impresso;

	@Expose
	int		embarcado;

	@Expose
	int		recebido;

	@Expose
	int		armazenado;

	@Expose
	int		separado;

	@Expose
	int		expedido;

	@Expose
	int		cancelado;

	/**
	 * @return the supplierName
	 */
	public String getSupplierName() {
		return supplierName;
	}

	/**
	 * @param supplierName
	 *            the supplierName to set
	 */
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	/**
	 * @return the documentType
	 */
	public String getDocumentType() {
		return documentType;
	}

	/**
	 * @param documentType
	 *            the documentType to set
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/**
	 * @return the documentCode
	 */
	public String getDocumentCode() {
		return documentCode;
	}

	/**
	 * @param documentCode
	 *            the documentCode to set
	 */
	public void setDocumentCode(String documentCode) {
		this.documentCode = documentCode;
	}

	/**
	 * @return the impresso
	 */
	public int getImpresso() {
		return impresso;
	}

	/**
	 * @param impresso
	 *            the impresso to set
	 */
	public void setImpresso(int impresso) {
		this.impresso = impresso;
	}

	/**
	 * @return the embarcado
	 */
	public int getEmbarcado() {
		return embarcado;
	}

	/**
	 * @param embarcado
	 *            the embarcado to set
	 */
	public void setEmbarcado(int embarcado) {
		this.embarcado = embarcado;
	}

	/**
	 * @return the recebido
	 */
	public int getRecebido() {
		return recebido;
	}

	/**
	 * @param recebido
	 *            the recebido to set
	 */
	public void setRecebido(int recebido) {
		this.recebido = recebido;
	}

	/**
	 * @return the separado
	 */
	public int getSeparado() {
		return separado;
	}

	/**
	 * @param separado
	 *            the separado to set
	 */
	public void setSeparado(int separado) {
		this.separado = separado;
	}

	/**
	 * @return the expedido
	 */
	public int getExpedido() {
		return expedido;
	}

	/**
	 * @param expedido
	 *            the expedido to set
	 */
	public void setExpedido(int expedido) {
		this.expedido = expedido;
	}

	/**
	 * @return the cancelado
	 */
	public int getCancelado() {
		return cancelado;
	}

	/**
	 * @param cancelado
	 *            the cancelado to set
	 */
	public void setCancelado(int cancelado) {
		this.cancelado = cancelado;
	}

	@Override
	public String toString() {
		return supplierName + ", " + documentType + ", " + documentCode + "," + impresso + "," + embarcado + "," + recebido + "," + separado + "," + cancelado;
	}

	/**
	 * @return the armazenado
	 */
	public int getArmazenado() {
		return armazenado;
	}

	/**
	 * @param armazenado
	 *            the armazenado to set
	 */
	public void setArmazenado(int armazenado) {
		this.armazenado = armazenado;
	}
}
