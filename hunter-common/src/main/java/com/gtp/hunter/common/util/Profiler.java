package com.gtp.hunter.common.util;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Profiler {
	private transient static final Logger		logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private String								name;
	private long								t0;
	private int									stepcount;
	private ConcurrentSkipListMap<Long, String>	lap;

	public Profiler() {
		this(null, false);
	}

	public Profiler(String name) {
		this(name, false);
	}

	public Profiler(String name, boolean log) {
		this.lap = new ConcurrentSkipListMap<>();
		this.t0 = System.nanoTime();
		this.stepcount = 0;
		if (name == null) {
			StackTraceElement ste = Thread.currentThread().getStackTrace().length > 3 ? Thread.currentThread().getStackTrace()[3] : Thread.currentThread().getStackTrace()[2];
			String callerClass = ste.getClassName().substring(ste.getClassName().lastIndexOf(".") + 1);
			String callerMethod = ste.getMethodName();

			name = String.format("%s.%s", callerClass, callerMethod);
		}
		this.name = name;
		lap.put(t0, name);
		if (log)
			logger.info(name);
	}

	public String step(String msg, boolean log) {
		String message = msg == null ? "null" : msg;
		Long timeNow = System.nanoTime();
		Long lastKey = lap.lastKey();
		String resp = String.format("[%s{%d}(%s): %.3fs/%.3fs]", name, ++stepcount, message, (timeNow - lastKey) / 1e9, (timeNow - t0) / 1e9);

		if (lap != null)
			lap.put(timeNow, message);
		if (log)
			logger.info(resp);
		// logger.debug(resp);
		return resp;
	}

	public List<String> done(String msg, boolean log, boolean resume) {
		List<String> res = new ArrayList<>();

		step(msg, log);
		res.addAll(resume());
		if (resume)
			res.forEach(logger::info);
		return res;
	}

	private List<String> resume() {
		List<String> ret = new ArrayList<>();
		long last = lap.firstKey();
		long total = 0;

		for (Entry<Long, String> e : lap.entrySet()) {
			Long ts = e.getKey();
			String m = e.getValue();
			Long elapsed = ts - last;

			ret.add(String.format("[%s(%s): %.3fs]", name, m, elapsed / 1E9));
			last = ts;
			total += elapsed;
		}
		ret.add(String.format("[%s(Total): %.3fs]", name, total / 1E9));
		return ret;
	}

	public long getT0() {
		return t0;
	}

	public int getStepcount() {
		return stepcount;
	}

	public ConcurrentSkipListMap<Long, String> getLap() {
		return lap;
	}

}
