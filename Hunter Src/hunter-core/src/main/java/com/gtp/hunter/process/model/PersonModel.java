package com.gtp.hunter.process.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "personmodel")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonModel extends UUIDAuditModel {

	@Expose
	@ManyToOne
	@JoinColumn(name = "parent_id")
	private PersonModel				parent;

	@Expose
	@Fetch(FetchMode.SUBSELECT)
	@OneToMany(mappedBy = "model", targetEntity = PersonModelField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<PersonModelField>	fields;

	@Expose
	private String					codedesc;

	public PersonModel getParent() {
		return parent;
	}

	public void setParent(PersonModel parent) {
		this.parent = parent;
	}

	public Set<PersonModelField> getFields() {
		return fields;
	}

	public void setFields(Set<PersonModelField> fields) {
		this.fields = fields;
	}

	public String getCodedesc() {
		return codedesc;
	}

	public void setCodedesc(String codedesc) {
		this.codedesc = codedesc;
	}

}
