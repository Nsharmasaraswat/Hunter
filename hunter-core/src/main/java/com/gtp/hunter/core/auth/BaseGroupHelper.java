package com.gtp.hunter.core.auth;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.GroupRepository;
import com.gtp.hunter.core.repository.PermissionRepository;
import com.gtp.hunter.core.repository.UserRepository;

public abstract class BaseGroupHelper {

	protected UserRepository		uRep;
	protected GroupRepository		gRep;
	protected PermissionRepository	pRep;

	public BaseGroupHelper(UserRepository uRep, GroupRepository gRep, PermissionRepository pRep) {
		this.uRep = uRep;
		this.gRep = gRep;
		this.pRep = pRep;
	}

	public GroupRepository getgRep() {
		return gRep;
	}

	public void setgRep(GroupRepository gRep) {
		this.gRep = gRep;
	}

	public PermissionRepository getpRep() {
		return pRep;
	}

	public void setpRep(PermissionRepository pRep) {
		this.pRep = pRep;
	}

	public List<Permission> internalCrawlPermissions(User user) {
		Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		List<Permission> ret = new ArrayList<Permission>();
		List<Group> lstGrp = new ArrayList<Group>();
		// GET PERMISSION BY USER
		for (Permission p : user.getPermissions()) {
			logger.info("Adicionando permissão de usuário: " + p.getId() + " - " + p.getMetaname());
			ret.add(p);
		}

		// GET GROUPS BY USER
		lstGrp.addAll(user.getGroups());

		// GET GROUPS BY GROUPS (RECURSIVE)
		for (Group g : user.getGroups()) {
			lstGrp.addAll(this.gRep.getFilhos(g));
		}

		// GET PERMISSION BY GROUPS
		for (Group g : lstGrp) {
			for (Permission p : g.getPermissions()) {
				if (!ret.contains(p)) {
					logger.info("Adicionando permissão de grupo: " + g.getName() + " - " + p.getId() + " - " + p.getMetaname());
					ret.add(p);
				}
			}
		}

		// RETURN
		return ret;
	}

	public abstract List<Permission> crawlPermissions(User user);

}
