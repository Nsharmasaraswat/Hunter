package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPVehicleDTO extends HeaderTableSapDTO {

	@SerializedName("TKNUM")//(Nº transporte)
	private String	transporte;

	@SerializedName("CODIGO")//(Nº equipamento)
	private String	codigo;

	@SerializedName("TAG")//(Nº identificação para o veículo)
	private String	tag;

	@SerializedName("PLACA")//(Placa de veículo)
	private String	placa;

	@SerializedName("COMPARTIMENTOS")//(Campo de caracteres do comprimento 10)
	private String	compartimentos;

	@SerializedName("CAPACIDADE")//(Peso total permitido)
	private double	capacidade;

	@SerializedName("STATUS")//(Campo de caracteres do comprimento 10)
	private String	status;

	@SerializedName("PM_CARROCERIA")//(Campo de texto comprimento 200)
	private String	carroceria;

	@SerializedName("PM_TIPO_RODADO")//(Campo de texto comprimento 200)
	private String	tipo_rodado;

	@SerializedName("TIPO_VEICULO")//(Tipo de veículo)
	private String	tipoVeiculo;

	/**
	 * @return the transporte
	 */
	public String getTransporte() {
		return transporte;
	}

	/**
	 * @param transporte the transporte to set
	 */
	public void setTransporte(String transporte) {
		this.transporte = transporte;
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
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the placa
	 */
	public String getPlaca() {
		return placa;
	}

	/**
	 * @param placa the placa to set
	 */
	public void setPlaca(String placa) {
		this.placa = placa;
	}

	/**
	 * @return the compartimentos
	 */
	public String getCompartimentos() {
		return compartimentos;
	}

	/**
	 * @param compartimentos the compartimentos to set
	 */
	public void setCompartimentos(String compartimentos) {
		this.compartimentos = compartimentos;
	}

	/**
	 * @return the capacidade
	 */
	public double getCapacidade() {
		return capacidade;
	}

	/**
	 * @param capacidade the capacidade to set
	 */
	public void setCapacidade(double capacidade) {
		this.capacidade = capacidade;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the carroceria
	 */
	public String getCarroceria() {
		return carroceria;
	}

	/**
	 * @param carroceria the carroceria to set
	 */
	public void setCarroceria(String carroceria) {
		this.carroceria = carroceria;
	}

	/**
	 * @return the tipo_rodado
	 */
	public String getTipo_rodado() {
		return tipo_rodado;
	}

	/**
	 * @param tipo_rodado the tipo_rodado to set
	 */
	public void setTipo_rodado(String tipo_rodado) {
		this.tipo_rodado = tipo_rodado;
	}

	/**
	 * @return the tipoVeiculo
	 */
	public String getTipoVeiculo() {
		return tipoVeiculo;
	}

	/**
	 * @param tipoVeiculo the tipoVeiculo to set
	 */
	public void setTipoVeiculo(String tipoVeiculo) {
		this.tipoVeiculo = tipoVeiculo;
	}
}