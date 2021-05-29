package com.gtp.hunter.custom.solar.sap.dtos;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.custom.solar.util.Constants;

public class ReadFieldsSap implements Serializable {

	private static final long						serialVersionUID	= 1L;

	private LinkedHashMap<String, Object>			resultList;

	@SerializedName("T_ZWH_START")
	private List<SAPReadStartDTO>					readStartDTOs;

	@SerializedName("T_ZWH_CONTROLE")
	private List<SAPReadControleDTO>				readControleDTOs;

	@SerializedName("T_ZWH_LFA1")
	private List<SAPSupplierDTO>					supplierDTOs;

	@SerializedName("T_ZWH_KNA1")
	private List<SAPCustomerDTO>					customerDTOs;

	@SerializedName("T_ZWH_MARA")
	private List<SAPProductDTO>						productDTOs;

	@SerializedName("T_ZWH_NFDOC")
	private List<SAPDocumentDTO>					documentDTOs;

	@SerializedName("T_ZWH_NFLIN")
	private List<SAPDocumentItemDTO>				documentItemDTOs;

	@SerializedName("T_ZWH_MARM")
	private List<SAPProductPropertyDTO>				productPropertyDTOs;

	@SerializedName("T_ZWH_CONFCEGALOG")
	private List<SAPConfCegaMsgDTO>					confCegaDTOs;

	@SerializedName("T_ZWH_ORDER_PRD")
	private List<SAPProductionOrderProductionDTO>	prodOrderPrdDTOs;

	@SerializedName("T_ZWH_ORDER_COMP")
	private List<SAPProductionOrderConsumptionDTO>	prodOrderConsDTOs;

	@SerializedName(Constants.TBL_TRANSFERENCIA)
	private List<SAPTransferMPDTO>					transferMPDTOs;

	@SerializedName(Constants.TBL_QUALIDADE)
	private List<SAPControleQualidadeDTO>			controleQualidadeDTOs;

	@SerializedName(Constants.TBL_CHECKINOUT)
	private List<SAPCheckInOutDTO>					checkInOutDTOs;

	@SerializedName(Constants.TBL_CHECKINOUT_PORTARIA)
	private List<SAPCheckInOutPortariaDTO>			checkInOutPortariaDTOs;

	@SerializedName(Constants.T_RETURN)
	private List<SAPReturnDTO>						returnDTOs;

	@SerializedName(Constants.I_LISTA_INVENTARIO)//ZWH_INVENT
	private List<SAPItemListInventarioDTO>			listaInventarioDTOs;

	@SerializedName(Constants.I_REGISTRO)
	private List<SAPInventoryItem>					itensInventarioDTOs;

	@SerializedName(Constants.TBL_PRONTA_ENTREGA)
	private List<SAPProntaEntregaDTO>				prontaEntregaDTOs;

	@SerializedName(Constants.TBL_CHECKOUT_FATURADO)
	private List<SAPCheckoutFaturadoItemDTO>		checkoutFaturadoItemDTOs;

	@SerializedName(Constants.TBL_VASILHAME_GERAL)
	private List<SAPVasilhameGeralDTO>				vasilhameGeralDTOs;

	@SerializedName("E_RETURN")
	private String									eReturn;

	@SerializedName("E_RETORNO")
	private String									eRetorno;

	@SerializedName("E_MENSAGEM")
	private String									eMensagem;

	@SerializedName("JCO_ERROR_SYSTEM_FAILURE")
	private Map<String, Object>						jcoError;

	@SerializedName(Constants.TBL_RECUSA_NF)//ZWH_RECUSA_DOC
	private List<SAPRecusaDocDTO>					recusaNFDTOs;

	@SerializedName(Constants.I_LISTA_VEICULOS)//ZWH_INVENT
	private List<SAPVehicleDTO>						listaVeiculoDTOs;

	/**
	 * @return the checkInOutDTOs
	 */
	public List<SAPCheckInOutDTO> getCheckInOutDTOs() {
		return checkInOutDTOs;
	}

	/**
	 * @return the checkInOutPortariaDTOs
	 */
	public List<SAPCheckInOutPortariaDTO> getCheckInOutPortariaDTOs() {
		return checkInOutPortariaDTOs;
	}

