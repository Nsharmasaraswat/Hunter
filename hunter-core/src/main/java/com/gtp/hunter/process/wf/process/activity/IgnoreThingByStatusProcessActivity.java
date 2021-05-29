package com.gtp.hunter.process.wf.process.activity;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class IgnoreThingByStatusProcessActivity extends BaseProcessActivity<ComplexData, Thing> {

	private String[]	params;
	private String		ignoreStatus;

	public IgnoreThingByStatusProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		this.params = model.getParam().split(",");
		this.ignoreStatus = this.params[0];

	}

	@Override
	public ProcessActivityExecuteReturn executePostConstruct() {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseProcess p) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn executePreTransform(ComplexData arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute(Thing arg) {
		if (arg == null || arg.getStatus() == null || arg.getStatus().toString().equals(this.ignoreStatus)) {
			return ProcessActivityExecuteReturn.OKNOPROCESS;
		} else {
			return ProcessActivityExecuteReturn.OK;
		}
	}

	@Override
	public Thing executeUnknown(ComplexData arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
