package com.gtp.hunter.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.GroupRepository;
import com.gtp.hunter.core.repository.UserRepository;

@Stateless
public class GroupService {

	@Inject
	private GroupRepository	gRep;

	@Inject
	private UserRepository	uRep;

	public Group findById(UUID id) {
		return gRep.findById(id);
	}

	public List<Group> listAll() {
		return gRep.listAll();
	}

	public Group persist(Group group) {
		return gRep.persist(group);
	}

	public void removeById(UUID id) {
		gRep.removeById(id);
	}

	public List<Group> listByUserId(UUID userid) {
		User us = uRep.findById(userid);

		return us != null ? new ArrayList<>(us.getGroups()) : new ArrayList<>();
	}
}
