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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "person")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "personmodel_id")
	@Expose()
	private PersonModel			model;

	@Expose()
	private String				code;

	@OneToMany(mappedBy = "person", targetEntity = PersonField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose()
	private Set<PersonField>	fields	= new HashSet<PersonField>();

	public Person() {
	}

	public Person(String name, PersonModel perm, String code, String status) {
		setName(name);
		setModel(perm);
		setCode(code);
		setStatus(status);
	}

	public PersonModel getModel() {
		return model;
	}

	public void setModel(PersonModel model) {
		this.model = model;
	}

	public Set<PersonField> getFields() {
		return fields;
	}

	public void setFields(Set<PersonField> fields) {
		this.fields = fields;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
