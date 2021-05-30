package com.gtp.hunter.process.wf.taskdef;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.websocket.session.TaskSession;
import com.gtp.hunter.process.wf.actiondef.BaseActionDef;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;
import com.gtp.hunter.ui.json.ViewTaskStub;

import io.reactivex.Observer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public abstract class BaseTaskDef {

	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final int				TASK_BLOCK_SIZE	= Integer.parseInt(ConfigUtil.get("hunter-process", "task_block_size", "5"));
	private static final ExecutorService	EXEC_SVC		= Executors.newCachedThreadPool();
	private static final Gson				GS				= new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	private TaskDef							def;
	private PublishSubject					obsTask;

	private RegisterService					regSvc;
	private RegisterStreamManager			rsm;
	private User							usr;

	public BaseTaskDef(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		this.def = def;
		this.regSvc = regSvc;
		this.rsm = rsm;
		obsTask = PublishSubject.create();
	}

	public void subscribe(User usr, Observer obs) {
		obsTask.subscribeOn(Schedulers.io()).subscribe(obs);
		final List<Document> docList = listQuickDocuments(usr);
		TaskSession ts = obs instanceof TaskSession ? (TaskSession) obs : null;//TODO: Check PublishSubject from subscribebyuser

		if (docList.size() > 0) {
			final int inteira = docList.size() / TASK_BLOCK_SIZE;
			final int frac = docList.size() % TASK_BLOCK_SIZE;
			final int qtd = inteira + (frac == 0 ? 0 : 1);

			EXEC_SVC.execute(() -> {
				for (int i = 0; i < qtd && (ts == null || ts.isOnline()); i++) {
					int fromIndex = TASK_BLOCK_SIZE * i;
					int toIndex = Math.min(docList.size(), TASK_BLOCK_SIZE * (i + 1));
					List<ViewTaskStub> ret = generateTasks(docList.subList(fromIndex, toIndex));

					logger.info("Sending Tasks: " + GS.toJson(ret));
					obsTask.onNext(ret);
				}
			});
		}
	}

	protected TaskDef getDef() {
		return def;
	}

	protected RegisterService getRegSvc() {
		return regSvc;
	}

	protected List<ViewTaskStub> generateTasks(List<Document> lst) {
		Profiler prof = new Profiler("TaskManager");
		Set<UUID> setId = lst.parallelStream()
						.map(d -> d.getId())
						.filter(i -> !rsm.getTsm().isTaskLocked(i))
						.collect(Collectors.toSet());
		List<ViewTaskStub> ret = new ArrayList<ViewTaskStub>();
		List<Document> dl = regSvc.getDcSvc().listById(setId);

		lst.removeIf(d -> d.getId() == null || dl.parallelStream().anyMatch(d1 -> d1.getId() == d.getId()));
		for (Document d : lst) {
			dl.add(d);
		}

		for (Document d1 : dl) {
			if (d1 != null) {
				logger.info(prof.step("Found Document " + d1.getCode() + " - (" + d1.getId().toString() + ") Items: " + d1.getItems().size() + " Things: " + d1.getThings().size() + " Transports: " + d1.getTransports().size() + " Siblings: " + d1.getSiblings().size(), false));
				ViewTaskStub t = new ViewTaskStub();

				try {
					Constructor bc = Class.forName(getDef().getDecorator()).getConstructor(String.class, RegisterService.class);
					BaseTaskDecorator bt = (BaseTaskDecorator) bc.newInstance(getDef().getDecParam(), regSvc);
					Optional<DocumentField> optPriority = d1.getFields().stream()
									.filter(df -> df.getField().getMetaname().equals("PRIORITY") && df.getValue() != null && !df.getValue().isEmpty())
									.findAny();

					t.setId(d1.getId());
					t.setCreatedAt(d1.getCreatedAt());
					t.setDoccode(d1.getCode());
					t.setDocname(bt.decorateName(d1));
					logger.info(prof.step("Decorate Name " + bt.getClass().getSimpleName(), false));
					t.setContents(bt.decorateContent(d1));
					logger.info(prof.step("Decorate Content " + bt.getClass().getSimpleName(), false));
					t.setPriority(optPriority.isPresent() ? Short.parseShort(optPriority.get().getValue()) : Short.MAX_VALUE);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				logger.info(prof.step("Create TaskStub", false));
				for (Action act : getDef().getActions()) {
					try {
						Constructor<? extends BaseActionDef> c = Class.forName(act.getActionDef()).asSubclass(BaseActionDef.class).getConstructor(Action.class, Purpose.class, RegisterService.class);

						for (Purpose p : getDef().getPurposes()) {
							Action a = new Action(act);

							if (act.getRoute() != null)
								a.setRoute(act.getRoute().replaceAll("%%docid%%", d1.getId().toString()));
							if (act.getParams() != null)
								a.setParams(act.getParams().replaceAll("%%docid%%", d1.getId().toString()));
							if (act.getDefparams() != null)
								a.setDefparams(act.getDefparams().replaceAll("%%docid%%", d1.getId().toString()));
							if (act.getSrvparams() != null)
								a.setSrvparams(act.getSrvparams().replaceAll("%%docid%%", d1.getId().toString()));
							a.setDocument(d1);
							BaseActionDef bd = c.newInstance(a, p, this.getRegSvc());

							t.getActions().addAll(bd.getActions());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				logger.info(prof.step("Created Actions", false));
				ret.add(t);
			}
		}
		prof.done("Tasks Found: " + ret.size(), false, false).forEach(logger::debug);
		return ret;
	}

	public int validateDoc(Document d) {
		List<ViewTaskStub> ret = validateTask(d);

		if ((ret != null) && ret.size() > 0) {
//			logger.info("TRACE-> " + def.getMetaname() + " Observer: " + obsTask.getClass().getSimpleName() + " Ret Size: " + (ret == null ? "NULL" : ret.size()));
			obsTask.onNext(ret);
		}
		return ret == null ? 0 : ret.size();
	}

	protected abstract List<Document> listQuickDocuments(User usr);

	protected abstract List<ViewTaskStub> validateTask(Document d);

	public void cancelTask(Document d) {
		if (d != null) {
			List<ViewTaskStub> lstret = validateTask(d);
			List<ViewTaskStub> ret = new ArrayList<ViewTaskStub>();

			for (ViewTaskStub t : lstret) {
				t.setCancel(true);
				t.setCancel_task(true);
				ret.add(t);
			}
			if (ret.size() > 0) {
				logger.info("Canceling Tasks: " + GS.toJson(ret));
				obsTask.onNext(ret);
			}
		}
	}
}
