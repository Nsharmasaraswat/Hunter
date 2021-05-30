package com.gtp.hunter.ui.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Document;

public class TaskInProgressStub {
	@Expose
	@SerializedName("document")
	private Document	document;

	@Expose
	@SerializedName("user")
	private User		user;

	public TaskInProgressStub() {

	}

	public TaskInProgressStub(Document d, User us) {
		this.document = d;
		this.user = us;
	}

	/**
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * @param document the document to set
	 */
	public void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
}
