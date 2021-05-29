package com.gtp.hunter.process.wf.process.activity;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.devices.UbeeSensorDevice;
import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.origin.PortOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class UbeeResetProcessActivity extends BaseProcessActivity {
	private transient static final Logger			logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final ScheduledExecutorService	exec	= Executors.newScheduledThreadPool(3);
	Map<String, Object>								params;

	public UbeeResetProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		this.params = JsonUtil.jsonToMap(model.getParam());
	}

	@Override
	public ProcessActivityExecuteReturn executePostConstruct() {
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn executePreTransform(Object arg) {
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseModel arg) {
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseProcess p) {
		List<Integer> resetIds = (List<Integer>) p.getParametros().getOrDefault("reset", new ArrayList<>());
		List<Integer> requestIds = (List<Integer>) p.getParametros().getOrDefault("request", new ArrayList<>());
		List<Integer> readIds = (List<Integer>) p.getParametros().getOrDefault("read", new ArrayList<>());
		List<Future<Command>> futureList = new ArrayList<>();
		PortOrigin orig = ((PortOrigin) getOrigin());
		UbeeSensorDevice d = (UbeeSensorDevice) orig.getDevices().get((String) params.get("device"));
		int i = 0;

		for (int id : resetIds) {
			futureList.add(exec.schedule(() -> d.reset(id), i++ * 700, TimeUnit.MILLISECONDS));
		}
		for (int id : requestIds) {
			futureList.add(exec.schedule(() -> d.request(id), i++ * 700, TimeUnit.MILLISECONDS));
		}
		for (int id : readIds) {
			futureList.add(exec.schedule(() -> d.read(id), i++ * 700, TimeUnit.MILLISECONDS));
		}
		p.getParametros().remove("reset");
		p.getParametros().remove("request");
		p.getParametros().remove("read");
		return ProcessActivityExecuteReturn.OK;
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
