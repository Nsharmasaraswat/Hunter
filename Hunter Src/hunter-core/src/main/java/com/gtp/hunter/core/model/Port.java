package com.gtp.hunter.core.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "port")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Port extends UUIDAuditModel {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "device_id")
	@Expose(serialize = false)
	@JsonIgnore
	private Device				device;

	@Expose
	private int					portId;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "portproperty", joinColumns = @JoinColumn(name = "port_id"))
	@MapKeyColumn(name = "property")
	@Fetch(FetchMode.SUBSELECT)
	@Column(name = "value")
	@Expose
	private Map<String, String>	properties	= new HashMap<String, String>();

	public Port() {
	}

	public Port(Device dev, int portId, String name, String metaname) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setDevice(dev);
		this.setPortId(portId);
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public boolean equals(Object o) {
		return this.getId().equals(((Port) o).getId());
	}

}
