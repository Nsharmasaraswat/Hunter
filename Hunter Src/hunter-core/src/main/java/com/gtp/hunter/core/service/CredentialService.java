package com.gtp.hunter.core.service;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.Credential;
import com.gtp.hunter.core.repository.CredentialRepository;

@Stateless
public class CredentialService {

	@Inject
	private CredentialRepository crdRep;

	public Credential persist(Credential cr) {
		return crdRep.persist(cr);
	}

	public void removeById(UUID id) {
		crdRep.removeById(id);
	}
}
