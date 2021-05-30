package com.gtp.hunter.process.model;

import javax.persistence.Entity;
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
@Table(name = "property", uniqueConstraints = {
		@UniqueConstraint(columnNames = {
				"thing_id",
				"propertymodelfield_id"
		})
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Property extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "thing_id", nullable = false)
	@Expose(serialize = false)
	@JsonIgnore
	private Thing				thing;

	@ManyToOne
	@JoinColumn(name = "propertymodelfield_id", nullable = false)
	@Expose()
	private PropertyModelField	field;

	@Transient
	@Expose
	@SerializedName("modelfield_id")
	private String				modelfield_id;

	@Expose()
	private String				value;

	public Property() {
	}

	public Property(Thing thing, PropertyModelField field, String value) {
		this.setThing(thing);
		this.setField(field);
		this.setValue(value);
	}

	public Property(Thing thing, PropertyModelField field, String value, String status) {
		this.setThing(thing);
		this.setField(field);
		this.setValue(value);
		this.setStatus(status);
	}

	public Thing getThing() {
		return thing;
	}

	public void setThing(Thing thing) {
		this.thing = thing;
	}

	public PropertyModelField getField() {
		return field;
	}

	public void setField(PropertyModelField field) {
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
