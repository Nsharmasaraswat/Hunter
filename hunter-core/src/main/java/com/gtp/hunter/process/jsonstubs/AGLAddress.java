package com.gtp.hunter.process.jsonstubs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLAddress extends AGLBase {

	/*
	 * {
	 *		"id":"0e25085b-01fb-11e9-8cf2-b88584fbf977",
	 *		"metaname":"address",
	 *		"created_at":"2018-12-17T00:00:00",
	 *		"updated_at":"2018-12-17T00:00:00",
	 *		"status":"NOVO",
	 *		"parent_id":"",
	 *		"name":"ENDERECO",
	 *		"product_model_id":"0f52b4cd-01ef-11e9-8cf2-b88584fbf977",
	 *		"properties":{
	 *			"address":"ENDERECO ",
	 * 			"description":"ENDERECO ",
	 * 			"capacity":0,
	 * 			"volume_type":"",
	 * 			"address_type":1,
	 * 			"capacity_height":5,
	 * 			"capacity_width":2,
	 * 			"capacity_length":2,
	 * 			"corridor":"",
	 * 			"warehouse":"",
	 * 			"road_id":"0e25085b-01fb-11e9-8cf2-b88584fbf977",
	 * 			"order":1
	 *		}
	 *	} 
	 */

	@Expose
	private UUID				id;
	@Expose
	private String				metaname;
	@Expose
	private String				status;
	@Expose
	private UUID				parent_id;
	@Expose
	private String				name;
	@Expose
	private UUID				product_model_id;
	@Expose
	private List<AGLAddress>	siblings	= new LinkedList<AGLAddress>();
	@Expose
	private Map<String, String>	properties;

	public AGLAddress() {
		properties = new HashMap<>();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getMetaname() {
		return metaname;
	}

	public void setMetaname(String metaname) {
		this.metaname = metaname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public UUID getParent_id() {
		return parent_id;
	}

	public void setParent_id(UUID parent_id) {
		this.parent_id = parent_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getProduct_model_id() {
		return product_model_id;
	}

	public void setProduct_model_id(UUID product_model_id) {
		this.product_model_id = product_model_id;
	}

	/**
	 * @return the siblings
	 */
	public List<AGLAddress> getSiblings() {
		return siblings;
	}

	/**
	 * @param siblings the siblings to set
	 */
	public void setSiblings(List<AGLAddress> siblings) {
		this.siblings = siblings;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

}
