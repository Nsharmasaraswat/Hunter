package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.repository.AlertRepository;

@Stateless
public class AlertService {

	@Inject
	private AlertRepository alertRep;

	@Asynchronous
	@Transactional(value = TxType.REQUIRES_NEW)
	public Future<Alert> persist(Alert alert) {
		return Executors.newCachedThreadPool().submit(() -> alertRep.persist(alert));
	}

	public List<Alert> listAll() {
		return alertRep.listAll();
	}

	public void removeById(UUID id) {
		alertRep.removeById(id);
	}
}
