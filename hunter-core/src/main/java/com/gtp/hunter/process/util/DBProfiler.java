package com.gtp.hunter.process.util;

import java.util.List;
import java.util.UUID;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.process.service.RegisterService;

public class DBProfiler extends Profiler {

	private RegisterService	regSvc;

	private UUID			tx	= UUID.randomUUID();

	public DBProfiler(RegisterService regSvc, String name, boolean log) {
		super(name, log);
		this.regSvc = regSvc;
	}

	@Override
	public List<String> done(String msg, boolean log, boolean resume) {
		return super.done(msg, log, resume);
	}
}
