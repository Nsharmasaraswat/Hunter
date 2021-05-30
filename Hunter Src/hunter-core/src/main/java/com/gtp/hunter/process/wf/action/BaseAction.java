package com.gtp.hunter.process.wf.action;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public abstract class BaseAction {

	private Action action;
	
	private RegisterStreamManager rsm;
	
	private RegisterService regSvc;
	
	private User usr;
	
	public BaseAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		this.action = action;
		this.rsm = rsm;
		this.regSvc = regSvc;
		this.usr = usr;
	}
	
	public abstract String execute(Action t) throws Exception;

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	protected RegisterStreamManager getRsm() {
		return rsm;
	}

	protected RegisterService getRegSvc() {
		return regSvc;
	}

	public User getUser() {
		return usr;
	}
}
