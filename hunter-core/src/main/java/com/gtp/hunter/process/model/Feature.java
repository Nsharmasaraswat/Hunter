package com.gtp.hunter.process.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name="feature")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature extends UUIDAuditModel {

	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="origin_id")
	@Expose
	private Origin origin;
	@Expose
	private String source;
	@Expose
	private String device;
	@Expose
	private String port;
	@Expose
	private boolean input;
	@Expose
	private boolean output;
	
	public Feature() {   };
	
	public Feature(String name, String metaname, Origin ori, String src, String dev, String port) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setOrigin(ori);
		this.setSource(src);
		this.setDevice(dev);
		this.setPort(port);
	}
		
	public Origin getOrigin() {
		return origin;
	}
	public void setOrigin(Origin origin) {
		this.origin = origin;
	}
	public boolean isInput() {
		return input;
	}
	public void setInput(boolean input) {
		this.input = input;
	}
	public boolean isOutput() {
		return output;
	}
	public void setOutput(boolean output) {
		this.output = output;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
}
