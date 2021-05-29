package com.gtp.hunter.core.service;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.websocket.CloseReason;

import org.slf4j.Logger;

import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.common.util.CryptoUtil;
import com.gtp.hunter.core.devices.BaseDevice;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.core.repository.DeviceNewRepository;
import com.gtp.hunter.core.repository.PortNewRepository;
import com.gtp.hunter.core.repository.SourceNewRepository;
import com.gtp.hunter.core.stream.RawDataStreamManager;
import com.gtp.hunter.core.util.MailUtil;
import com.gtp.hunter.core.websocket.ControlSession;
import com.gtp.hunter.core.websocket.source.SourceDisconnectNotifier;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;

@Startup
@Singleton
public class SourceService {

	private static final Map<UUID, ScheduledFuture<Boolean>>	notificationMap	= Collections.synchronizedMap(new HashMap<>());

	@Inject
	private Logger												logger;

	@Resource
	private ManagedScheduledExecutorService						mses;

	@Inject
	private SourceNewRepository									sRep;

	@Inject
	private DeviceNewRepository									dRep;

	@Inject
	private PortNewRepository									pRep;

	@Inject
	private RawDataStreamManager								rdsm;

	@Inject
	private MailUtil											mail;

	private Map<UUID, Source>									sources			= new HashMap<UUID, Source>();
	private Map<UUID, Device>									devmdls			= new HashMap<UUID, Device>();
	private Map<UUID, BaseDevice>								devices			= new HashMap<UUID, BaseDevice>();
	private Map<String, UUID>									tokens			= new HashMap<String, UUID>();
	private Map<UUID, PublishSubject<ComplexData>>				streams			= new HashMap<UUID, PublishSubject<ComplexData>>();

	private boolean												bypassAuthentication;

