package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.core.model.User;

//@Entity
//@Table(name = "processaudit")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessAudit extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User		user;

	@ManyToOne
	@JoinColumn(name = "process_id")
	private Process		process;

	@JoinColumn(name = "device_id")
	@OneToOne(targetEntity = Device.class)
	private Device		device;

	@ManyToOne
	@JoinColumn(name = "document_id")
	private Document	document;

	@OneToMany(targetEntity = Thing.class, fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinColumn(name = "thing_id")
	@Fetch(FetchMode.SUBSELECT)
	private Set<Thing>	things	= new HashSet<Thing>();

	private boolean		result;

	public ProcessAudit() {}

	@Override
	public boolean equals(Object arg0) {
		return this.getId().equals(((ProcessAudit) arg0).getId());
	}
}
