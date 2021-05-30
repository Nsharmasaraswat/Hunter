package com.gtp.hunter.core.stream;

import javax.ejb.Singleton;

import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.ComplexData;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

//@ApplicationScoped
@Singleton
public class RawDataStreamManager {

	private PublishSubject<ComplexData>	realTime;
	private PublishSubject<Command>		commands;

	public void publishToRealTime(PublishSubject<ComplexData> ps) {
		if (realTime == null)
			initRT();
		ps.subscribe(realTime);
	}

	public Observable<ComplexData> getRealTime() {
		if (realTime == null)
			initRT();
		return realTime;
	}

	public PublishSubject<Command> getCommands() {
		if (commands == null) {
			initRT();
		}

		return commands;
	}

	private void initRT() {
		if (realTime == null)
			realTime = PublishSubject.create();
		if (commands == null)
			commands = PublishSubject.create();
	}

	public PublishSubject<ComplexData> getPublisher() {
		PublishSubject<ComplexData> ret = PublishSubject.create();
		publishToRealTime(ret);
		return ret;
	}

}
