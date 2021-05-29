package com.gtp.hunter.ejbcommon.util;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ConfigWatcher implements Serializable, Runnable {
	private static final long				serialVersionUID	= 9145983485498271639L;

	private static volatile ConfigWatcher	watcherInstance;
	private static boolean					run;

	private Path							confPath;
	private WatchService					watcher;

	@Inject
	private Logger							logger;

	private ConfigWatcher() {
		if (watcherInstance != null)
			throw new RuntimeException("Use getInstance()");
	}

	public static ConfigWatcher getInstance() {
		if (watcherInstance == null) {
			synchronized (ConfigWatcher.class) {
				watcherInstance = new ConfigWatcher();
				watcherInstance.init();
			}
		}

		return watcherInstance;
	}

	protected ConfigWatcher readResolve() {
		return getInstance();
	}

	@PostConstruct
	public void init() {
		try {
			confPath = Paths.get(System.getProperty("jboss.server.config.dir"));
			watcher = FileSystems.getDefault().newWatchService();
			confPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
			if (!run)
				Executors.newSingleThreadExecutor().execute(this);
			else
				this.finalize();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage() + " Adding watcher to " + confPath.toFile().getAbsolutePath());
			logger.debug("Could not add watcher.", e);
		} catch (Throwable e) {
			logger.warn("ConfigWatcher is already running");
		}
	}

	@Override
	public void run() {
		logger.info("Running Configuration Watcher");
		run = true;

		while (run) {
			try {
				// wait for key to be signaled
				WatchKey key = watcher.take();
				Thread.sleep(50);
				for (final WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					// This key is registered only for ENTRY_CREATE events, but an OVERFLOW event can occur regardless
					// if events are lost or discarded.
					if (kind == StandardWatchEventKinds.OVERFLOW) {
						logger.debug("Not processing this event kind: " + kind.toString());
						continue;
					} else {
						// The filename is the context of the event.
						@SuppressWarnings("unchecked")
						WatchEvent<Path> ev = (WatchEvent<Path>) event;

						if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
							fileAdded(confPath.resolve(ev.context()));
						} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
							fileChanged(confPath.resolve(ev.context()));
						} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
							fileRemoved(confPath.resolve(ev.context()));
						}
					}
				}
				// Reset the key -- this step is critical if you want to receive further watch events.
				// If the key is no longer valid, the directory is inaccessible so exit the loop.
				boolean valid = key.reset();

				if (!valid) {
					run = false;
					continue;
				}
			} catch (ClosedWatchServiceException e) {
				run = false;
				logger.info("Configuration watcher closed");
			} catch (InterruptedException ie) {
				logger.trace(ie.getLocalizedMessage(), ie);
				logger.warn("Configuration changes are not being watched anymore!");
				run = false;
			}
		}
	}

	private void fileChanged(Path p) {
		logger.info("File " + p.toString() + " Changed");
		ConfigUtil.reloadIfExists(p.getFileName().toString());
	}

	private void fileAdded(Path p) {
		logger.info("File " + p.toString() + " Added");
	}

	private void fileRemoved(Path p) {
		logger.info("File " + p.toString() + " Removed");
	}

	@PreDestroy
	public void stop() {
		try {
			watcher.close();
			run = false;
		} catch (IOException e) {
		}
	}
}
