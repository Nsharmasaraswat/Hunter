package com.gtp.hunter.custom.solar.sap.dtos;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class SAPControleQualidadeDTO extends HeaderTableSapDTO {

	@SerializedName("PLANT")
	private String	centro;
	//	PLANT(Centro)

	@SerializedName("REF_DOC_NO")
	private String	refDoc;
	//	REF_DOC_NO(Nº documento de referência)

	@SerializedName("PSTNG_DATE")
	private Date	docDate;
	//	PSTNG_DATE(Data de lançamento no documento)

	@SerializedName("DOC_DAT")
	private Date	dateInDoc;
	//	DOC_DAT(Data no documento)

	@SerializedName("MATERIAL")
	private String	sku;
	//	MATERIAL(Nº do material)

	@SerializedName("MOVE_TYPE")
	private int		moveType;
	//	MOVE_TYPE(Tipo de movimento (administração de estoques))

	@SerializedName("MOVE_COD")
	private String	moveCode;
	//	MOVE_COD(Código do movimento Hunter->SAP)

	@SerializedName("BATCH")
	private String	batch;
	//	BATCH(Número do lote)

	@SerializedName("STGE_LOC")
	private String	storage;
	//	STGE_LOC(Depósito)

	@SerializedName("MOVE_STLOC")
	private String	recStorage;
	//	MOVE_STLOC(Depósito de recebimento/de saída)

	@SerializedName("ENTRY_QNT")
	private double	qty;
	//	ENTRY_QNT(Quantidade)

	@SerializedName("UNID_MED")
	private String	measureUnit;
	//	UNID_MED(Unidade de medida básica)

	@SerializedName("MOVE_PLANT")
	private String	centroReceptorEmissor;
	//	MOVE_PLANT(Centro receptor/emissor)

	@SerializedName("MOVE_BATCH")
	private String	loteReceptorEmissor;
	//	MOVE_BATCH(Lote receptor/emissor)

	@SerializedName("TIPO_NRHUNTER")
	private String	document;
	//	TIPO_NRHUNTER(Tipo e Número do documento do Hunter)

	@SerializedName("ANO")
	private int		year;
	//	ANO(Ano do documento do material)

	@SerializedName("DOCUMENTO")
	private String	matDoc;
	//	DOCUMENTO(Nº documento de material)

	/**
	 * @return the centro
	 */
	public String getCentro() {
		return centro;
	}

	/**
	 * @param centro the centro to set
	 */
	public void setCentro(String centro) {
		this.centro = centro;
	}

	/**
	 * @return the refDoc
	 */
	public String getRefDoc() {
		return refDoc;
	}

	/**
	 * @param refDoc the refDoc to set
	 */
	public void setRefDoc(String refDoc) {
		this.refDoc = refDoc;
	}

	/**
	 * @return the docDate
	 */
	public Date getDocDate() {
		return docDate;
	}

	/**
	 * @param docDate the docDate to set
	 */
	public void setDocDate(Date docDate) {
		this.docDate = docDate;
	}

	/**
	 * @return the dateInDoc
	 */
	public Date getDateInDoc() {
		return dateInDoc;
	}

	/**
	 * @param dateInDoc the dateInDoc to set
	 */
	public void setDateInDoc(Date dateInDoc) {
		this.dateInDoc = dateInDoc;
	}

	/**
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return the moveType
	 */
	public int getMoveType() {
		return moveType;
	}

	/**
	 * @param moveType the moveType to set
	 */
	public void setMoveType(int moveType) {
		this.moveType = moveType;
	}

	/**
	 * @return the moveCode
	 */
	public String getMoveCode() {
		return moveCode;
	}

	/**
	 * @param moveCode the moveCode to set
	 */
	public void setMoveCode(String moveCode) {
		this.moveCode = moveCode;
	}

	/**
	 * @return the batch
	 */
	public String getBatch() {
		return batch;
	}

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(String batch) {
		this.batch = batch;
	}

	/**
	 * @return the storage
	 */
	public String getStorage() {
		return storage;
	}

	/**
	 * @param storage the storage to set
	 */
	public void setStorage(String storage) {
		this.storage = storage;
	}

	/**
	 * @return the recStorage
	 */
	public String getRecStorage() {
		return recStorage;
	}

	/**
	 * @param recStorage the recStorage to set
	 */
	public void setRecStorage(String recStorage) {
		this.recStorage = recStorage;
	}

	/**
	 * @return the qty
	 */
	public double getQty() {
		return qty;
	}

	/**
	 * @param qty the qty to set
	 */
	public void setQty(double qty) {
		this.qty = qty;
	}

	/**
	 * @return the measureUnit
	 */
	public String getMeasureUnit() {
		return measureUnit;
	}

	/**
	 * @param measureUnit the measureUnit to set
	 */
	public void setMeasureUnit(String measureUnit) {
		this.measureUnit = measureUnit;
	}

	/**
	 * @return the centroReceptorEmissor
	 */
	public String getCentroReceptorEmissor() {
		return centroReceptorEmissor;
	}

	/**
	 * @param centroReceptorEmissor the centroReceptorEmissor to set
	 */
	public void setCentroReceptorEmissor(String centroReceptorEmissor) {
		this.centroReceptorEmissor = centroReceptorEmissor;
	}

	/**
	 * @return the loteReceptorEmissor
	 */
	public String getLoteReceptorEmissor() {
		return loteReceptorEmissor;
	}

	/**
	 * @param loteReceptorEmissor the loteReceptorEmissor to set
	 */
	public void setLoteReceptorEmissor(String loteReceptorEmissor) {
		this.loteReceptorEmissor = loteReceptorEmissor;
	}

	/**
	 * @return the document
	 */
	public String getDocument() {
		return document;
	}

	/**
	 * @param document the document to set
	 */
	public void setDocument(String document) {
		this.document = document;
	}

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the matDoc
	 */
	public String getMatDoc() {
		return matDoc;
	}

	/**
	 * @param matDoc the matDoc to set
	 */
	public void setMatDoc(String matDoc) {
		this.matDoc = matDoc;
	}
}
