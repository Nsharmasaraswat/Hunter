package com.gtp.hunter.process.wf.process.timer;

import java.util.TimerTask;

import com.gtp.hunter.process.wf.process.ContinuousProcess;

public class BasicLockDownTimer extends TimerTask {

	private ContinuousProcess	proc;

	private boolean				running	= false;

	public BasicLockDownTimer(ContinuousProcess proc) {
		this.proc = proc;
	}

	@Override
	public void run() {
		if (!running) {
			running = true;
			this.proc.unlock();
		}

	}

}
