package com.gtp.hunter.core.service;

import javax.ejb.Singleton;
import javax.inject.Inject;

import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.repository.RawDataRepository;
import com.gtp.hunter.core.stream.RawDataStreamManager;

import io.reactivex.subjects.PublishSubject;

@Singleton
public class RawDataService {

	@Inject
	private RawDataRepository			rawRep;

	@Inject
	private RawDataStreamManager		rdsm;

	@Inject
	private UnitService					uSvc;

	private PublishSubject<ComplexData>	stream;

	private PublishSubject<Command>		commands;

	public void processRawData(ComplexData rd) {
		rawRep.persist(rd);
		if (stream == null)
			stream = rdsm.getPublisher();
		Unit u = uSvc.findByTagId(rd.getTagId());
		rd.setUnit(u);
		stream.onNext(rd);
	}

	public void processRawData(ComplexData[] rds) {
		for (ComplexData r : rds) {
			processRawData(r);
		}
	}

	public void processCommand(Command cmd) {
		if (commands == null)
			commands = rdsm.getCommands();
		commands.onNext(cmd);
	}
}
