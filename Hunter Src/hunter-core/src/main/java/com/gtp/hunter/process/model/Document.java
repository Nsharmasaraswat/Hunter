package com.gtp.hunter.process.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
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
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.core.model.User;

@Entity
@Table(name = "document")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document extends UUIDAuditModel {

	@Expose
	@Basic(fetch = FetchType.EAGER)
	private String					code;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "documentmodel_id")
	@Expose
	private DocumentModel			model;

	@OneToMany(mappedBy = "document", targetEntity = DocumentItem.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@NotFound(action = NotFoundAction.IGNORE)
	@Expose()
	private Set<DocumentItem>		items		= new HashSet<DocumentItem>();

	@OneToMany(mappedBy = "document", targetEntity = DocumentThing.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@NotFound(action = NotFoundAction.IGNORE)
	@Expose
	private Set<DocumentThing>		things		= new HashSet<DocumentThing>();

	@OneToMany(mappedBy = "document", targetEntity = DocumentField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@NotFound(action = NotFoundAction.IGNORE)
	@Expose
	private Set<DocumentField>		fields		= new HashSet<DocumentField>();

	@OneToMany(mappedBy = "document", targetEntity = DocumentTransport.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@NotFound(action = NotFoundAction.IGNORE)
	@Expose
	private Set<DocumentTransport>	transports	= new HashSet<DocumentTransport>();

	@OneToMany(mappedBy = "parent", targetEntity = Document.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@NotFound(action = NotFoundAction.IGNORE)
	@Expose
	private Set<Document>			siblings	= new HashSet<Document>();

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "person_id")
	@Expose
	private Person					person;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@Expose
	private User					user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@Expose(serialize = false)
	@SerializedName("parent")
	@JsonIgnore
	private Document				parent;

	@Transient
	@Expose
	@SerializedName("props")
	private Map<String, String>		props;

	@Transient
	@Expose
	@SerializedName("parent_id")
	private String					parent_id;

	public Document() {
	}

	public Document(DocumentModel mdl, String name, String code, String status) {
		this.setModel(mdl);
		this.setName(name);
		this.setCode(code);
		this.setStatus(status);
		this.items = new HashSet<>();
	}

	public DocumentModel getModel() {
		return model;
	}

	public void setModel(DocumentModel model) {
		this.model = model;
	}

	public Set<DocumentItem> getItems() {
		return items;
	}

	public void setItems(Set<DocumentItem> items) {
		this.items = items;
	}

	public Set<DocumentThing> getThings() {
		return things;
	}

	public void setThings(Set<DocumentThing> things) {
		this.things = things;
	}

	public Set<DocumentField> getFields() {
		return fields;
	}

	public void setFields(Set<DocumentField> fields) {
		this.fields = fields;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Document getParent() {
		return parent;
	}

	public void setParent(Document parent) {
		this.parent = parent;
	}

	public Set<DocumentTransport> getTransports() {
		return transports;
	}

	public void setTransports(Set<DocumentTransport> transports) {
		this.transports = transports;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Set<Document> getSiblings() {
		return siblings;
	}

	public void setSiblings(Set<Document> siblings) {
		this.siblings = siblings;
	}

	@Override
	public boolean equals(Object arg0) {
		return this.getId().equals(((Document) arg0).getId());
	}

	@Override
	public String getMetaname() {
		if (super.getMetaname() == null && getModel() != null)
			return getModel().getMetaname();
		return super.getMetaname();
	}

	public Map<String, String> getProps() {
		props = new HashMap<String, String>();
		for (DocumentField df : fields) {
			props.put(df.getField().getMetaname().toLowerCase(), df.getValue());
		}
		return props;
	}

	@JsonGetter("parent_id")
	public String getParent_id() {
		if (this.parent != null && this.parent.getId() != null) {
			return this.parent.getId().toString();
		} else {
			return parent_id;
		}
	}

	@JsonSetter("parent_id")
	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}
}
