package com.gtp.hunter.process.wf.process;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.ProcessStreamManager;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.activity.BaseProcessActivity;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityExecuteReturn;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityPhase;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public abstract class BaseSingleProcess extends BaseProcess {

	// private ThingRepository tRep;
	private RegisterService							regSvc;
	private RegisterStreamManager					rsm;
	protected List<BaseProcessActivity>				filters		= new ArrayList<BaseProcessActivity>();
	private PublishSubject							ps;
	private Process									model;
	private Map<UUID, ProcessActivityExecuteReturn>	returns		= new HashMap<UUID, ProcessActivityExecuteReturn>();
	private BaseOrigin								origin;
	private Set<Disposable>							disps		= new HashSet<Disposable>();
	private boolean									complete	= false;
	private ProcessStreamManager					psm;
	private Map<String, Object>						parametros	= new HashMap<String, Object>();

	// TODO: Parametrize parameters and initialization
	protected boolean								autoValidate;
	protected Set<Thing>							validatedThings;
	protected Timer									validationTimer;
	protected long									validationDelay;

	public void onBaseInit(Process model, RegisterService regSvc, BaseOrigin origin, RegisterStreamManager rsm) {
		this.model = model;
		this.regSvc = regSvc;
		this.rsm = rsm;
		this.ps = PublishSubject.create();
		this.origin = origin;
		this.origin.getOrigin().subscribe(this);
		this.psm = rsm.getPsm();
		this.validatedThings = new HashSet<Thing>();
		this.validationDelay = Long.parseLong(ConfigUtil.get("hunter-process", "default-auto-validation-delay", "7000"));
		// initFilters();
	}

	protected String logPrefix() {
		String modelName = model == null ? "NULLMODEL" : model.getMetaname();
		String originName = model == null ? "NULLMODEL" : (model.getOrigin() == null ? "NULLORIGIN" : model.getOrigin().getMetaname());

		return "Process " + modelName + " on origin " + originName + ": ";
	}

	@Override
	public void onNext(ComplexData arg0) {
		Profiler p = new Profiler();

		if (!isFailure()) {
			p.step("Início Process", true);
			startValidationTimer();
			// EXECUTA AS ATIVIDADES ANTES DA TRANSFORMACAO
			filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.PRETRANSFORM)).forEach(a -> {
				a.executePreTransform(arg0);
			});
			p.step(logPrefix() + "PRETRANSFORM phase executed", true);
			// EXECUTA O PROCESSO ANTES DA TRANSFORMACAO
			this.processBefore(arg0);
			p.step(logPrefix() + "ProcessBefore executed", true);
			// TRANSFORMA
			Thing i = getRegSvc().getThSvc().transform(arg0);
			p.step(logPrefix() + "Transform executed", true);

			// VERIFICA O PROSSEGUIMENTO DO PROCESSO
			if (i == null) {
				for (BaseProcessActivity a : filters) {
					if (a.getModel().getPhase().equals(ProcessActivityPhase.UNKNOWN)) {
						i = (Thing) a.executeUnknown(arg0);
						if (i != null)
							break;
					}
				}
				p.step(logPrefix() + "UNKNOWN phases executed", true);
			}

			if (i != null) {
				// TODO: Ignorar crachá do jeito certo ou usar pra alugma coisa
				if (i.getModel().getMetaname().equals("AUTH")) {
					p.done(logPrefix() + "Found Auth!", true, false);
					return;// CRACHA
				}
				// SE O ITEM ESTÁ NO ESTADO QUE DEVERIA, PROCESSA.
				// if (i.getStatus().equals(model.getEstadoDe())) {
				// EXECUTA TODAS AS ATIVIDADES PRÉ PROCESSAMENTO DO ITEM
				for (BaseProcessActivity a : filters) {
					if (a.getModel().getPhase().equals(ProcessActivityPhase.PREPROCESS)) {
						a.execute(i);
					}
				}
				p.step(logPrefix() + "PREPROCESS phase executed", true);
				// EXECUTA TODAS AS ATIVIDADES QUE VALIDAM SE O ITEM PODE SER PROCESSADO
				boolean processo = true;

				for (BaseProcessActivity a : filters) {
					if (a.getModel().getPhase().equals(ProcessActivityPhase.PROCESS)) {
						ProcessActivityExecuteReturn ret = a.execute(i);
						returns.put(a.getModel().getId(), ret);
						switch (ret) {
							case OKNOPROCESS:
							case NOKNOPROCESS:
								processo = false;
								break;
							case FAILURE:
								setFailure(a.getModel().getMetaname());
								break;
							case OK:
							case NOK:
						}
					}
					if (!processo)
						break;
				}
				p.step(logPrefix() + "PROCESS phase executed", true);
				if (processo) {
					processAfter(i);
					p.step(logPrefix() + "ProcessAfter Executed", true);
				}
			} else {
				p.step(logPrefix() + "UNKNOWN THING...", true);
				// TODO: Mano, deu ruim geral. depois a gente coloca algo aqui
			}
		} else {
			p.step(logPrefix() + "PROCESSO EM FALHA - " + this.getFailReason(), true);
		}
		p.done(logPrefix() + "Complete!", true, false);
	}

	@Override
	public void initFilters() {
		final Profiler p = new Profiler();
		model.getActivities().stream().forEach(f -> {
			try {
				p.step(logPrefix() + "Adicionando Activity " + f.getClasse(), true);
				Constructor<BaseProcessActivity> c = (Constructor<BaseProcessActivity>) Class.forName(f.getClasse()).getConstructor(ProcessActivity.class, BaseOrigin.class);
				BaseProcessActivity bpf = c.newInstance(f, this.origin);
				// bpf.executePostConstruct();
				filters.add(bpf);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		});
		// FILTROS CARREGADOS. RODANDO OS FILTROS DE POSTCONSTRUCT
		filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.POSTCONSTRUCT)).forEach(a -> a.executePostConstruct());
		p.done(logPrefix() + "Process Complete!", true, false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#onComplete()
	 */
	@Override
	public void onComplete() {
		ps.onComplete();
		for (Disposable disp : disps) {
			disp.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#onError(java.lang.Throwable)
	 */
	@Override
	public void onError(Throwable arg0) {
		ps.onError(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#onSubscribe(io.reactivex.disposables.Disposable)
	 */
	@Override
	public void onSubscribe(Disposable arg0) {
		this.disps.add(arg0);
		ps.onSubscribe(arg0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#subscribe(io.reactivex.Observer)
	 */
	@Override
	public void subscribe(Observer arg0) {
		ps.subscribe(arg0);
		connect();
	}

	/*
	 * (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#getModel()
	 */
	@Override
	public Process getModel() {
		return model;
	}

	protected void resend(Object t) {
		ps.onNext(t);
	}

	/*
	 * (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#finish()
	 */
	@Override
	public String finish() {
		final Profiler p = new Profiler();
		validate();
		if (this.validationTimer != null) {
			this.validationTimer.cancel();
			this.validationTimer = null;
		}
		filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.FINISH)).forEach(a -> {
			p.step(logPrefix() + "Rodando Finish para " + a.getModel().getMetaname(), true);
			a.execute(this);
		});

		this.complete = true;
		p.step(logPrefix() + "RETORNO DO FIM DO PROCESSO: " + this.getModel().getUrlRetorno(), true);

		// this.disp.dispose();
		// for()

		psm.deactivateProcess(this.getModel());
		p.done(logPrefix() + "Process Complete!", true, false);
		return this.getModel().getUrlRetorno();
	}

	public void validate() {
		Profiler p = new Profiler();

		if (!isFailure()) {
			success();
			filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.SUCCESS)).forEach(a -> {
				p.step(logPrefix() + "Rodando Success para " + a.getModel().getMetaname(), true);
				a.execute(this);
			});
		} else {
			failure();
			filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.FAILURE)).forEach(a -> {
				p.step(logPrefix() + "Rodando Failure para " + a.getModel().getMetaname(), true);
				a.execute(this);
			});
		}
		p.done(logPrefix() + "Validation Done!", true, false);
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
		return this.rsm;
	}

	@Override
	public Map<String, Object> getParametros() {
		return parametros;
	}

	protected void persistThing(Thing t) {
		getRegSvc().getThSvc().persist(t);
	}

	protected void persistDocThing(DocumentThing dt) {
		getRegSvc().getDtSvc().persist(dt);
	}

	protected void persistDoc(Document d) {
		getRegSvc().getDcSvc().persist(d);
	}

	@Override
	public Observable<ComplexData> getFilterByDocument(UUID document) {
		return null; // SIMPLE PROCESS N TEM ISSO
	}

	@Override
	public Observable<ComplexData> getFilterByTagId(String tagId) {
		return null; // SIMPLE PROCESS N TEM ISSO
	}

	protected abstract void connect();

	protected abstract void processBefore(ComplexData rd);

	protected abstract void processAfter(Thing rd);

	@Override
	public void cancel() {
		this.onComplete();
	}

	protected void startValidationTimer() {
		if (autoValidate) {
			if (this.validationTimer != null) {
				this.validationTimer.cancel();
				this.validationTimer = null;
			}
			this.validationTimer = new Timer();
			this.validationTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					validate();
					// FILTROS CARREGADOS. RODANDO OS FILTROS DE POSTCONSTRUCT
					filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.POSTCONSTRUCT)).forEach(a -> a.executePostConstruct());
				}

			}, validationDelay);
		}
	}

}
