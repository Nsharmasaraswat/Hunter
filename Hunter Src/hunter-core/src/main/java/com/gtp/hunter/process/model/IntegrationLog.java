package com.gtp.hunter.process.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "integrationlog")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationLog extends UUIDAuditModel {

	private String	urlrequest;
	private String	methodtype;
	@Column(columnDefinition = "LONGTEXT")
	private String	bodyparams;
	private String	address;
	private int		returncode;
	@Column(columnDefinition = "LONGTEXT")
	private String	returned;
	private long	timeconsumed;

	public IntegrationLog() {}

	public IntegrationLog(String urlreq, String method, String address, String body, int returnCode, String ret, long time) {
		this.setUrlrequest(urlreq);
		this.setMethodtype(method);
		this.setBodyparams(body);
		this.setAddress(address);
		this.setReturnCode(returnCode);
		this.setReturned(ret);
		this.setTimeconsumed(time);
	}

	public String getUrlrequest() {
		return urlrequest;
	}

	public void setUrlrequest(String urlrequest) {
		this.urlrequest = urlrequest;
	}

	public String getMethodtype() {
		return methodtype;
	}

	public void setMethodtype(String methodtype) {
		this.methodtype = methodtype;
	}

	public String getBodyparams() {
		return bodyparams;
	}

	public void setBodyparams(String bodyparams) {
		this.bodyparams = bodyparams;
	}

	public String getReturned() {
		return returned;
	}

	public void setReturned(String returned) {
		this.returned = returned;
	}

	public long getTimeconsumed() {
		return timeconsumed;
	}

	public void setTimeconsumed(long timeconsumed) {
		this.timeconsumed = timeconsumed;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getReturnCode() {
		return returncode;
	}

	public void setReturnCode(int returnCode) {
		this.returncode = returnCode;
	}
}
