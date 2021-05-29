package com.gtp.hunter.process.wf.actiondef;

import java.util.List;

import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public abstract class BaseActionDef {
	
	private Purpose pur;
	private Action act;
	private RegisterService regSvc;
	
	public BaseActionDef(Action act, Purpose pur, RegisterService regSvc) {
		this.act = act;
		this.pur = pur;
		this.regSvc = regSvc;
	}

	protected Purpose getPur() {
		return pur;
	}

	protected void setPur(Purpose pur) {
		this.pur = pur;
	}
	
	protected Action getAct() {
		return act;
	}

	protected void setAct(Action act) {
		this.act = act;
	}

	protected RegisterService getRegSvc() {
		return regSvc;
	}
	
	public abstract List<Action> getActions();

	protected void setRegSvc(RegisterService regSvc) {
		this.regSvc = regSvc;
	}
	
}
