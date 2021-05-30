package com.gtp.hunter.core.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.UserRepository;

@Stateless
public class UserService {

	@Inject
	private UserRepository usrRep;

	public List<User> listAll() {
		return usrRep.listAll();
	}

	public List<User> listByGroup(String groupMeta) {
		return usrRep.listByGroup(groupMeta);
	}

	public User findById(UUID id) {
		return id == null ? null : usrRep.findById(id);
	}

	public User persist(User usr) {
		return usrRep.persist(usr);
	}

	public void removeById(UUID usId) {
		usrRep.removeById(usId);
	}

	public User findByProperty(String key, String value) {
		return usrRep.findByProperty(key, value);
	}
}
