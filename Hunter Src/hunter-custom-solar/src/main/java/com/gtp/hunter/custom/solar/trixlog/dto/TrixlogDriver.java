package com.gtp.hunter.custom.solar.trixlog.dto;

import com.google.gson.annotations.Expose;

public class TrixlogDriver {
	@Expose
	int					id;//": 123,

	@Expose
	String				cardId;//":"AFFF1020",

	@Expose
	String				name;//":"Jhon Doe"

	@Expose
	String				type;//": "DRIVER",

	@Expose
	String				login;//": "41634453077",

	@Expose
	String				phone;//": "+5585987226823",

	@Expose
	TrixlogOrganization	organization;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the cardId
	 */
	public String getCardId() {
		return cardId;
	}

	/**
	 * @param cardId the cardId to set
	 */
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the organization
	 */
	public TrixlogOrganization getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(TrixlogOrganization organization) {
		this.organization = organization;
	}
}
