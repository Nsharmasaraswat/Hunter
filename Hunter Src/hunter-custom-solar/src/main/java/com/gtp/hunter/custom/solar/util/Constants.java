package com.gtp.hunter.custom.solar.util;

public class Constants {

	public static final String	RFC_INFORMACAO				= "Z_HW_INFORMACAO_CHEGADA";
	public static final String	RFC_OGC						= "Z_HW_OGC";
	public static final String	RFC_PSC						= "Z_HW_PSC";
	public static final String	RFC_RESERVA					= "Z_HW_RESERVE_OF_MATERIAL";
	public static final String	RFC_BAIXA_RESERVA			= "Z_HW_BAIXA_RESERVA";
	public static final String	RFC_CONFERENCIA_CEGA		= "Z_HW_CONFERENCIA_CEGA";
	public static final String	RFC_CONFCEGATRANSP			= "Z_HW_CONFERENCIA_CEGA_TRANSP";
	public static final String	RFC_TRANSFERENCIA			= "Z_HW_DEVOLUCAO_MP";
	public static final String	RFC_START					= "Z_HW_READ_START";
	public static final String	RFC_CONTROLE				= "Z_HW_READ_CONTROLE";
	public static final String	RFC_QUALIDADE				= "Z_HW_CONTROLE_QA";
	public static final String	RFC_PRODUTOS				= "Z_HW_PRODUTOS";
	public static final String	RFC_REPROCESSO_NF			= "Z_HW_PROCESSO_NOTA_INDIVIDUAL";
	public static final String	RFC_DELETE_CONF_CEGA		= "Z_HW_DELETE_START_CONF_CEGA";
	public static final String	RFC_INVENTARIO				= "Z_HW_INFORMACAO_INVENTARIO";
	public static final String	RFC_CHECKINOUT				= "Z_HW_CHECKIN_CHECKOUT";
	public static final String	RFC_CHECKINOUT_PORTARIA		= "Z_HW_CHECKIN_CHECKOUT_PORTARIA";
	public static final String	RFC_VEICULOS				= "Z_HW_VEICULOS";
	public static final String	RFC_RECUSA_NF				= "Z_HW_RECUSA_DOC";
	public static final String	RFC_PRONTA_ENTREGA			= "Z_HW_PRONTA_ENTREGA";
	public static final String	RFC_VASILHAME_GERAL			= "Z_HW_SWITCH_VAS";
	public static final String	RFC_CHECKOUT_FATURADO		= "Z_HW_CHECKOUT_FATURADO";

	public static final String	TBL_CONFERENCIA_CEGA_TRANSP	= "T_ZWH_CONFCEGATRAN";
	public static final String	TBL_CONFERENCIA_CEGA		= "T_ZWH_CONFCEGA";
	public static final String	TBL_TRANSFERENCIA			= "T_ZWH_DEVMP";
	public static final String	TBL_QUALIDADE				= "T_ZWH_CTLQA";
	public static final String	TBL_CHECKINOUT				= "T_ZWH_CHECK_IN_OUT";
	public static final String	TBL_CHECKINOUT_PORTARIA		= "T_ZWH_CHECK_PORTA";
	public static final String	TBL_RECUSA_NF				= "T_ZWH_RECUSA_DOC";
	public static final String	TBL_PRONTA_ENTREGA			= "T_ZWH_PENT";
	public static final String	TBL_VASILHAME_GERAL			= "T_ZWH_SWITCH_VAS";
	public static final String	TBL_CHECKOUT_FATURADO		= "T_ZWH_CHECKOUT_TOT";
	public static final String	T_RETURN					= "T_RETURN";

	public static final String	I_CONTROLE					= "I_CONTROLE";
	public static final String	I_EUCATEX					= "I_EUCATEX";
	public static final String	I_PALLET					= "I_PALLETS";
	public static final String	I_LACRE						= "I_LACRE";
	public static final String	I_CHAVE						= "I_CHAVE";
	public static final String	I_CODE						= "I_CODE";
	public static final String	I_EXPAND_LOT				= "I_EXPAND_LOT";
	public static final String	I_FLUXO						= "I_FLUXO";
	public static final String	I_LISTA_INVENTARIO			= "I_LISTA_INVENTARIO";
	public static final String	I_LISTA_VEICULOS			= "I_LISTA_VEICULOS";
	public static final String	I_MASSIVA					= "I_MASSIVA";
	public static final String	I_ORDER_NUMBER				= "I_ORDER_NUMBER";
	public static final String	I_PLANT						= "I_PLANT";
	public static final String	I_QTD_PRODUCED				= "I_QTD_PRODUCED";
	public static final String	I_REGISTRO					= "I_REGISTRO";
	public static final String	I_TKNUM						= "I_TKNUM";

	public static final String	CODE						= "CODE";
	public static final String	CONTROLE					= "CONTROLE";
	public static final String	CODE_MSG					= "CODE_MSG";
	public static final String	MSG							= "MSG";
	public static final String	ORIGEM						= "ORIGEM";

	public static final int		CODE_NF						= 1;
	public static final int		CODE_TRANSFERENCIA			= 3;
	public static final int		CODE_CONTROLEQUALIDADE		= 5;
	public static final int		CODE_INVENTARIO				= 7;
	public static final int		CODE_PLANPROD				= 8;
	public static final int		CODE_VEICULO				= 13;
	public static final int		CODE_SOLICRESERVA			= 14;
	public static final int		CODE_RECUSA_NF				= 15;
	public static final int		CODE_PRONTA_ENTREGA			= 16;

	public static final int		NF_DIR_ENTRADA				= 1;
	public static final int		NF_DIR_SAIDA				= 2;
	public static final int		NF_DIR_SAIDADEV				= 3;
	public static final int		NF_DIR_ENTRADADEV			= 4;
	public static final int		NF_DIR_DEVSAIDA				= 5;

}
