package com.gtp.hunter.process.wf.origin;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.devices.BaseDevice;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.process.model.Feature;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class DeviceOrigin extends BaseOrigin {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Map<String, Source>				sources;
	private Map<String, BaseDevice>			devices	= new HashMap<String, BaseDevice>();
	private Map<String, Port>				ports	= new HashMap<String, Port>();
	private static final boolean			logNow	= false;

	public DeviceOrigin(RegisterService regSvc, RegisterStreamManager rsm, Origin origin) {
		super(regSvc, rsm, origin);
		Profiler p = new Profiler();

		this.sources = new HashMap<>();

		for (Feature f : origin.getFeatures()) {
			try {
				p.step("Loop " + f.getMetaname(), logNow);
				Source source = this.getRegSvc().getSrcSvc().findByMetaname(f.getSource());
				p.step("Get Source " + f.getSource() + " Found: " + (source != null), logNow);
				Device device = this.getRegSvc().getSrcSvc().findDevByMetaname(source.getId(), f.getDevice());
				p.step("Get Device " + f.getDevice() + " Found: " + (device != null), logNow);
				Port port = this.getRegSvc().getSrcSvc().getPort(source.getId(), device.getId(), f.getPort());
				p.step("Get Port " + f.getPort() + " Found: " + (port != null), logNow);
				rsm.getRdcm().byDevice(source.getId(), device.getId()).subscribe(this.origin);
				p.step("Subscribe", logNow);
				features.put(f.getMetaname(), f);
				BaseDevice bdev = this.getRegSvc().getSrcSvc().getBaseDeviceByUUID(device.getId());
				p.step("Base Device " + bdev.getModel().getMetaname(), logNow);
				sources.put(source.getMetaname(), source);
				devices.put(device.getMetaname(), bdev);
				ports.put(f.getMetaname(), port);
			} catch (Exception ex) {
				logger.error("Exception: " + ex.getLocalizedMessage());
			}
		}
		p.done(origin.getMetaname(), logNow, false).forEach(logger::info);
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
