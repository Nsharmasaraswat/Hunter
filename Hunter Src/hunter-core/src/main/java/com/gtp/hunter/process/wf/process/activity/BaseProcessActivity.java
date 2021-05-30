package com.gtp.hunter.process.wf.process.activity;

import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public abstract class BaseProcessActivity<T extends Object, I extends BaseModel> {
	
	private ProcessActivity model;
	
	private BaseOrigin origin;
	
	public BaseProcessActivity(ProcessActivity model, BaseOrigin origin) {
		this.model = model;
		this.origin = origin;
	}
	
	public ProcessActivity getModel() {
		return model;
	}
	
	public BaseOrigin getOrigin() {
		return origin;
	}
	
	public abstract ProcessActivityExecuteReturn executePostConstruct();
	
	public abstract ProcessActivityExecuteReturn executePreTransform(T arg);
	
	public abstract ProcessActivityExecuteReturn execute(I arg);
	
	public abstract ProcessActivityExecuteReturn execute();
	
	public abstract ProcessActivityExecuteReturn execute(BaseProcess p);
	
	public abstract I executeUnknown(T arg);

}
