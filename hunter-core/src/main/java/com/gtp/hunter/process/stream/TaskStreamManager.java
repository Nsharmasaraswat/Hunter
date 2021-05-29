package com.gtp.hunter.process.stream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.spi.EventMetadata;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.repository.TaskDefPermissionRepository;
import com.gtp.hunter.process.repository.TaskDefRepository;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.action.BaseAction;
import com.gtp.hunter.process.wf.taskdef.BaseTaskDef;
import com.gtp.hunter.ui.json.ViewTaskStub;

import io.reactivex.Observer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

@Startup
@Singleton
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class TaskStreamManager {

	@Inject
	private transient Logger								logger;
	@Inject
	private TaskDefRepository								tRep;
	@Inject
	private RegisterService									regSvc;
	@Inject
	private RegisterStreamManager							rsm;
	@Inject
	private TaskDefPermissionRepository						tdpRep;

	@Resource
	private ManagedScheduledExecutorService					mses;

	private final Map<UUID, Future<Boolean>>				scheduledUnlockTass	= new ConcurrentHashMap<>();
	private final Map<UUID, UUID>							lockedTasks			= new ConcurrentHashMap<>();
	private final Map<String, BaseTaskDef>					observers			= new ConcurrentHashMap<>();
	private final Map<UUID, PublishSubject<ViewTaskStub>>	usertasks			= new ConcurrentHashMap<>();
	private final Map<UUID, BaseAction>						useractions			= new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		Profiler prof = new Profiler("TaskManager");
		logger.info("STARTING Task Stream Manager (TSM)");
		try {
			List<TaskDef> defs = tRep.listAll();
			int cntTdefs = defs.size();
			CountDownLatch latch = new CountDownLatch(cntTdefs);

			logger.info(prof.step(cntTdefs + " Taskdefs Listed", false));
			for (TaskDef def : defs) {
				logger.info("Ativando TaskDef " + def.getMetaname() + " Actions: " + (def.getActions() == null ? 0 : def.getActions().size()));
				mses.submit(() -> {
					initialize(def);
					latch.countDown();
				});
				prof.step("Taskdef Activated", false);
			}
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Interrupted TaskStreamManager");
		} catch (Exception e) {
			logger.error("TaskDef Activation Failed: " + e.getLocalizedMessage());
		}
		prof.done("Taskdefs Activated", false, false).forEach(logger::debug);
	}

	public void initialize(TaskDef t) {

		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Constructor<? extends BaseTaskDef> c = Class.forName(t.getFilterClass(), true, cl).asSubclass(BaseTaskDef.class).getConstructor(TaskDef.class, RegisterService.class, RegisterStreamManager.class, User.class);
			BaseTaskDef btd = c.newInstance(t, regSvc, rsm, null);

			observers.put(t.getMetaname(), btd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void subscribeByTask(String token, Observer<ViewTaskStub> obs, String metaname) {
		User usr = regSvc.getAuthSvc().getUser(token);

		if (usr != null) observers.get(metaname).subscribe(usr, obs);
	}

	public void subscribeByUser(String token, Observer<ViewTaskStub> obs) {
		Profiler prof = new Profiler("TaskManager");
		User usr = regSvc.getAuthSvc().getUser(token);

		if (usr != null) {
			PublishSubject<ViewTaskStub> ps = PublishSubject.create();

			logger.info("Created PublishSubject " + ps.hashCode() + " For Observer " + obs.hashCode());
			if (usertasks.containsKey(usr.getId())) {
				usertasks.get(usr.getId()).onComplete();
				usertasks.remove(usr.getId());
			}
			ps.subscribeOn(Schedulers.io()).distinct().subscribe(obs);
			usr.getGroups().stream()
							.flatMap(g -> tdpRep.quickMetanameListByPermission(g.getId()).parallelStream())
							.distinct()
							.forEach(m -> subscribeByTask(token, ps, m));

			usertasks.put(usr.getId(), ps);
		} else
			prof.done("User is not logged, token = " + token, false, false).forEach(logger::warn);
	}

	@AccessTimeout(value = 360000)
	public String runAction(User usr, Action action) throws Exception {
		Profiler prof = new Profiler("TaskManager");
		logger.info("Rodando action " + action.getId() + " (" + action.getName() + ")");
		String ret = "";
		try {
			logger.info(action.getClasse());
			Constructor<? extends BaseAction> c = Class.forName(action.getClasse()).asSubclass(BaseAction.class).getConstructor(User.class, Action.class, RegisterStreamManager.class, RegisterService.class);
			BaseAction ba = c.newInstance(usr, action, rsm, regSvc);

			ret = ba.execute(action);
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
		prof.done("Action Ran", false, false).forEach(logger::debug);
		return ret;
	}

	private void validateDocument(Document d) {
		Profiler prof = new Profiler("TaskManager");

		if (d != null) {
			if (!isTaskLocked(d.getId())) {
				for (String task : observers.keySet()) {
					//					observers.get(task).validateDoc(d);
					int size = observers.get(task).validateDoc(d);
					if (size > 0)
						prof.step("Task " + task + " Validated (" + size + ")", false);
				}
			} else {
				String message = String.format("Task %s locked on session %s", d.getName(), lockedTasks.entrySet().stream()
								.filter(e -> e.getValue().equals(d.getId()))
								.findAny()
								.get().getKey());
				Throwable t = new Throwable(message);

				t.setStackTrace(Thread.currentThread().getStackTrace());
				logger.warn(message, t);
				prof.step(message, false);
			}
			prof.done("Document Validated", false, false).forEach(logger::info);
		} else
			prof.done("Invalid Document", false, false).forEach(logger::info);
	}

	public void unlockTasks() {
		lockedTasks.clear();
	}

	public Map<UUID, UUID> listLockedTasks() {
		return this.lockedTasks;
	}

	public void unlockTask(Document task) {
		long delay = Long.parseLong(ConfigUtil.get("hunter-process", "unlock-task-delay", "2"));

		scheduledUnlockTass.put(task.getId(), mses.schedule(() -> {
			lockedTasks.values().removeIf(v -> v.equals(task.getId()));
			validateDocument(task);
			return true;
		}, delay, TimeUnit.SECONDS));
	}

	public void lockTask(UUID userId, UUID taskId) {
		Future<Boolean> fut = scheduledUnlockTass.remove(taskId);

		if (userId != null)
			lockedTasks.put(userId, taskId);
		if (fut != null && !fut.isCancelled() && !fut.isDone())
			fut.cancel(true);
	}

	public boolean isTaskLocked(UUID id) {
		if (id != null)
			return lockedTasks.values().contains(id);
		logger.info("ID IS NULL");
		return true;
	}

	public boolean isTaskBoundToUser(UUID userId, UUID id) {
		boolean ret = lockedTasks.entrySet().parallelStream()
						.anyMatch(e -> e.getKey().equals(userId) && e.getValue().equals(id));

		if (lockedTasks.containsKey(userId))
			logger.debug("LockedTasks User: " + userId + " Value: " + lockedTasks.get(userId) + " (" + lockedTasks.size() + ")");
		if (lockedTasks.containsValue(id)) {
			lockedTasks.entrySet().parallelStream()
							.filter(e -> e.getValue().equals(id))
							.forEach(e -> {
								logger.debug("LockedTasks Value: " + id + " User: " + e.getKey() + " (" + lockedTasks.size() + ")");
							});
		}
		logger.debug("Bound To User: " + ret);
		return ret;
	}

	public void cancelTask(UUID userId, UUID docId) {
		cancelTask(userId, regSvc.getDcSvc().findById(docId));
	}

	public void cancelTask(UUID userId, Document doc) {
		Profiler prof = new Profiler("TaskManager");

		if (doc != null) {
			for (String task : observers.keySet()) {
				observers.get(task).cancelTask(doc);
			}
			if (!isTaskLocked(doc.getId()))
				lockTask(userId, doc.getId());
		}
		prof.done("Task Canceled", false, false).forEach(logger::debug);
	}

	public boolean isTaskDefActive(String metaname) {
		return observers.containsKey(metaname);
	}

	public RegisterStreamManager getRsm() {
		return rsm;
	}

	public void getDocumentEvent(@ObservesAsync Document d, EventMetadata meta) {
		validateDocument(d);
	}

	public void registerAction(User usr, BaseAction ba) {
		this.useractions.put(usr.getId(), ba);
	}

	public void unregisterAction(User usr) {
		this.useractions.remove(usr.getId());
	}

	public BaseAction getRegisteredAction(UUID usrId) {
		return this.useractions.get(usrId);
	}

	public BaseAction getRegisteredAction(User usr) {
		return this.useractions.get(usr.getId());
	}
}
