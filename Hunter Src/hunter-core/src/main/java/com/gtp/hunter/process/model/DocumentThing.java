package com.gtp.hunter.process.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "documentthing", uniqueConstraints = {
		@UniqueConstraint(columnNames = {
				"document_id",
				"thing_id"
		})
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentThing extends UUIDAuditModel {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "document_id")
	@Expose(serialize = false)
	@JsonIgnore
	private Document	document;

	@Expose
	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "thing_id")
	private Thing		thing;

	@Transient
	@Expose()
	private String		unit;

	@Transient
	@Expose()
	private String		sku;

	@Transient
	@Expose()
	private String		description;

	@Transient
	@Expose()
	private String		tstatus;

	@Transient
	@Expose()
	private String		tLot;

	public DocumentThing() {
	}

	public DocumentThing(Document document, Thing thing, String status) {
		setDocument(document);
		setThing(thing);
		setStatus(status);
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Thing getThing() {
		return thing;
	}

	public void setThing(Thing thing) {
		this.thing = thing;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTstatus() {
		return tstatus;
	}

	public void setTstatus(String tstatus) {
		this.tstatus = tstatus;
	}

	public String gettLot() {
		return tLot;
	}

	public void settLot(String tLot) {
		this.tLot = tLot;
	}
}
