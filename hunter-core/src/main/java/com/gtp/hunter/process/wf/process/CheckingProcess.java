package com.gtp.hunter.process.wf.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public class CheckingProcess extends OriginProcess {

	private List<Thing>	things	= new ArrayList<Thing>();
	private Thing		container;
	private String[]	params;

	// public CheckingProcess(Process model, RegisterService tRep, BaseOrigin origin, ProcessStreamManager psm) {
	// //super(model, tRep, origin,psm);
	// }

	@Override
	public void onInit() {
		params = getModel().getParam().split(",");
		if (params.length > 2) {
			this.container = getRegSvc().getThSvc().findById(UUID.fromString(params[1]));
			List<Thing> lstThings = getRegSvc().getThSvc().listByParent(container);
			this.things = lstThings;
			this.things.stream().forEach(t -> t.getErrors().add(params[3]));
		}
		// TODO: remove after parametrizing BaseSingleProcess
		this.autoValidate = false;
	}

	@Override
	protected void processBefore(ComplexData rd) {

	}

	@Override
	protected void processAfter(Thing t) {
		if (!this.things.contains(t)) {
			this.things.add(t);
		} else {
			this.things.get(this.things.indexOf(t)).setErrors(new HashSet<String>());
		}
		if (this.container != null) {
			if (t.getParent() == null || !t.getParent().getId().equals(container.getId())) {
				t.getErrors().add(params[2]);
			}
		} else {
			if (t.getProduct().getModel().getMetaname().equals(params[0])) {
				this.container = t;
				things.stream().filter(th -> !th.getProduct().getModel().getMetaname().equals(params[0])).filter(th -> !th.getParent().equals(container)).forEach(th -> {
					th.getErrors().add(params[2]);
					// resend(th);
				});
			}
		}
		resend(this.things.get(this.things.indexOf(t)));
	}

	@Override
	protected void connect() {
		// things.stream().forEach(t -> resend(t));
	}

	@Override
	public void message(BaseProcessMessage msg) {
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void failure() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void success() {

	}

}
