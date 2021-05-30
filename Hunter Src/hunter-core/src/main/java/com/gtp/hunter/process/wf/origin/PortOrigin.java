package com.gtp.hunter.process.wf.origin;

import java.util.HashMap;
import java.util.Map;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.devices.BaseDevice;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.process.model.Feature;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class PortOrigin extends BaseOrigin {

	private Map<String, Source>		sources;
	private Map<String, BaseDevice>	devices	= new HashMap<String, BaseDevice>();
	private Map<String, Port>		ports	= new HashMap<String, Port>();
	private static final boolean	logNow	= false;

	public PortOrigin(RegisterService regSvc, RegisterStreamManager rsm, Origin params) {
		super(regSvc, rsm, params);
		Profiler p = new Profiler();
		this.sources = new HashMap<>();

		for (Feature f : params.getFeatures()) {
			p.step("Loop", logNow);
			Source source = this.getRegSvc().getSrcSvc().findByMetaname(f.getSource());
			p.step("Get Source", logNow);
			Device device = this.getRegSvc().getSrcSvc().findDevByMetaname(source.getId(), f.getDevice());
			p.step("Get Device", logNow);
			Port port = this.getRegSvc().getSrcSvc().getPort(source.getId(), device.getId(), f.getPort());
			p.step("Get Port", logNow);
			this.getRSM().getRdcm().byDevicePort(device.getId(), port.getPortId()).subscribe(this.origin);
			p.step("Subscribe", logNow);
			features.put(f.getMetaname(), f);
			BaseDevice bdev = this.getRegSvc().getSrcSvc().getBaseDeviceByUUID(device.getId());
			p.step("Base Device", logNow);
			sources.put(source.getMetaname(), source);
			devices.put(device.getMetaname(), bdev);
			ports.put(f.getMetaname(), port);
		}
		p.done(params.getMetaname(), logNow, false);
	}

	public Map<String, BaseDevice> getDevices() {
		return devices;
	}

	public Map<String, Port> getPorts() {
		return ports;
	}

	public Map<String, Source> getSources() {
		return sources;
	}

}
