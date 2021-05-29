package com.gtp.hunter.process.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "documentitem")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentItem extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "document_id")
	@Expose(serialize = false)
	@JsonIgnore
	private Document			document;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	@Expose
	private Product				product;

	@Expose
	private String				measureUnit;

	@Expose
	private double				qty;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "documentitemproperty", joinColumns = @JoinColumn(name = "documentitem_id"))
	@MapKeyJoinColumn(name = "prop")
	@Fetch(FetchMode.SUBSELECT)
	@Column(name = "value")
	@Expose
	private Map<String, String>	properties	= new HashMap<String, String>();

	@Transient
	@Expose
	private List<Thing>			things		= new ArrayList<Thing>();

	@Transient
	@Expose
	private int					qtdThings;

	public DocumentItem() {
	}

	public DocumentItem(Document d, Product p, double qty, String status) {
		setDocument(d);
		setProduct(p);
		setQty(qty);
		setStatus(status);
	}

	public DocumentItem(Document d, Product p, double qty, String status, String measureUnit) {
		this(d, p, qty, status);
		setMeasureUnit(measureUnit);
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public double getQty() {
		return qty;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public List<Thing> getThings() {
		return things;
	}

	public void setThings(List<Thing> things) {
		this.things = things;
	}

	public int getQtdThings() {
		return qtdThings;
	}

	public void setQtdThings(int qtdThings) {
		this.qtdThings = qtdThings;
	}

	public String getMeasureUnit() {
		return measureUnit;
	}

	public void setMeasureUnit(String measureUnit) {
		this.measureUnit = measureUnit;
	}
}
