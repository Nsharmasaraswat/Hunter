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
@Table(name = "personfield", uniqueConstraints = {
		@UniqueConstraint(columnNames = {
				"person_id",
				"personmodelfield_id"
		})
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonField extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "person_id", nullable = false)
	@Expose(serialize = false)
	@JsonIgnore
	private Person				person;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "personmodelfield_id", nullable = false)
	@Expose
	private PersonModelField	field;

	@Transient
	@Expose
	@SerializedName("modelfield_id")
	private String				modelfield_id;

	@Expose
	private String				value;

	public PersonField() {
	}

	public PersonField(Person p, PersonModelField psmf, String v) {
		setPerson(p);
		setField(psmf);
		setValue(v);
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public PersonModelField getField() {
		return field;
	}

	public void setField(PersonModelField field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@JsonGetter("modelfield_id")
	public String getModelfield_id() {
		if (this.field != null && this.field.getId() != null) {
			return this.field.getId().toString();
		} else {
			return modelfield_id;
		}
	}

	@JsonSetter("modelfield_id")
	public void setModelfield_id(String modelfieldid) {
		this.modelfield_id = modelfieldid;
	}
}
