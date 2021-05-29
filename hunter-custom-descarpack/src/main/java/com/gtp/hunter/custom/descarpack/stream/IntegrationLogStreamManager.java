package com.gtp.hunter.custom.descarpack.stream;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.process.model.IntegrationLog;

import io.reactivex.subjects.PublishSubject;

@Singleton
@Startup
public class IntegrationLogStreamManager {

	@Inject
	private transient Logger				logger;

	private PublishSubject<IntegrationLog>	ilStream;

	@PostConstruct
	public void init() {
		logger.info("Inicializando IntegrationLogStream");
		this.ilStream = PublishSubject.create();
	}

	public PublishSubject<IntegrationLog> getStream() {
		return ilStream;
	}

	public void send(IntegrationLog il) {
		ilStream.onNext(il);
	}

}
