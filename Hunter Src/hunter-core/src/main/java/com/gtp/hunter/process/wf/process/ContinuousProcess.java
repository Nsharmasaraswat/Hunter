package com.gtp.hunter.process.wf.process;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.jsonstubs.AGLRawData;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.activity.BaseProcessActivity;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityExecuteReturn;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityPhase;
import com.gtp.hunter.process.wf.process.timer.BasicContinuousTimer;
import com.gtp.hunter.process.wf.process.timer.BasicLockDownTimer;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public abstract class ContinuousProcess extends BaseProcess {

	private transient static final Logger			logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private RegisterService							regSvc;
	private RegisterStreamManager					rsm;
	private List<BaseProcessActivity>				filters			= new ArrayList<BaseProcessActivity>();
	private PublishSubject							ps;
	private Process									model;
	private Map<UUID, ProcessActivityExecuteReturn>	returns			= new HashMap<UUID, ProcessActivityExecuteReturn>();
	private Map<String, Thing>						itensLidos;
	private BaseOrigin								origin;
	private Disposable								disp;
	private boolean									complete		= false;
	private BasicContinuousTimer					timer;
	private Timer									regtimer;
	private BasicLockDownTimer						ldtimer;
	private Timer									regldtimer;
	private Map<String, Object>						params;
	private boolean									lockdown		= false;
	private boolean									successRunning	= false;
	private Profiler								prof;
	private final boolean							logImmediatelly	= false;
	private final boolean							logResume		= false;

	public void onBaseInit(Process model, RegisterService tRep, BaseOrigin origin, RegisterStreamManager rsm) throws Exception {
		this.model = model;
		this.regSvc = tRep;
		this.rsm = rsm;
		this.ps = PublishSubject.create();
		this.origin = origin;
		// this.origin.getOrigin().subscribe(this);
		this.params = JsonUtil.jsonToMap(model.getParam());
		baseCheckParams();
		checkParams();
	}

	private void baseCheckParams() throws Exception {
		if (!this.params.containsKey("timeout"))
			throw new Exception("Parâmetro 'timeout' não encontrado.");
		if (!this.params.containsKey("lockdown"))
			throw new Exception("Parâmetro 'lockdown' não encontrado.");
		if (!this.params.containsKey("runsuccess"))
			throw new Exception("Parâmetro 'runsuccess' não encontrado.");
	}

	@Override
	public void initFilters() {
		// final Profiler p = new Profiler();
		model.getActivities().stream().forEach(f -> {
			try {
				// p.step(logPrefix() + " - Adicionando Activity " + f.getClasse(), true);
				Constructor<? extends BaseProcessActivity> c = (Constructor<BaseProcessActivity>) Class.forName(f.getClasse()).asSubclass(BaseProcessActivity.class).getConstructor(ProcessActivity.class, BaseOrigin.class);
				BaseProcessActivity bpf = c.newInstance(f, this.origin);
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
		// filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.POSTCONSTRUCT)).forEach(a -> a.executePostConstruct());
		for (BaseProcessActivity a : filters) {
			if (a.getModel().getPhase().equals(ProcessActivityPhase.POSTCONSTRUCT)) {
				a.executePostConstruct();
			}
		}
		// p.done(logPrefix() + " Complete!", true, false);
	}

	@Override
	public void onSubscribe(Disposable arg0) {
		this.disp = arg0;
		ps.onSubscribe(arg0);
	}

	@Override
	public void onNext(ComplexData cd) {
		try {
			if (!lockdown) {
				// CANCELA TIMER
				if (regtimer != null || timer != null) {
					try {
						timer.cancel();
					} catch (Exception npe) {
					} finally {
						regtimer = null;
						timer = null;
					}
				}
				if (itensLidos == null) {
					this.prof = new Profiler("Master Follow", logImmediatelly);
					itensLidos = new ConcurrentHashMap<String, Thing>();
					getParametros().remove("doc");
					getParametros().remove("itens");
					getParametros().remove("address");
					getParametros().remove("thing");
					for (BaseProcessActivity a : filters) {
						if (a.getModel().getPhase().equals(ProcessActivityPhase.NEWRUN)) {
							a.execute();
							prof.step("NewRun: " + a.getModel().getMetaname(), logImmediatelly);
						}
					}
					prof.step(logPrefix() + "NewRun", logImmediatelly);
				}
				if (cd != null && cd.getTagId() != null) {
					if (itensLidos != null && !itensLidos.containsKey(cd.getTagId())) {
						prof.step(logPrefix() + "Nova Tag - " + cd.getTagId(), logImmediatelly);
						boolean processo = true;
						// RODA PROCESSBEFORE
						this.processBefore(cd);
						prof.step(logPrefix() + "ProcessBefore", logImmediatelly);
						// RODA PRETRANSFORM
						for (BaseProcessActivity a : filters) {
							if (a.getModel().getPhase().equals(ProcessActivityPhase.PRETRANSFORM)) {
								a.executePreTransform(cd);
								prof.step("PreTransform: " + a.getModel().getMetaname(), logImmediatelly);
							}
						}
						prof.step(logPrefix() + "PreTransform", logImmediatelly);
						// TRANSFORMA CD EM THING
						Thing t = getRegSvc().getThSvc().transform(cd);
						prof.step(logPrefix() + "Transform", logImmediatelly);
						// VERIFICA UNKNOWN
						if (t == null) {
							for (BaseProcessActivity a : filters) {
								if (a.getModel().getPhase().equals(ProcessActivityPhase.UNKNOWN)) {
									t = (Thing) a.executeUnknown(cd);
									prof.step("Unkown: " + a.getModel().getMetaname(), logImmediatelly);
									if (t != null)
										break;
								}
							}
						}
						prof.step(logPrefix() + "Unknown", logImmediatelly);
						// ATIVIDADES DE PREPROCESSAMENTO
						for (BaseProcessActivity a : filters) {
							if (a.getModel().getPhase().equals(ProcessActivityPhase.PREPROCESS)) {
								a.execute(t);
								prof.step("PreProcess: " + a.getModel().getMetaname(), logImmediatelly);
							}
						}
						prof.step(logPrefix() + "PreProcess", logImmediatelly);
						// VERIFICA SE PROCESSO PODE SER EXECUTADO
						for (BaseProcessActivity a : filters) {
							if (a.getModel().getPhase().equals(ProcessActivityPhase.PROCESS)) {
								ProcessActivityExecuteReturn ret = a.execute(t);
								prof.step("Process: " + a.getModel().getMetaname(), logImmediatelly);
								returns.put(a.getModel().getId(), ret);
								switch (ret) {
									case OKNOPROCESS:
									case NOKNOPROCESS:
										processo = false;
										break;
									case FAILURE:
										setFailure(a.getModel().getMetaname());
										processo = false;
										break;
									case OK:
									case NOK:
								}
							}
							if (!processo)
								break;
						}
						prof.step(logPrefix() + "Process", logImmediatelly);
						// EXECUTANDO ESPECIALIZAÇÃO SE TUDO CORRETO
						if (processo && t != null && itensLidos != null) {
							itensLidos.put(cd.getTagId(), t);
							processAfter(t);
							prof.step("ProcessAfter", logImmediatelly);
						}
					} else {
						prof.step("Item Descartado", logImmediatelly);
						logger.debug("ITEM " + cd.getTagId() + " EXISTENTE");
					}
					// REINICIA TIMER DE TIMEOUT
					if (this.params != null && this.params.get("timeout") != null) {
						timer = new BasicContinuousTimer(this);
						regtimer = new Timer();
						regtimer.schedule(timer, Integer.parseInt(this.params.get("timeout").toString()));
						prof.step(logPrefix() + "Timer Reinit", logImmediatelly);
					}
				}
			} else {
				prof.step(logPrefix() + "PROCESSO EM FALHA - " + this.getFailReason(), logImmediatelly);
			}
		} catch (Exception e) { //TODO: Remove after fix
			logger.error(logPrefix() + e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void message(BaseProcessMessage msg) {
		logger.info("Message Received: " + msg.toString());
	}

	public void lockdown(String reason) {
		setFailReason("Lockdown Called - " + reason);
		prof.step(logPrefix() + "LockDown " + reason, logImmediatelly);
		this.lockdown = true;
		if (timer != null) {
			timer.cancel();
		}
		regtimer = null;
		timer = null;
		if (ldtimer != null) {
			ldtimer.cancel();
		}
		ldtimer = null;
		if (regldtimer != null) {
			regldtimer.cancel();
		}
		prof.step(logPrefix() + "Timers Anulados", logImmediatelly);
		regldtimer = null;
		for (BaseProcessActivity a : filters) {
			if (a.getModel().getPhase().equals(ProcessActivityPhase.LOCKDOWN)) {
				a.execute();
			}
		}
		prof.step(logPrefix() + "Process LockDown", logImmediatelly);
		ldtimer = new BasicLockDownTimer(this);
		regldtimer = new Timer();
		regldtimer.schedule(ldtimer, Integer.parseInt(this.params.get("lockdown").toString()));
		itensLidos = null;
		prof.step(logPrefix() + "Fim LockDown", logImmediatelly);
	}

	public void runSucess() {
		this.lockdown = true;
		long time = System.currentTimeMillis();

		while (successRunning && (System.currentTimeMillis() - time <= new Long(this.params.get("runsuccess").toString()))) {
			try {
				//				 logger.debug("WAITING RUNSUCCESS TO FINISH");
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.successRunning = true;
		if (ldtimer != null) {
			ldtimer.cancel();
			ldtimer = null;
		}
		if (regldtimer != null) {
			regldtimer.cancel();
			regldtimer = null;
		}
		for (BaseProcessActivity a : filters) {
			if (a.getModel().getPhase().equals(ProcessActivityPhase.RUNSUCCESS)) {
				a.execute(this);
				prof.step(logPrefix() + "RUNSUCCESS: " + a.getModel().getMetaname(), logImmediatelly);
			}
		}
		// ldtimer = new BasicLockDownTimer(this);
		// regldtimer = new Timer();
		// regldtimer.schedule(ldtimer, Integer.parseInt(this.params.get("runsuccess").toString()));
		itensLidos = null;
		successRunning = false;
		unlock();
	}

	public void unlock() {
		// prof.step("Unlock", false);
		if (timer != null)
			timer.cancel();
		if (regtimer != null)
			regtimer.cancel();
		if (regldtimer != null)
			regldtimer.cancel();
		if (ldtimer != null)
			ldtimer.cancel();
		timer = null;
		regtimer = null;
		regldtimer = null;
		ldtimer = null;
		// filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.REBOOT)).forEach(a -> {
		// a.execute();
		// });
		for (BaseProcessActivity a : filters) {
			if (a.getModel().getPhase().equals(ProcessActivityPhase.REBOOT)) {
				a.execute();
			}
		}
		prof.done(logPrefix() + "Unlock", logImmediatelly, logResume);
		this.lockdown = false;
	}

	public void onTimeout() {
		prof.step(logPrefix() + "Timeout", logImmediatelly);
		this.lockdown = true;
		if (timer != null)
			timer.cancel();
		if (regtimer != null)
			regtimer.cancel();
		timer = null;
		regtimer = null;
		this.timeout(itensLidos);
		prof.step(logPrefix() + "Fim Timeout", logImmediatelly);
		itensLidos = null;
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

	public void start() {
		final Profiler p = new Profiler();

		filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.STARTUP)).forEach(a -> {
			p.step(logPrefix() + "Rodando Startup para " + a.getModel().getMetaname(), true);
			a.execute(this);
		});
	}

	@Override
	public String finish() {
		final Profiler p = new Profiler();
		if (!isFailure()) {
			success();
			filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.SUCCESS)).forEach(a -> {
				p.step(logPrefix() + "Rodando Success para " + a.getModel().getMetaname(), true);
				a.execute(this);
			});
		} else {
			failure();
			filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.FAILURE)).forEach(a -> {
				p.step(logPrefix() + " Rodando Failure para " + a.getModel().getMetaname(), true);
				a.execute(this);
			});
		}
		filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.FINISH)).forEach(a -> {
			p.step(logPrefix() + " Rodando Finish para " + a.getModel().getMetaname(), true);
			a.execute(this);
		});

		this.complete = true;
		p.step(logPrefix() + " RETORNO DO FIM DO PROCESSO: " + this.getModel().getUrlRetorno(), true);

		this.disp.dispose();

		rsm.getPsm().deactivateProcess(this.getModel());
		p.done(logPrefix() + " complete!", true, false);
		return this.getModel().getUrlRetorno();
	}

	protected void resend(Object t) {
		if (t instanceof Thing) {
			Thing rd = (Thing) t;
			if (rd.isCancelProcess()) {
				AlertType tipo = AlertType.PROCESS;
				String item = "ContinuousProcess";
				if (rd.getDocument() != null) {
					tipo = AlertType.DOCUMENT;
					item = rd.getDocCode();

				} else if (this.origin != null) {
					tipo = AlertType.ORIGIN;
					item = this.origin.getParams().getMetaname();
				}
				String erro = "";
				for (String e : rd.getErrors()) {
					erro = e;
				}
				Alert a = new Alert(tipo, AlertSeverity.ERROR, item, erro, erro);
				getRegSvc().getAlertSvc().persist(a);
			}
		}
		ps.onNext(t);
	}

	protected abstract void checkParams() throws Exception;

	protected abstract void connect();

	public abstract void timeout(Map<String, Thing> itens);

	protected abstract void processBefore(ComplexData rd);

	protected abstract void processAfter(Thing rd);

	protected abstract void processUnknown(ComplexData rd);

	protected abstract void success();

	protected abstract void failure();

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
		}).observeOn(Schedulers.computation()).doOnSubscribe(a -> connect());
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
}
