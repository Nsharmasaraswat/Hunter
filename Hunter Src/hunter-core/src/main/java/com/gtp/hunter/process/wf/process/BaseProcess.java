package com.gtp.hunter.process.wf.process;

import java.util.Map;
import java.util.UUID;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;

public abstract class BaseProcess implements Observer<ComplexData>, ObservableSource<Object> {

	private boolean	sucesso	= true;
	private String	failReason;

	public abstract Process getModel();

	public abstract void onBaseInit(Process model, RegisterService regSvc, BaseOrigin origin, RegisterStreamManager rsm) throws Exception;

	public abstract void onInit();

	protected abstract void success();

	protected abstract void failure();

	public abstract String finish();

	public abstract void cancel();

	public abstract void message(BaseProcessMessage msg);

	public abstract Observable getFilterByDocument(UUID document);

	public abstract Observable<ComplexData> getFilterByTagId(String tagId);

	public abstract boolean isComplete();

	public final boolean isFailure() {
		return !this.sucesso;
	}

	public final void setFailure(String reason) {
		this.sucesso = false;
		this.failReason = reason;
	}

	public final String getFailReason() {
		return this.failReason;
	}

	protected final void setFailReason(String reason) {
		this.failReason = reason;
	}

	public abstract Map<String, Object> getParametros();

	public abstract RegisterService getRegSvc();

	public abstract RegisterStreamManager getRsm();

	public abstract void initFilters();

	protected String logPrefix() {
		String modelName = getModel() == null ? "NULLMODEL" : getModel().getMetaname();
		String originName = getModel() == null ? "NULLMODEL" : (getModel().getOrigin() == null ? "NULLORIGIN" : getModel().getOrigin().getMetaname());

		return "Process " + modelName + " on origin " + originName + ": ";
	}

}
