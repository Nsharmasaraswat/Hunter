package com.gtp.hunter.common.service;

import java.util.concurrent.CountDownLatch;

import com.gtp.hunter.common.model.Command;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class BlockingSingleObserver implements SingleObserver<Command> {

	private CountDownLatch	cdl;
	private Disposable		d;
	private Command			resposta;

	public BlockingSingleObserver(Command cmd) {
		this.cdl = new CountDownLatch(1);
	}

	public CountDownLatch getCdl() {
		return this.cdl;
	}

	public Command getResposta() {
		return resposta;
	}

	@Override
	public void onSubscribe(Disposable d) {
		this.d = d;
	}

	@Override
	public void onSuccess(Command value) {
		resposta = value;
		//cdl.countDown();
		d.dispose();
	}

	@Override
	public void onError(Throwable e) {
		//		logger.debug(" onError : " + e.getMessage());
	}

}
