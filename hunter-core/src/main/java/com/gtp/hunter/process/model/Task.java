package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

/**
 * @author Fernando
 *
 */
@Entity
@Table(name = "task")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "taskdef_id")
	@Expose
	private TaskDef		taskdef;

	@ManyToOne
	@JoinColumn(name = "doc_id")
	@Expose
	private Document	document;

	@Type(type = "uuid-char")
	@Column(name = "user_id", columnDefinition = "CHAR(36)")
	private UUID		user;

	@Transient
	@Expose
	private boolean		remove	= false;

	@Transient
	@Expose
	private Set<Action>	actions	= new HashSet<Action>();

	public Task() {
	}

	public Task(TaskDef def, Document doc) {
		this.setTaskdef(def);
		this.setDocument(doc);
		this.setStatus("NOVO");
	}

	public TaskDef getTaskdef() {
		return taskdef;
	}

	public void setTaskdef(TaskDef taskdef) {
		this.taskdef = taskdef;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public UUID getUser() {
		return user;
	}

	public void setUser(UUID user) {
		this.user = user;
	}

	public boolean isRemove() {
		return remove;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public Set<Action> getActions() {
		return actions;
	}

	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}
}
