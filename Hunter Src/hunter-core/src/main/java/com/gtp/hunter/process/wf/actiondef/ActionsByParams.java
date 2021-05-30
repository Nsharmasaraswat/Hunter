package com.gtp.hunter.process.wf.actiondef;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class ActionsByParams extends BaseActionDef {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ActionsByParams(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
		MethodHandles.lookup().lookupClass();
	}

	@Override
	public List<Action> getActions() {
		Map<String, Object> params = JsonUtil.jsonToMap(this.getAct().getParams());
		for (String s : params.keySet()) {
			logger.info(s);
		}
		return null;
	}

}
