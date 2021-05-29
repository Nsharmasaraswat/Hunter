package com.gtp.hunter.process.stream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.repository.ProcessRepository;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

import io.reactivex.schedulers.Schedulers;

@Startup
@Singleton
@DependsOn("OriginStreamManager")
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ProcessStreamManager {

	@Resource
	private ManagedExecutorService	mexec;

	@Inject
	private transient Logger		logger;

	@Inject
	private RegisterStreamManager	rsm;

	@Inject
	private ProcessRepository		pRep;

	@Inject
	private RegisterService			regSvc;

	private Map<UUID, BaseProcess>	processes	= new HashMap<UUID, BaseProcess>();
	private Map<UUID, UUID>			allocation	= new HashMap<UUID, UUID>();

	//TODO: Figure Out how to wait initialization on another module
	private boolean					initialized;

	@PostConstruct
	public void init() {
		logger.info("STARTING Process Stream Manager (PSM)");
		try {
			List<Process> lst = pRep.listAll();
			CountDownLatch latch = new CountDownLatch(lst.size());

			for (Process p : lst) {
				//				mexec.submit(() -> {
				activateProcess(p);
				latch.countDown();
				//				});
			}
			//			latch.await();
		} catch (Exception e) {
			logger.error("Process Initialization Failed");
		}
		initialized = true;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public Map<UUID, BaseProcess> getProcesses() {
		return processes;
	}

	public Map<UUID, UUID> getAllocation() {
		return this.allocation;
	}

	public Process getProcessFromMetaname(String id) {
		for (UUID u : processes.keySet()) {
			if (processes.get(u).getModel().getMetaname() != null && processes.get(u).getModel().getMetaname().equals(id)) {
				return processes.get(u).getModel();
			}
		}

		return null;
	}

	public Process getProcessFromDatabase(UUID id) {
		return pRep.findById(id);
	}

	public UUID getBaseProcessFromProcessMetaname(String id) {
		UUID ret = null;

		for (UUID proc : processes.keySet()) {
			if (processes.get(proc).getModel().getMetaname().equals(id)) {
				ret = proc;
				break;
			}
		}

		return ret;
	}

	public void activateProcess(Process p) {
		p.getActivities().forEach(pa -> logger.info(pa.getMetaname() + " - " + pa.getOrdem()));
		if (p.getOrigin() != null) {
			logger.info("Starting process " + p.getName() + " - " + p.getId().toString() + " Alloted: " + allocation.containsKey(p.getOrigin().getId()) + " Cancellable: " + p.isCancelable());
			if (!allocation.containsKey(p.getOrigin().getId()) || (allocation.containsKey(p.getOrigin().getId()) && processes.get(allocation.get(p.getOrigin().getId())).getModel().isCancelable())) {

				if (allocation.containsKey(p.getOrigin().getId()) && processes.get(allocation.get(p.getOrigin().getId())).getModel().isCancelable()) {
					deactivateProcess(p);
				}
				try {
					logger.info("Running process " + p.getClasse());
					p.getActivities().stream().forEach(pa -> logger.info(pa.getMetaname()));
					UUID orId = p.getOrigin().getId();
					BaseOrigin baseOrigin = rsm.getOsm().getOrigin(orId);
					logger.info("BaseOrigin Loaded: " + Boolean.toString(baseOrigin != null));
					Constructor<BaseProcess> c = (Constructor<BaseProcess>) Class.forName(p.getClasse()).getConstructor();
					BaseProcess b = c.newInstance();
					allocation.put(orId, p.getId());
					processes.put(p.getId(), b);
					b.onBaseInit(p, regSvc, baseOrigin, rsm);
					b.onInit();
					b.initFilters();
					logger.info("Attaching Process " + p.getName() + " to Origin " + p.getOrigin().getName());
					//osm.getStreams().get(orId).getOrigin().subscribe(b);
					baseOrigin.getOrigin().observeOn(Schedulers.computation()).subscribe(b);
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				logger.info("Origin Allotted (" + p.getOrigin().getName() + " - " + processes.get(allocation.get(p.getOrigin().getId())).getModel().getName() + "). Can't Continue");
			}
		} else {
			logger.info("Origin Null. Ignoring " + p.getName());
		}
	}

	public String finishProcess(UUID id) {
		return processes.get(id).finish();
	}

	public void deactivateProcess(Process p) {
		if (ConfigUtil.get("hunter-process", "verbose-process", "FALSE").equalsIgnoreCase("TRUE")) {
			logger.info("Lista de Alocações:");
			for (UUID origId : allocation.keySet()) {
				UUID procId = allocation.get(origId);

				if (procId != null) {
					BaseProcess proc = processes.get(procId);
					if (proc != null)
						logger.info(proc.getModel().getMetaname() + " - " + proc.getModel().getOrigin().getMetaname());
					else {
						logger.info("Allocation invalid. Process does not exist");
						processes.remove(procId);
					}
				} else {
					logger.info("Origin " + origId + " Not allotted, removing allocation");
					allocation.remove(origId);
				}
			}
		}

		logger.info("Desativando processo " + p.getMetaname() + " rodando no Origin " + p.getOrigin().getMetaname());
		if (allocation.containsKey(p.getOrigin().getId())) {
			if (processes.containsKey(allocation.get(p.getOrigin().getId()))) {
				BaseProcess proc = processes.get(allocation.get(p.getOrigin().getId()));

				if (!proc.isComplete()) proc.cancel();
			} else {
				logger.warn("PSM: PROCESSES " + p.getId().toString() + " NÃO ENCONTRADO!!!!");
			}
			allocation.remove(p.getOrigin().getId());
		} else {
			logger.warn("PSM: ALLOCATION " + p.getOrigin().getMetaname() + " NÃO ENCONTRADO!!!!");
		}
	}
}
