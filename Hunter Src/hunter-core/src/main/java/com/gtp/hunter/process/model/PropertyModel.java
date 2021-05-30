package com.gtp.hunter.process.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name="propertymodel")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyModel extends UUIDAuditModel{
	
	@OneToMany(mappedBy = "model", targetEntity = PropertyModelField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Expose
	@Fetch(FetchMode.SUBSELECT)
	private Set<PropertyModelField> fields;
	
	public PropertyModel() {  }
	
	public PropertyModel(String name, String metaname) {
		this.setName(name);
		this.setMetaname(metaname);
	}
	
	public Set<PropertyModelField> getFields() {
		return fields;
	}

	public void setFields(Set<PropertyModelField> fields) {
		this.fields = fields;
	}
	
}
