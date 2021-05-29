package com.gtp.hunter.core.websocket;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class BaseSession<T> implements Observer<T> {
	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final List<Disposable>			disp	= Collections.synchronizedList(new ArrayList<>());
	protected Session						session;
	private Gson							gs;
	private CloseReason						cr;

	public BaseSession(Session ss) {
		gs = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		this.session = ss;
	}

	public void close(CloseReason cr) {
		this.cr = cr;
		this.onComplete();
	}

	@Override
	public void onComplete() {
		try {
			if (this.session != null) {
				this.session.getUserProperties().clear();
				this.session.close(cr);
				this.session = null;
			}
			this.disp.forEach(Disposable::dispose);
			this.disp.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(Throwable arg0) {
		// TODO Auto-generated method stub
		arg0.printStackTrace();
	}

	@Override
	public void onNext(Object msg) {
		//		try {
		if (isOnline()) {
			String task = gs.toJson(msg);
			//			logger.info("TRACE-> Sending Task: " + task);
			session.getAsyncRemote().sendText(task);
		}
		//				session.getBasicRemote().sendText(gs.toJson(msg));
		//		} catch (IOException e) {
		//			this.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, e.getLocalizedMessage()));
		//		}
	}

	@Override
	public void onSubscribe(Disposable arg0) {
		if (arg0 != null)
			this.disp.add(arg0);
	}

	public boolean isOnline() {
		return (session != null && session.isOpen());
	}

	public Session getBaseSession() {
		return session;
	}

	protected Gson getGson() {
		return gs;
	}
}
