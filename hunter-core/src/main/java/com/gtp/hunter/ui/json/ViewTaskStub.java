package com.gtp.hunter.ui.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.google.gson.annotations.Expose;
import com.gtp.hunter.process.model.Action;

public class ViewTaskStub {

	@Expose
	private UUID			id;

	@Expose(serialize = false)
	private Date			createdAt;

	@Expose
	private String			taskType;

	@Expose
	private String			docname;

	@Expose
	private String			doccode;

	@Expose
	private String			contents;

	@Expose
	private boolean			cancel		= false;

	@Expose
	private List<Action>	actions		= new ArrayList<Action>();

	@Expose
	private String			created_at;

	@Expose
	private String			created_at2;

	@Expose
	private boolean			cancel_task	= false;

	@Expose
	private short			priority;

	public ViewTaskStub() {

	}

	public ViewTaskStub(UUID id, Date createdAt, String taskType, String doccode, String docname, String contents, List<Action> actions) {
		super();
		setId(id);
		setCreatedAt(createdAt);
		setTaskType(taskType);
		setDoccode(doccode);
		setDocname(docname);
		setContents(contents);
		setActions(actions);
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		this.createdAt = createdAt;
		this.created_at = sdf1.format(createdAt);
		this.created_at2 = sdf2.format(createdAt);
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getDoccode() {
		return doccode;
	}

	public void setDoccode(String doccode) {
		this.doccode = doccode;
	}

	public String getDocname() {
		return docname;
	}

	public void setDocname(String docname) {
		this.docname = docname;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
		this.cancel_task = cancel;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		//this.created_at = created_at;
	}

	public Boolean getCancel_task() {
		return cancel;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public void setCancel_task(Boolean cancel_task) {
		this.cancel_task = cancel_task;
		this.cancel = cancel_task;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(short priority) {
		this.priority = priority;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !getClass().equals(o.getClass())) return false;
		ViewTaskStub other = (ViewTaskStub) o;
		if (!Objects.deepEquals(id, other.id)) return false;
		if (!Objects.deepEquals(createdAt, other.createdAt)) return false;
		if (!Objects.deepEquals(taskType, other.taskType)) return false;
		if (!Objects.deepEquals(docname, other.docname)) return false;
		if (!Objects.deepEquals(doccode, other.doccode)) return false;
		if (!Objects.deepEquals(contents, other.contents)) return false;
		if (!Objects.deepEquals(actions, other.actions)) return false;
		if (!Objects.deepEquals(cancel, other.cancel)) return false;
		if (!Objects.deepEquals(created_at, other.created_at)) return false;
		if (!Objects.deepEquals(created_at2, other.created_at2)) return false;
		if (!Objects.deepEquals(cancel_task, other.cancel_task)) return false;
		if (priority != other.priority) return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = 0;
		result = 31 * result + Objects.hashCode(id);
		result = 31 * result + Objects.hashCode(createdAt);
		result = 31 * result + Objects.hashCode(taskType);
		result = 31 * result + Objects.hashCode(docname);
		result = 31 * result + Objects.hashCode(doccode);
		result = 31 * result + Objects.hashCode(contents);
		result = 31 * result + Objects.hashCode(actions);
		result = 31 * result + Objects.hashCode(cancel);
		result = 31 * result + Objects.hashCode(created_at);
		result = 31 * result + Objects.hashCode(created_at2);
		result = 31 * result + Objects.hashCode(cancel_task);
		result = 31 * result + priority;
		return result;
	}
}
