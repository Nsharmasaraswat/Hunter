package com.gtp.hunter.process.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "documenttransport")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentTransport extends UUIDAuditModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id")
	@Expose(serialize = false)
	@JsonIgnore
	private Document	document;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "thing_id")
	@Expose(serialize = false)
	private Thing		thing;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "address_id")
	@Expose(serialize = false)
	private Address		address;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "origin_id")
	@Expose(serialize = false)
	private Address		origin;

	@Expose
	private int			seq;

	@Transient
	@Expose
	private UUID		thing_id;

	@Transient
	@Expose
	private UUID		address_id;

	@Transient
	@Expose
	private UUID		origin_id;

	public DocumentTransport() {
	}

	public DocumentTransport(Document d, int seq, Thing t, Address a) {
		setDocument(d);
		setSeq(seq);
		setThing(t);
		setOrigin(t.getAddress());
		setAddress(a);
	}

	public DocumentTransport(Document d, int seq, Thing t, Address a, Address o) {
		setDocument(d);
		setSeq(seq);
		setThing(t);
		setOrigin(o);
		setAddress(a);
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
		this.thing_id = thing.getId();
		setOrigin(thing.getAddress());
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
		this.address_id = address == null ? null : address.getId();
	}

	public UUID getThing_id() {
		return thing_id;
	}

	public void setThing_id(UUID thing_id) {
		this.thing_id = thing_id;
	}

	public UUID getAddress_id() {
		return address_id;
	}

	public void setAddress_id(UUID address_id) {
		this.address_id = address_id;
	}

	public UUID getOrigin_id() {
		return origin_id;
	}

	public void setOrigin_id(UUID origin_id) {
		this.origin_id = origin_id;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	/**
	 * @return the origin
	 */
	public Address getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(Address origin) {
		this.origin = origin;
		this.origin_id = origin == null ? null : origin.getId();
	}
}
