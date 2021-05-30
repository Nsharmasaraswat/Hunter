package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "addressmodel")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressModel extends UUIDAuditModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@Expose(serialize = false)
	@JsonIgnore
	private AddressModel			parent;

	@Transient
	private Set<AddressModel>		siblings	= new HashSet<AddressModel>();

	@OneToMany(mappedBy = "model", targetEntity = AddressModelField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose
	private Set<AddressModelField>	fields		= new HashSet<AddressModelField>();

	private String					classe;

	public AddressModel getParent() {
		return parent;
	}

	public void setParent(AddressModel parent) {
		this.parent = parent;
	}

	public Set<AddressModel> getSiblings() {
		return siblings;
	}

	public void setSiblings(Set<AddressModel> siblings) {
		this.siblings = siblings;
	}

	public Set<AddressModelField> getFields() {
		return fields;
	}

	public void setFields(Set<AddressModelField> fields) {
		this.fields = fields;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

}
