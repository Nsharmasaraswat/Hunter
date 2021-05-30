package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SAPReturnDTO extends HeaderTableSapDTO {

	@Expose
	@SerializedName("MENSAGEM")
	private String	mensagem;

	@Expose
	@SerializedName("SEQ")
	private String	seq;

	@Expose
	@SerializedName("TIPO")
	private String	tipo;

	@Expose
	@SerializedName("TYPE")
	private String	type;

	@Expose
	@SerializedName("ID")
	private String	id;

	@Expose
	@SerializedName("NUMBER")
	private String	number;

	@Expose
	@SerializedName("MESSAGE")
	private String	message;

	@Expose
	@SerializedName("LOG_NO")
	private String	log_no;

	@Expose
	@SerializedName("LOG_MSG_NO")
	private String	log_msg_no;

	@Expose
	@SerializedName("MESSAGE_V1")
	private String	message_v1;

	@Expose
	@SerializedName("MESSAGE_V2")
	private String	message_v2;

	@Expose
	@SerializedName("MESSAGE_V3")
	private String	message_v3;

	@Expose
	@SerializedName("MESSAGE_V4")
	private String	message_v4;

	@Expose
	@SerializedName("ROW")
	private String	row;

	@Expose
	@SerializedName("FIELD")
	private String	field;

	@Expose
	@SerializedName("SYSTEM")
	private String	system;

	/**
	 * @return the mensagem
	 */
	public String getMensagem() {
		return mensagem;
	}

	/**
	 * @param mensagem the mensagem to set
	 */
	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	/**
	 * @return the seq
	 */
	public String getSeq() {
		return seq;
	}

	/**
	 * @param seq the seq to set
	 */
	public void setSeq(String seq) {
		this.seq = seq;
	}

	/**
	 * @return the tipo
	 */
	public String getTipo() {
		return tipo;
	}

	/**
	 * @param tipo the tipo to set
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number the number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the log_no
	 */
	public String getLog_no() {
		return log_no;
	}

	/**
	 * @param log_no the log_no to set
	 */
	public void setLog_no(String log_no) {
		this.log_no = log_no;
	}

	/**
	 * @return the log_msg_no
	 */
	public String getLog_msg_no() {
		return log_msg_no;
	}

	/**
	 * @param log_msg_no the log_msg_no to set
	 */
	public void setLog_msg_no(String log_msg_no) {
		this.log_msg_no = log_msg_no;
	}

	/**
	 * @return the message_v1
	 */
	public String getMessage_v1() {
		return message_v1;
	}

	/**
	 * @param message_v1 the message_v1 to set
	 */
	public void setMessage_v1(String message_v1) {
		this.message_v1 = message_v1;
	}

	/**
	 * @return the message_v2
	 */
	public String getMessage_v2() {
		return message_v2;
	}

	/**
	 * @param message_v2 the message_v2 to set
	 */
	public void setMessage_v2(String message_v2) {
		this.message_v2 = message_v2;
	}

	/**
	 * @return the message_v3
	 */
	public String getMessage_v3() {
		return message_v3;
	}

	/**
	 * @param message_v3 the message_v3 to set
	 */
	public void setMessage_v3(String message_v3) {
		this.message_v3 = message_v3;
	}

	/**
	 * @return the message_v4
	 */
	public String getMessage_v4() {
		return message_v4;
	}

	/**
	 * @param message_v4 the message_v4 to set
	 */
	public void setMessage_v4(String message_v4) {
		this.message_v4 = message_v4;
	}

	/**
	 * @return the row
	 */
	public String getRow() {
		return row;
	}

	/**
	 * @param row the row to set
	 */
	public void setRow(String row) {
		this.row = row;
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the system
	 */
	public String getSystem() {
		return system;
	}

	/**
	 * @param system the system to set
	 */
	public void setSystem(String system) {
		this.system = system;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create().toJson(this);
	}
}
