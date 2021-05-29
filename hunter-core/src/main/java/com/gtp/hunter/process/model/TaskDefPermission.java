package com.gtp.hunter.process.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name="taskdefpermission")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskDefPermission extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name="taskdef_id")
	@Expose(serialize=false)
	@JsonIgnore
	private TaskDef taskdef;
	@Type(type="uuid-char")
    @Column(nullable = false,columnDefinition = "CHAR(36)")
	@Expose
	private UUID permission;
	
	public TaskDefPermission() {   }
	
	public TaskDefPermission(TaskDef task, UUID perm) {
		this.setTaskdef(task);
		this.setPermission(perm);
	}
	public UUID getPermission() {
		return permission;
	}
	public void setPermission(UUID permission) {
		this.permission = permission;
	}

	public TaskDef getTaskdef() {
		return taskdef;
	}

	public void setTaskdef(TaskDef taskdef) {
		this.taskdef = taskdef;
	}
	
}
