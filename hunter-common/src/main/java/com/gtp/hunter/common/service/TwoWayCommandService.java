package com.gtp.hunter.common.service;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.model.Command;

import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public abstract class TwoWayCommandService {

	protected transient static final Logger	logger					= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int				DEFAULT_COMMAND_TIMEOUT	= 2;
	private PublishSubject<Command>			commands;

	public void setObservable(PublishSubject<Command> commands) {
		this.commands = commands;
	}

	public Command sendSyncCommand(Command cmd) {
		return sendSyncCommand(cmd, DEFAULT_COMMAND_TIMEOUT);
	}

	public Command sendSyncCommand(Command cmd, int command_timeout) {
		try {
			BlockingSingleObserver sng = new BlockingSingleObserver(cmd);
			final UUID cmdId = cmd.getId();
			commands.subscribeOn(Schedulers.io()).filter(new Predicate<Command>() {
				@Override
				public boolean test(Command v) {
					boolean completed = v.getId().equals(cmdId) && v.getReturnValue() != null;
					return completed;
				}
			}).doOnNext(t -> {
				logger.info("Command " + t.getMethod() + " emitted (" + t.getPayload().replace("\n", "\\n") + ")");
				sng.onSuccess(t);
			}).singleOrError().observeOn(Schedulers.computation()).subscribe(sng);
			commands.onNext(cmd);

			boolean finalizado = false;

			//			try {
			long rodando = System.currentTimeMillis();
			while (System.currentTimeMillis() < rodando + command_timeout * 1000) {
				if (sng.getResposta() == null) {
					Thread.yield();
				} else {
					finalizado = true;
					break;
				}
			}
			//finalizado = sng.getCdl().await(command_timeout, TimeUnit.SECONDS);
			//			} catch (InterruptedException e) {
			//				finalizado = false;
			//			}

			if (finalizado) {
				cmd = sng.getResposta();
			} else {
				logger.info("Command " + cmd.getMethod() + " failed (" + cmd.getPayload().replace("\n", "\\n") + ")");
				logger.debug("Source: " + cmd.getSource());
				logger.debug("Device: " + cmd.getDevice());
				logger.debug("Port: " + cmd.getPort());
				logger.debug("Method: " + cmd.getMethod());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cmd;
	}

	public void sendAsyncCommand(Command cmd) {

	}

}
