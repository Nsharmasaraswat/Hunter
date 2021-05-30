package com.gtp.hunter.core.devices;

import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.common.service.TwoWayCommandService;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public abstract class BaseDevice extends TwoWayCommandService {

	private Source					src;

	private PublishSubject<Command>	cmdPublisher	= PublishSubject.create();

	private Device					model;

	public BaseDevice(Source src, Device model, PublishSubject<Command> commands) {
		this.src = src;
		this.model = model;
		this.cmdPublisher = commands;
		this.setObservable(this.cmdPublisher);
	}

	protected void send(Command c) {
		if (src.getControlSession() != null)
			src.getControlSession().onNext(c);
	}

	public void attachSession() {
		this.cmdPublisher.filter(v -> v.getSource().equals(model.getSource().getId()) && v.getDevice() == model.getId()).subscribe(src.getControlSession());
	}

	public Device getModel() {
		return this.model;
	}

	protected Command getBaseCommand() {
		Command cmd = new Command();

		cmd.setDevice(model.getId());
		cmd.setSource(src.getId());
		if (model.getPorts() != null & !model.getPorts().iterator().hasNext())
			cmd.setPort(model.getPorts().iterator().next().getPortId());
		else
			cmd.setPort(-1);
		return cmd;
	}

	protected abstract Command execute(DeviceData data);
}
