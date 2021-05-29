package com.gtp.hunter.process.wf.process.activity.solar;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonString;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.process.wf.process.activity.BaseProcessActivity;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityExecuteReturn;

public abstract class TruckAddressBaseActivity extends BaseProcessActivity {

	protected Map<String, Object>	params;
	protected List<UUID>			addressList;
	protected List<String>			statusList;
	protected String				statusTo;

	public TruckAddressBaseActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		this.params = JsonUtil.jsonToMap(model.getParam());
		this.addressList = ((List<JsonString>) params.get("address-list"))
						.parallelStream()
						.map(js -> UUID.fromString(js.getString()))
						.collect(Collectors.toList());
		this.statusList = ((List<JsonString>) params.get("status-list"))
						.parallelStream()
						.map(js -> js.getString())
						.collect(Collectors.toList());
		this.statusTo = (String) params.get("status-to");
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseProcess p) {
		Map<String, Object> procParams = p.getParametros();
		Profiler prof = new Profiler();

		if (procParams.containsKey("doc")) {
			Document d = (Document) procParams.get("doc");
			Thing truck = (Thing) procParams.get("thing");
			Address addr = (Address) procParams.get("address");

			prof.step(getClass().getName() + " Get Params", false);
			if (statusList.contains(d.getStatus()) && addressList.contains(addr.getId())) {
				Document doc = p.getRegSvc().getDcSvc().findById(d.getId());

				prof.step(getClass().getName() + " Load Document", false);
				p.getRsm().getTsm().cancelTask(p.getModel().getId(), doc);
				prof.step(getClass().getName() + " Lock Task", false);
				process(p, doc, truck, addr);
				prof.step(getClass().getName() + " process", false);
				p.getRsm().getTsm().unlockTask(doc);
				prof.step(getClass().getName() + " Unlock Task", false);
			} else
				prof.step(getClass().getName() + " Status: " + d.getStatus() + " in " + statusList.parallelStream().collect(Collectors.joining(", ")) + " = " + statusList.contains(d.getStatus()) + " Address: " + addr.getMetaname() + " in " + addressList.parallelStream().map(a -> a.toString()).collect(Collectors.joining(",")) + " = " + addressList.contains(addr.getId()), false);
		} else
			prof.step(getClass().getName() + " ProcParams does not contain doc", false);
		prof.done(getClass().getName() + " Finish", false, false);
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn executePostConstruct() {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn executePreTransform(Object arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseModel arg) {
		return null;
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

	protected abstract void process(BaseProcess p, Document d, Thing truck, Address addr);

}