	public List<SAPConfCegaMsgDTO> getConfCegaDTOs() {
		return confCegaDTOs;
	}

	public List<SAPControleQualidadeDTO> getControleQualidadeDTOs() {
		return controleQualidadeDTOs;
	}

	public List<SAPCustomerDTO> getCustomerDTOs() {
		return customerDTOs;
	}

	public List<SAPDocumentDTO> getDocumentDTOs() {
		return documentDTOs;
	}

	public List<SAPDocumentItemDTO> getDocumentItemDTOs() {
		return documentItemDTOs;
	}

	public String geteReturn() {
		return eReturn;
	}

	public List<SAPInventoryItem> getItensInventarioDTOs() {
		return itensInventarioDTOs;
	}

	public List<SAPItemListInventarioDTO> getListaInventarioDTOs() {
		return listaInventarioDTOs;
	}

	/**
	 * @return the listaVeiculoDTOs
	 */
	public List<SAPVehicleDTO> getListaVeiculoDTOs() {
		return listaVeiculoDTOs;
	}

	public List<SAPProductionOrderConsumptionDTO> getProdOrderConsDTOs() {
		return prodOrderConsDTOs;
	}

	public List<SAPProductionOrderProductionDTO> getProdOrderPrdDTOs() {
		return prodOrderPrdDTOs;
	}

	public List<SAPProductDTO> getProductDTOs() {
		return productDTOs;
	}

	public List<SAPProductPropertyDTO> getProductPropertyDTOs() {
		return productPropertyDTOs;
	}

	public List<SAPReadControleDTO> getReadControleDTOs() {
		return readControleDTOs;
	}

	public List<SAPReadStartDTO> getReadStartDTOs() {
		return readStartDTOs;
	}

	public LinkedHashMap<String, Object> getResultList() {
		return resultList;
	}

	public List<SAPReturnDTO> getReturnDTOs() {
		return returnDTOs;
	}

	public List<SAPSupplierDTO> getSupplierDTOs() {
		return supplierDTOs;
	}

	public List<SAPTransferMPDTO> getTransferMPDTOs() {
		return transferMPDTOs;
	}

	/**
	 * @param checkInOutDTOs the checkInOutDTOs to set
	 */
	public void setCheckInOutDTOs(List<SAPCheckInOutDTO> checkInOutDTOs) {
		this.checkInOutDTOs = checkInOutDTOs;
	}

	/**
	 * @param checkInOutPortariaDTOs the checkInOutPortariaDTOs to set
	 */
	public void setCheckInOutPortariaDTOs(List<SAPCheckInOutPortariaDTO> checkInOutPortariaDTOs) {
		this.checkInOutPortariaDTOs = checkInOutPortariaDTOs;
	}

	public void setConfCegaDTOs(List<SAPConfCegaMsgDTO> confCegaDTOs) {
		this.confCegaDTOs = confCegaDTOs;
	}

	public void setControleQualidadeDTOs(List<SAPControleQualidadeDTO> controleQualidadeDTOs) {
		this.controleQualidadeDTOs = controleQualidadeDTOs;
	}

	public void setCustomerDTOs(List<SAPCustomerDTO> customerDTOs) {
		this.customerDTOs = customerDTOs;
	}

	public void setDocumentDTOs(List<SAPDocumentDTO> documentDTOs) {
		this.documentDTOs = documentDTOs;
	}

	public void setDocumentItemDTOs(List<SAPDocumentItemDTO> documentItemDTOs) {
		this.documentItemDTOs = documentItemDTOs;
	}

	public void seteReturn(String eReturn) {
		this.eReturn = eReturn;
	}

	public void setItensInventarioDTOs(List<SAPInventoryItem> itensInventarioDTOs) {
		this.itensInventarioDTOs = itensInventarioDTOs;
	}

	public void setListaInventarioDTOs(List<SAPItemListInventarioDTO> listaInventarioDTOs) {
		this.listaInventarioDTOs = listaInventarioDTOs;
	}

	/**
	 * @param listaVeiculoDTOs the listaVeiculoDTOs to set
	 */
	public void setListaVeiculoDTOs(List<SAPVehicleDTO> listaVeiculoDTOs) {
		this.listaVeiculoDTOs = listaVeiculoDTOs;
	}

