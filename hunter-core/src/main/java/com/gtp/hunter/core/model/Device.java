package com.gtp.hunter.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "device")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device extends UUIDAuditModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "src_id")
	@Expose
	private Source				source;

	@OneToMany(mappedBy = "device", targetEntity = Port.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose
	private Set<Port>			ports;

	@Expose
	private String				connectionType;
	@Expose
	private String				address;
	@Expose
	private Integer				dstport;
	@Expose
	private String				srvClass;
	@Expose
	private String				vendor;
	@Expose
	private String				model;
	@Expose
	private boolean				enableOnStart	= false;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "deviceproperty", joinColumns = @JoinColumn(name = "device_id"))
	@MapKeyColumn(name = "property")
	@Fetch(FetchMode.SUBSELECT)
	@Column(name = "value")
	@Expose
	private Map<String, String>	properties		= new HashMap<String, String>();

	@Transient
	@Expose(deserialize = false)
	@JsonIgnore
	private boolean				online			= false;

	public Device() {
	}

	public Device(Source src, String name, String meta, String srvClass, String vendor, String model, String connType, String address, Integer dstport, boolean enable) {
		this.source = src;
		this.setName(name);
		this.setMetaname(meta);
		this.srvClass = srvClass;
		this.vendor = vendor;
		this.model = model;
		this.connectionType = connType;
		this.address = address;
		this.dstport = dstport;
		this.enableOnStart = enable;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isOnline() {
		return online;
	}

	public String getSrvClass() {
		return srvClass;
	}

	public void setSrvClass(String srvClass) {
		this.srvClass = srvClass;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Set<Port> getPorts() {
		return ports;
	}

	public void setPorts(Set<Port> ports) {
		this.ports = ports;
	}

	public Integer getDstport() {
		return dstport;
	}

	public void setDstport(Integer dstport) {
		this.dstport = dstport;
	}

	public boolean isEnableOnStart() {
		return enableOnStart;
	}

	public void setEnableOnStart(boolean enableOnStart) {
		this.enableOnStart = enableOnStart;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public boolean equals(Object o) {
		return this.getId().equals(((Device) o).getId());
	}

}
