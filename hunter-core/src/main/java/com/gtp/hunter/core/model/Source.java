package com.gtp.hunter.core.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.websocket.CloseReason;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.websocket.ControlSession;

@Entity
@Table(name = "source")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Source extends UUIDAuditModel {

	@OneToMany(mappedBy = "source", targetEntity = Device.class, fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@Expose(serialize = false)
	@Fetch(FetchMode.SUBSELECT)
	@JsonIgnore
	private Set<Device>			devices;

	@Expose
	private boolean				uselocal;

	@Transient
	@JsonIgnore
	private ControlSession		controlSession;

	@Expose
	@Transient
	private transient String	token;

	public Source() {
	}

	public Source(String name, String metaname, boolean useLocal) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setUselocal(useLocal);
	}

	public boolean isOnline() {
		return (controlSession != null && controlSession.isOnline());
	}

	public ControlSession getControlSession() {
		return controlSession;
	}

	public void setControlSession(ControlSession ss) {
		this.controlSession = ss;
	}

	public boolean isUselocal() {
		return uselocal;
	}

	public void setUselocal(boolean uselocal) {
		this.uselocal = uselocal;
	}

	public Set<Device> getDevices() {
		return devices;
	}

	public void setDevices(Set<Device> devices) {
		this.devices = devices;
	}

	public void closeControlSession(CloseReason cr) {
		if (this.controlSession != null) {
			this.controlSession.close(cr);
		}
		this.controlSession = null;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String tkn) {
		this.token = tkn;
	}

	@Override
	public boolean equals(Object o) {
		return this.getId().equals(((Source) o).getId());
	}

	@Override
	public String toString() {
		return this.getId().toString();
	}
}