	@PostConstruct
	public void init() {
		sRep.getAll().forEach(s -> {
			logger.debug("ADDING SOURCE: " + s.getMetaname());
			sources.put(s.getId(), s);
			s.getDevices().forEach(d -> {
				devmdls.put(d.getId(), d);
				streams.put(d.getId(), PublishSubject.create());
				try {
					logger.info("Source: " + s.getMetaname() + " - ID: " + s.getId().toString());
					logger.info("Device: " + d.getMetaname() + " - ID: " + d.getId().toString());
					logger.info("Command Stream Null? " + Boolean.toString(rdsm.getCommands() == null));
					logger.info("Device Class: " + d.getSrvClass());
					Constructor<? extends BaseDevice> c = Class.forName(d.getSrvClass()).asSubclass(BaseDevice.class).getConstructor(Source.class, Device.class, PublishSubject.class);
					logger.info("Constructor Null? " + Boolean.toString(c == null));
					logger.info("Constructor Name" + c.getName());
					BaseDevice bd = c.newInstance(s, d, rdsm.getCommands());
					devices.put(d.getId(), bd);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		});
	}

	@PreDestroy
	public void destroy() {
		sources.keySet().forEach(srcId -> detachSource(srcId));
	}

	public Map<UUID, Source> getSources() {
		return sources;
	}

	public String attach(ControlSession ss, UUID srcId) {
		String ret = CryptoUtil.getRandomSalt();

		removeFailureNotification(srcId);
		getSources().get(srcId).setControlSession(ss);
		devices.values().parallelStream()
						.filter(d -> d.getModel().getSource().getId().equals(srcId))
						.forEach(d -> d.attachSession());
		tokens.put(ret, srcId);
		return ret;
	}

	public void detachSource(UUID srcId) {
		ControlSession ss = getSources().get(srcId).getControlSession();
		String token = tokens.entrySet().stream()
						.filter(en -> en.getValue().equals(srcId))
						.map(en -> en.getKey())
						.findAny()
						.orElse(null);

		if (token != null) {
			tokens.remove(token);
			ss.onComplete();
			getSources().get(srcId).setControlSession(null);
			removeFailureNotification(srcId);
		}
	}

	public void remove(String token, CloseReason cr) {
		if (tokens.containsKey(token)) {
			if (sources.containsKey(tokens.get(token))) {
				Source s = sources.get(tokens.get(token));

				s.closeControlSession(cr);
				tokens.remove(token);
			}
		}
	}

	public void removeByUUID(UUID uid, CloseReason cr) {
		String token = "";

		for (String t : tokens.keySet()) {
			if (tokens.get(t).equals(uid)) {
				token = t;
				break;
			}
		}

		remove(token, cr);
	}

	public Source findByMetaname(String metaname) {
		return sRep.findByMetaname(metaname);
	}

	public Source findById(UUID id) {
		return sRep.findById(id);
	}

	public Device findDevByMetaname(UUID srcId, String metaname) {
		return dRep.findByMetaname(srcId, metaname);
	}

	public List<Device> listBySource(Source src) {
		return dRep.listByField("source", src);
	}

	public void notifySource(Device dev) {
		if (dev.getSource() == null) {
			dev = dRep.findById(dev.getId());
		} else {

		}
		if (getSources().containsKey(dev.getSource().getId())) {
			if (getSources().get(dev.getSource().getId()).isOnline()) {
				Command c = new Command();

				c.setDevice(dev.getId());
				c.setSource(dev.getSource().getId());
				c.setMethod("DEVICE-CHANGE");
				getSources().get(dev.getSource().getId()).getControlSession().onNext(c);
			}
		}
	}

	public BaseDevice getBaseDeviceByUUID(UUID id) {
		return devices.get(id);
	}

	public boolean verifyToken(String token) {
		boolean ret = false;

		if (tokens.containsKey(token)) {
			if (sources.containsKey(tokens.get(token))) {
				ret = sources.get(tokens.get(token)).isOnline();
			} else {
				// NAO EXISTE O SOURCE
			}
		} else {
			// NAO EXISTE O TOKEN
		}

		return ret || bypassAuthentication;
	}

	public void fillToken(Source s) {
		s.setToken(tokens.entrySet().stream().filter(e -> e.getValue().equals(s.getId())).map(Map.Entry::getKey).findFirst().orElse(""));
	}

	public boolean verifySource(UUID srcId) {
		return (getSources().containsKey(srcId) || bypassAuthentication);
	}

	public boolean isAttached(UUID token) {
		return tokens.containsValue(token);
	}

	public Source findByToken(String token) {
		if (tokens.containsKey(token)) {
			UUID srcId = tokens.get(token);
			if (sources.containsKey(srcId)) {
				return sources.get(srcId);
			} else {
				logger.error("Source " + srcId.toString() + " is not attached with token " + token);
			}
		} else {
			logger.error("Token " + token + " not found");
		}
		return null;
	}

	public Port getPort(UUID srcId, UUID dev, String metaname) {
		return pRep.findByMetaname(srcId, dev, metaname);
	}

	public void onNext(UUID id, ComplexData dt) {
		streams.get(id).onNext(dt);
	}

	public Flowable<ComplexData> getFlowable(UUID id) {
		return streams.get(id).toFlowable(BackpressureStrategy.BUFFER);
	}

	public void addFailureNotification(String token, UUID srcId) {
		int delay = Integer.parseInt(ConfigUtil.get("hunter-core", "source-mailer-delay", "60000"));
		ScheduledFuture<Boolean> future = mses.schedule(new SourceDisconnectNotifier(mail, sources.get(srcId)), delay, TimeUnit.MILLISECONDS);

		notificationMap.put(srcId, future);
	}

	public void removeFailureNotification(UUID srcId) {
		ScheduledFuture<Boolean> future = notificationMap.remove(srcId);

		if (future != null) {
			future.cancel(true);
			if (future.isCancelled())
				logger.info("Notification Canceled");
			else
				logger.info("Notification NOT Canceled");
		}
	}
}
