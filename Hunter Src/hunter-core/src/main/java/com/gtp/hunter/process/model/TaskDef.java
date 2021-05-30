package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "taskdef")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskDef extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "docmodel_id")
	@Expose()
	private DocumentModel model;
	@Expose()
	private String state;
	@Expose()
	private String filterClass;
	
	@Expose()
	@OneToMany(mappedBy = "taskdef", targetEntity = Action.class, fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	private Set<Action> actions = new HashSet<Action>();

	@Expose(serialize = false)
	@JsonIgnore
	@OneToMany(mappedBy = "taskdef", targetEntity = TaskDefPermission.class, fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	private Set<TaskDefPermission> permissions = new HashSet<TaskDefPermission>();

	@ManyToMany(mappedBy = "tasks", fetch = FetchType.EAGER)
	@JsonIgnore
	@Expose(serialize = false)
	@Fetch(FetchMode.SUBSELECT)
	private Set<Purpose> purposes = new HashSet<Purpose>();
	
	@Expose
	private String decorator;
	
	@Expose
	private String decParam;

	public TaskDef() {
	}

	public TaskDef(String name, String metaname, String filterClass, DocumentModel model, String state) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setModel(model);
		this.setState(state);
		this.setFilterClass(filterClass);
	}

	public DocumentModel getModel() {
		return model;
	}

	public void setModel(DocumentModel model) {
		this.model = model;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Set<Action> getActions() {
		return actions;
	}

	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}

	public Set<TaskDefPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<TaskDefPermission> permissions) {
		this.permissions = permissions;
	}

	public Set<Purpose> getPurposes() {
		return purposes;
	}

	public void setPurposes(Set<Purpose> purposes) {
		this.purposes = purposes;
	}

	public String getFilterClass() {
		return filterClass;
	}

	public void setFilterClass(String filterClass) {
		this.filterClass = filterClass;
	}

	@Override
	public boolean equals(Object obj) {
		return this.getId().equals(((TaskDef) obj).getId());
	}

	public String getDecorator() {
		return decorator;
	}

	public void setDecorator(String decorator) {
		this.decorator = decorator;
	}

	public String getDecParam() {
		return decParam;
	}

	public void setDecParam(String decParam) {
		this.decParam = decParam;
	}

}