	public void setProdOrderConsDTOs(List<SAPProductionOrderConsumptionDTO> prodOrderConsDTOs) {
		this.prodOrderConsDTOs = prodOrderConsDTOs;
	}

	public void setProdOrderPrdDTOs(List<SAPProductionOrderProductionDTO> ordPrdPrdDTOs) {
		this.prodOrderPrdDTOs = ordPrdPrdDTOs;
	}

	public void setProductDTOs(List<SAPProductDTO> productDTOs) {
		this.productDTOs = productDTOs;
	}

	public void setProductPropertyDTOs(List<SAPProductPropertyDTO> productPropertyDTOs) {
		this.productPropertyDTOs = productPropertyDTOs;
	}

	public void setReadControleDTOs(List<SAPReadControleDTO> readControleDTOs) {
		this.readControleDTOs = readControleDTOs;
	}

	public void setReadStartDTOs(List<SAPReadStartDTO> readStartDTOs) {
		this.readStartDTOs = readStartDTOs;
	}

	public void setResultList(LinkedHashMap<String, Object> resultList) {
		this.resultList = resultList;
	}

	public void setReturnDTOs(List<SAPReturnDTO> retTransferMPDTOs) {
		this.returnDTOs = retTransferMPDTOs;
	}

	public void setSupplierDTOs(List<SAPSupplierDTO> supplierDTOs) {
		this.supplierDTOs = supplierDTOs;
	}

	public void setTransferMPDTOs(List<SAPTransferMPDTO> devMPDTOs) {
		this.transferMPDTOs = devMPDTOs;
	}

	/**
	 * @return the eMensagem
	 */
	public String geteMensagem() {
		return eMensagem;
	}

	/**
	 * @param eMensagem the eMensagem to set
	 */
	public void seteMensagem(String eMensagem) {
		this.eMensagem = eMensagem;
	}

	/**
	 * @return the recusaNFDTOs
	 */
	public List<SAPRecusaDocDTO> getRecusaNFDTOs() {
		return recusaNFDTOs;
	}

	/**
	 * @param recusaNFDTOs the recusaNFDTOs to set
	 */
	public void setRecusaNFDTOs(List<SAPRecusaDocDTO> recusaNFDTOs) {
		this.recusaNFDTOs = recusaNFDTOs;
	}

	/**
	 * @return the eRetorno
	 */
	public String geteRetorno() {
		return eRetorno;
	}

	/**
	 * @param eRetorno the eRetorno to set
	 */
	public void seteRetorno(String eRetorno) {
		this.eRetorno = eRetorno;
	}

	/**
	 * @return the prontaEntregaDTOs
	 */
	public List<SAPProntaEntregaDTO> getProntaEntregaDTOs() {
		return prontaEntregaDTOs;
	}

	/**
	 * @param prontaEntregaDTOs the prontaEntregaDTOs to set
	 */
	public void setProntaEntregaDTOs(List<SAPProntaEntregaDTO> prontaEntregaDTOs) {
		this.prontaEntregaDTOs = prontaEntregaDTOs;
	}

	/**
	 * @return the checkoutFaturadoItemDTOs
	 */
	public List<SAPCheckoutFaturadoItemDTO> getCheckoutFaturadoItemDTOs() {
		return checkoutFaturadoItemDTOs;
	}

	/**
	 * @param checkoutFaturadoItemDTOs the checkoutFaturadoItemDTOs to set
	 */
	public void setCheckoutFaturadoItemDTOs(List<SAPCheckoutFaturadoItemDTO> checkoutFaturadoItemDTOs) {
		this.checkoutFaturadoItemDTOs = checkoutFaturadoItemDTOs;
	}

	/**
	 * @return the jcoError
	 */
	public Map<String, Object> getJcoError() {
		return jcoError;
	}

	/**
	 * @param jcoError the jcoError to set
	 */
	public void setJcoError(Map<String, Object> jcoError) {
		this.jcoError = jcoError;
	}

	public List<SAPVasilhameGeralDTO> getVasilhameGeralDTO() {
		return vasilhameGeralDTOs;
	}

}
