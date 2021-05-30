package com.gtp.hunter.process.wf.process.timer;

import java.util.TimerTask;

import com.gtp.hunter.process.wf.process.ContinuousProcess;

public class BasicContinuousTimer extends TimerTask {

	private ContinuousProcess	proc;
	private boolean				running	= false;

	public BasicContinuousTimer(ContinuousProcess proc) {
		this.proc = proc;
	}

	@Override
	public void run() {
		if (!running) {
			running = true;
			this.proc.onTimeout();
		}
	}

	@Override
	public boolean cancel() {
		this.running = true;
		return super.cancel();
	}

}
