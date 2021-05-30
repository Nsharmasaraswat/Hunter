package com.gtp.hunter.process.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "dashboardwidget", uniqueConstraints = {
		@UniqueConstraint(columnNames = {
				"dashboard_id",
				"widget_id"
		})
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardWidget extends UUIDAuditModel {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "dashboard_id")
	@Expose(serialize = false)
	@JsonIgnore
	private Dashboard	dashboard;

	@Expose
	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "widget_id")
	private Widget		widget;

	@Expose
	@Column(columnDefinition = "json")
	@SerializedName("params")
	private JsonNode	params;

	public DashboardWidget() {
	}

	public DashboardWidget(Dashboard dashboard, Widget widget, String status) {
		setDashboard(dashboard);
		setWidget(widget);
		setStatus(status);
	}

	/**
	 * @return the dashboard
	 */
	public Dashboard getDashboard() {
		return dashboard;
	}

	/**
	 * @param dashboard the dashboard to set
	 */
	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	/**
	 * @return the widget
	 */
	public Widget getWidget() {
		return widget;
	}

	/**
	 * @param widget the widget to set
	 */
	public void setWidget(Widget widget) {
		this.widget = widget;
	}

	/**
	 * @return the params
	 */
	public JsonNode getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(JsonNode params) {
		this.params = params;
	}
}
