package com.gtp.hunter.process.wf.process;

import java.util.ArrayList;
import java.util.List;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public class SimpleProcess extends OriginProcess {

	private List<Thing> things = new ArrayList<Thing>();

	// public SimpleProcess(Process model, RegisterService tRep, BaseOrigin origin, ProcessStreamManager psm) {
	// super(model, tRep, origin, psm);
	// // TODO Auto-generated constructor stub
	// }

	@Override
	protected void processAfter(Thing rd) {
		things.add(rd);
	}

	@Override
	protected void success() {
		things.stream().forEach(t -> t.setStatus(getModel().getEstadoPara()));
		getRegSvc().getThSvc().multiPersist(things);
	}

	@Override
	protected void connect() {

	}

	@Override
	public void message(BaseProcessMessage msg) {
	}

	@Override
	protected void failure() {

	}

	@Override
	protected void processBefore(ComplexData rd) {

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInit() {
		// TODO: remove after parametrizing BaseSingleProcess
		this.autoValidate = false;
	}

}
