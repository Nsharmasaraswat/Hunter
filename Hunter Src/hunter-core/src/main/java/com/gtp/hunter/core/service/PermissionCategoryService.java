package com.gtp.hunter.core.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.PermissionCategory;
import com.gtp.hunter.core.repository.PermissionCategoryRepository;

@Stateless
public class PermissionCategoryService {

	@Inject
	private PermissionCategoryRepository pRep;

	public List<PermissionCategory> listAll() {
		return pRep.listAll();
	}

	public PermissionCategory findById(UUID id) {
		return id == null ? null : pRep.findById(id);
	}

	public PermissionCategory persist(PermissionCategory perm) {
		return pRep.persist(perm);
	}

	public void removeById(UUID id) {
		pRep.removeById(id);
	}
}
