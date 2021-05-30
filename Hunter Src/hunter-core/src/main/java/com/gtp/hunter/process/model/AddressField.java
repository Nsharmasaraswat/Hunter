package com.gtp.hunter.process.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "addressfield", uniqueConstraints = {
		@UniqueConstraint(columnNames = {
				"address_id",
				"addressmodelfield_id"
		})
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressField extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "address_id", nullable = false)
	@Expose(serialize = false)
	@JsonIgnore
	private Address				address;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "addressmodelfield_id", nullable = false)
	@Expose
	private AddressModelField	model;

	@Transient
	@Expose
	@SerializedName("modelfield_id")
	private String				modelfield_id;

	private String				value;

	/**
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(Address address) {
		this.address = address;
	}

	/**
	 * @return the model
	 */
	public AddressModelField getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(AddressModelField model) {
		this.model = model;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@JsonGetter("modelfield_id")
	public String getModelfield_id() {
		if (this.model != null && this.model.getId() != null) {
			return this.model.getId().toString();
		} else {
			return modelfield_id;
		}
	}

	@JsonSetter("modelfield_id")
	public void setModelfield_id(String modelfieldid) {
		this.modelfield_id = modelfieldid;
	}
}
