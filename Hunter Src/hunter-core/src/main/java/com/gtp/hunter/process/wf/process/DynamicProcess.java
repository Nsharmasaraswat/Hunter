package com.gtp.hunter.process.wf.process;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.common.util.RestUtil;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.jsonstubs.AGLRawData;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.interfaces.ExternalProcessor;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

//TODO: add activities from other modules
public class DynamicProcess extends BaseProcess {

	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private RestUtil						rest;
	private RegisterService					regSvc;
	private RegisterStreamManager			rsm;
	private PublishSubject					ps;
	private Process							model;
	private BaseOrigin						origin;
	private Disposable						disp;
	private Map<String, Object>				params;
	private boolean							complete;
	private List<ExternalProcessor>			extProcessors	= new CopyOnWriteArrayList<>();

	public void onBaseInit(Process model, RegisterService tRep, BaseOrigin origin, RegisterStreamManager rsm) throws Exception {
		this.model = model;
		this.regSvc = tRep;
		this.rsm = rsm;
		this.ps = PublishSubject.create();
		this.origin = origin;
		// this.origin.getOrigin().subscribe(this);
		this.params = JsonUtil.jsonToMap(model.getParam());
		baseCheckParams();
		this.rest = new RestUtil((String) this.params.get("base-url"));
	}

	private void baseCheckParams() throws Exception {
		if (!this.params.containsKey("base-url"))
			throw new Exception("Parâmetro 'base-url' não encontrado.");
	}

	@Override
	public void initFilters() {
	}

	@Override
	public void onSubscribe(Disposable arg0) {
		this.disp = arg0;
		ps.onSubscribe(arg0);
	}

	@Override
	public void onNext(ComplexData cd) {
		logger.info("RawData Received: " + cd.toString());
		ps.onNext(cd);
	}

	@Override
	public void message(BaseProcessMessage msg) {
		logger.info("Message Received: " + msg.toString());
		extProcessors.stream()
						.filter(fw -> fw.getType().equals(msg.getCommand()))
						.forEach(fm -> {
							ps.onNext(fm.process(this, msg.getData()));
						});
	}

	@Override
	public void onError(Throwable arg0) {
		logger.error(this.logPrefix() + arg0.getLocalizedMessage());
		Stream.of(arg0.getStackTrace()).forEach(s -> logger.error(s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")"));
		ps.onError(arg0);
	}

	@Override
	public void onComplete() {
		ps.onComplete();
		this.disp.dispose();
	}

	@Override
	public void subscribe(Observer arg0) {
		ps.subscribe(arg0);
		connect();
	}

	@Override
	public Process getModel() {
		return this.model;
	}

	@Override
	public boolean isComplete() {
		return this.complete;
	}

	@Override
	public RegisterService getRegSvc() {
		return regSvc;
	}

	@Override
	public RegisterStreamManager getRsm() {
		return rsm;
	}

	@Override
	public Map<String, Object> getParametros() {
		return params;
	}

	@Override
	public String finish() {
		final Profiler p = new Profiler();
		if (!isFailure()) {
			success();
		} else {
			failure();
		}

		this.complete = true;
		p.step(logPrefix() + " RETORNO DO FIM DO PROCESSO: " + this.getModel().getUrlRetorno(), true);

		this.disp.dispose();

		rsm.getPsm().deactivateProcess(this.getModel());
		p.done(logPrefix() + " complete!", true, false);
		return this.getModel().getUrlRetorno();
	}

	private void connect() {
		logger.info("Client Connected!");
	}

	public void addExternalProcessor(ExternalProcessor fpe) {
		this.extProcessors.add(fpe);
	}

	public void removeExternalProcessor(ExternalProcessor fpe) {
		this.extProcessors.remove(fpe);
	}

	@Override
	public Observable getFilterByDocument(UUID document) {
		return ps.filter(new Predicate<Object>() {
			@Override
			public boolean test(Object o) {
				if (o instanceof Thing) {
					Thing v = (Thing) o;

					if (v == null || v.getDocument() == null || document == null)
						return false;
					return (v.getDocument().equals(document));
				} else if (o instanceof BaseProcessMessage) {
					return true;
				}
				return false;
			}
		}).observeOn(Schedulers.computation()).doOnSubscribe((a) -> connect());
	}

	@Override
	public Observable getFilterByTagId(String tagId) {
		return ps.filter(new Predicate<Object>() {
			@Override
			public boolean test(Object cd) {
				if (cd instanceof ComplexData)
					return ((ComplexData) cd).getTagId().equals(tagId);
				if (cd instanceof AGLRawData<?>) {
					return ((AGLRawData) cd).getTagId().equals(tagId);
				} else
					return true;
			}
		}).observeOn(Schedulers.computation());
	}

	@Override
	public void onInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void success() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void failure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}
}
