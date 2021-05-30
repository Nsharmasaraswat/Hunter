package com.gtp.hunter.process.wf.process.activity;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class ProcessLogActivity extends BaseProcessActivity<ComplexData, Thing> {

	@Inject
	private static transient Logger	logger;

	private Gson					g;

	public ProcessLogActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		logger.debug("INICIADO");
		this.g = new Gson();
	}

	@Override
	public ProcessActivityExecuteReturn executePostConstruct() {
		logger.debug("RECEBI POSTCONSTRUCT");
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseProcess p) {
		logger.debug("RECEBI PROCESS - " + p.getModel().getName());
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn executePreTransform(ComplexData arg) {
		logger.debug("RECEBI PRE TRANSFORM - " + arg.getTagId());
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn execute(Thing arg) {
		logger.debug("RECEBI EXECUTE - " + arg.getProduct().getSku());
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public Thing executeUnknown(ComplexData arg) {
		logger.debug("RECEBI UNKNOWN - " + arg.getTagId());
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
