package com.gtp.hunter.process.wf.process.activity;

import com.gtp.hunter.common.devicedata.GPIOData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.devices.GPIODevice;
import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.origin.DeviceOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class GPIOCommandProcessActivity extends BaseProcessActivity {

	private String[] params;

	public GPIOCommandProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		this.params = model.getParam().split(",");
	}

	@Override
	public ProcessActivityExecuteReturn executePostConstruct() {
		this.executeCommand();
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn executePreTransform(Object arg) {
		this.executeCommand();
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseModel arg) {
		this.executeCommand();
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseProcess p) {
		this.executeCommand();
		return ProcessActivityExecuteReturn.OK;
	}

	private void executeCommand() {
		//
		Port prt = ((DeviceOrigin) getOrigin()).getPorts().get(this.params[0]);
		GPIODevice dev = (GPIODevice) ((DeviceOrigin) getOrigin()).getDevices().get(prt.getDevice().getMetaname());
		GPIOData data = new GPIOData();
		data.setPin(prt.getPortId());
		data.setState(Integer.valueOf(this.params[1]));
		Command ret = dev.setState(data);

	}

	@Override
	public BaseModel executeUnknown(Object arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute() {
		this.executeCommand();
		return ProcessActivityExecuteReturn.OK;
	}

}
