package com.gtp.hunter.process.wf.origin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Feature;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;

public abstract class BaseOrigin {

	protected PublishSubject<ComplexData>	origin;
	protected PublishSubject<Command>		commands;
	protected Origin						params;
	private Process							process		= null;
	protected Map<String, Feature>			features	= new HashMap<String, Feature>();
	private Set<Flowable<ComplexData>>		dispStreams	= new HashSet<Flowable<ComplexData>>();
	protected RegisterService				regSvc;
	protected RegisterStreamManager			rsm;

	public BaseOrigin(RegisterService regSvc, RegisterStreamManager rsm, Origin params) {
		this.regSvc = regSvc;
		this.rsm = rsm;
		this.params = params;
		this.origin = PublishSubject.create();
		this.commands = PublishSubject.create();
	}

	public PublishSubject<ComplexData> getOrigin() {
		return origin;
	}

	protected RegisterStreamManager getRSM() {
		return rsm;
	}

	protected RegisterService getRegSvc() {
		return regSvc;
	}

	public PublishSubject<Command> getCommands() {
		return commands;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public Map<String, Feature> getFeatures() {
		return features;
	}

	public Set<Flowable<ComplexData>> getDispStreams() {
		return dispStreams;
	}

	public Origin getParams() {
		return params;
	}

	public void setParams(Origin params) {
		this.params = params;
	}

}