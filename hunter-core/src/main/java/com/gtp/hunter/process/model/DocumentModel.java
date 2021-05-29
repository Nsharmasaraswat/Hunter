package com.gtp.hunter.process.model;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "documentmodel")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentModel extends UUIDAuditModel {

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name = "parent_id")
	@Expose()
	private DocumentModel	parent;

	@Expose()
	private String			classe;

	@OneToMany(mappedBy = "model", targetEntity = DocumentModelField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose()	
	@OrderBy("ordem")
	private SortedSet<DocumentModelField>	fields = new ConcurrentSkipListSet<DocumentModelField>();

	public DocumentModel() {}

	public DocumentModel(String name, String meta) {
		this.setName(name);
		this.setMetaname(meta);
	}

	public DocumentModel(String name, String meta, DocumentModel parent) {
		this.setName(name);
		this.setMetaname(meta);
		this.setParent(parent);
	}

	public DocumentModel getParent() {
		return parent;
	}

	public void setParent(DocumentModel parent) {
		this.parent = parent;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public SortedSet<DocumentModelField> getFields() {
		return fields;
	}

	public void setFields(SortedSet<DocumentModelField> fields) {
		this.fields = fields;
	}

}
