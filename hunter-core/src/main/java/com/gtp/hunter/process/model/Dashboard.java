package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.core.model.User;

@Entity
@Table(name = "dashboard")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dashboard extends UUIDAuditModel {

	@OneToMany(mappedBy = "dashboard", targetEntity = DashboardWidget.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@NotFound(action = NotFoundAction.IGNORE)
	@Expose
	private Set<DashboardWidget>	widgets	= new HashSet<DashboardWidget>();

	@ManyToOne
	@JoinColumn(name = "user_id")
	@Expose
	private User					user;

	public Dashboard() {
	}

	public Dashboard(String name, String status) {
		this.setName(name);
		this.setStatus(status);
	}

	/**
	 * @return the widgets
	 */
	public Set<DashboardWidget> getWidgets() {
		return widgets;
	}

	/**
	 * @param widgets the widgets to set
	 */
	public void setWidgets(Set<DashboardWidget> widgets) {
		this.widgets = widgets;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
