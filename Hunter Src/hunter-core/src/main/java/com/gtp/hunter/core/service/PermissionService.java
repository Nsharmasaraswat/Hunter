package com.gtp.hunter.core.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.repository.PermissionRepository;

@Stateless
public class PermissionService {

	@Inject
	private PermissionRepository pRep;

	public Permission findById(UUID id) {
		return id == null ? null : pRep.findById(id);
	}

	public Permission persist(Permission perm) {
		return pRep.persist(perm);
	}

	public List<Permission> listByGroupMeta(String groupMeta) {
		return pRep.listByGroup(groupMeta);
	}

	public List<Permission> listByUserId(UUID usId) {
		return pRep.listByUserId(usId);
	}

	public List<Permission> listAll() {
		return pRep.listAll();
	}

	public List<Permission> listByApp(String app) {
		return pRep.listByField("app", app);
	}

	public void removeById(UUID id) {
		pRep.removeById(id);
	}

	public List<Permission> listByCategoryId(UUID catId) {
		return pRep.listByField("category.id", catId);
	}
}
