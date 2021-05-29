package com.gtp.hunter.process.stream;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.stream.RawDataStreamManager;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@Startup
@Singleton
@DependsOn("RawDataStreamManager")
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class RawDataConsumerManager {

	private PublishSubject<ComplexData>	realTime;

	private PublishSubject<Command>		commands;

	@Resource(name = "java:global/hunter-core/RawDataStreamManager!com.gtp.hunter.core.stream.RawDataStreamManager")
	private RawDataStreamManager		rdsm;

	@Inject
	private Logger						logger;

	@PostConstruct
	public void init() {
		logger.info("STARTING Raw Data Consumer Manager (RDCM)");
	}

	public Observable<ComplexData> byDevice(UUID srcId, UUID devId) {
		if (realTime == null)
			initRT();
		return realTime.filter(v -> v != null && v.getSource() != null && v.getDevice() != null && v.getSource().equals(srcId) && v.getDevice().equals(devId));
	}

	public Observable<ComplexData> bySource(UUID srcId) {
		if (realTime == null)
			initRT();
		return realTime.filter(v -> v != null && v.getSource() != null && v.getSource().equals(srcId)).observeOn(Schedulers.io());
	}

	public Observable<ComplexData> byDevicePort(UUID devId, int port) {
		if (realTime == null)
			initRT();
		return realTime.filter(v -> v != null && v.getDevice() != null && v.getDevice().equals(devId) && v.getPort() == port).observeOn(Schedulers.io());
	}

	public Observable<ComplexData> byDeviceByType(UUID src, UUID dev, RawDataType type) {
		if (realTime == null)
			initRT();
		return realTime.filter(v -> v != null && v.getSource() != null && v.getDevice() != null && v.getType() != null && v.getSource().equals(src) && v.getDevice() == dev && v.getType().equals(type));
	}

	private void initRT() {
		realTime = PublishSubject.create();
		rdsm.getRealTime().subscribe(realTime);
	}

	private void initCMD() {
		commands = PublishSubject.create();
		rdsm.getCommands().subscribe(commands);
	}

}