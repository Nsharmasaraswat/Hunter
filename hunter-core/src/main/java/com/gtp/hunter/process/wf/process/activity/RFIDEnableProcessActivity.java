package com.gtp.hunter.process.wf.process.activity;

import com.gtp.hunter.core.devices.RFIDDevice;
import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.origin.DeviceOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class RFIDEnableProcessActivity extends BaseProcessActivity {

	private String[] params;
	
	public RFIDEnableProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		//this.params = model.getParam().split(",");
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
		Port prt = ((DeviceOrigin) getOrigin()).getPorts().get(getModel().getParam());
		RFIDDevice rd = (RFIDDevice) ((DeviceOrigin) getOrigin()).getDevices().get(prt.getDevice().getMetaname());
		rd.enable();
	}

	@Override
	public BaseModel executeUnknown(Object arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
