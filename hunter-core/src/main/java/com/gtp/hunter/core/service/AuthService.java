package com.gtp.hunter.core.service;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.util.CryptoUtil;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.annotation.AuditEvent;
import com.gtp.hunter.core.annotation.AuthLogin;
import com.gtp.hunter.core.annotation.qualifier.AuthLoginQualifier;
import com.gtp.hunter.core.annotation.qualifier.SuccessQualifier;
import com.gtp.hunter.core.auth.BaseAuthHelper;
import com.gtp.hunter.core.auth.BaseGroupHelper;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Credential;
import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.CredentialRepository;
import com.gtp.hunter.core.repository.GroupRepository;
import com.gtp.hunter.core.repository.PermissionRepository;
import com.gtp.hunter.core.repository.UserRepository;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.ejbcommon.util.OrderedProperties;
import com.gtp.hunter.process.service.AlertService;

@Startup
@Singleton
public class AuthService {

	@Inject
	private Logger								logger;

	@Inject
	private CredentialRepository				cRep;

	@Inject
	private UserRepository						uRep;

	@Inject
	private GroupRepository						gRep;

	@Inject
	private PermissionRepository				pRep;

	@Inject
	private AlertService						aSvc;

	@Inject
	private Event<User>							uEvent;

	@Inject
	@AuditEvent
	@AuthLogin
	private Event<User>							userLoginEvent;

	private Map<UUID, BaseAuthHelper>			sessions	= new HashMap<UUID, BaseAuthHelper>();
	private Map<String, User>					users		= new HashMap<String, User>();
	private Map<String, UUID>					tokens		= new HashMap<String, UUID>();
	private MultivaluedMap<UUID, Permission>	perms		= new MultivaluedHashMap<UUID, Permission>();

	@PostConstruct
	public void init() {
		logger.info("INIT AUTHENTICATION SERVICE");
		OrderedProperties props = ConfigUtil.getProps("apikeys");

		for (Object key : props.keySet()) {
			String login = (String) key;
			String salt = props.getProperty(login);
			Map<String, String> preAuth = getPreAuth(login);
			User us = users.get(login);

			if (salt.isEmpty()) {
				salt = CryptoUtil.getRandomSalt();
				props.put(key, salt);
				ConfigUtil.save(props, "apikeys");
			}
			us.setSalt(salt);
			tokens.put(us.getSalt(), us.getId());
			try {
				Constructor<BaseGroupHelper> ps = (Constructor<BaseGroupHelper>) Class.forName("com.gtp.hunter.core.auth.InternalGroupHelper").getConstructor(UserRepository.class, GroupRepository.class, PermissionRepository.class);
				BaseGroupHelper bgh = ps.newInstance(uRep, gRep, pRep);

				perms.put(us.getId(), bgh.crawlPermissions(sessions.get(us.getId()).getUser()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.info("Creating API-KEY " + login + " TOKEN: " + salt);
		}
	}

	public Map<String, String> getPreAuth(String login) {
		Profiler p = new Profiler();
		Credential c = cRep.findByLogin(login);

		logger.info(p.step("findByLogin", false));
		if (c == null || c.getUser() == null) {
			p.step("No Credential", false);
			String defaultCredentialHelper = ConfigUtil.get("hunter-core", "defaultCredentialHelper", "com.gtp.hunter.core.auth.CredentialPasswordHelper");
			BaseAuthHelper b = null;
			try {
				Constructor<BaseAuthHelper> cs = (Constructor<BaseAuthHelper>) Class.forName(defaultCredentialHelper).getConstructor(User.class, Credential.class);
				b = cs.newInstance(null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			c = b.notFound(login);
			p.step("Create Credential", false);
			if (c != null && c.getUser() != null) {
				uRep.persist(c.getUser());
				cRep.persist(c);
			}
			p.step("Persist User and Credential", false);
		}
		if (c == null) {
			aSvc.persist(new Alert(AlertType.AUTH, AlertSeverity.WARNING, "Authentication", "User not Found", "User Typed: " + login));

			return null;
		} else {
			users.put(login, c.getUser());
			BaseAuthHelper b = null;
			try {
				Constructor<BaseAuthHelper> cs = (Constructor<BaseAuthHelper>) Class.forName(c.getLoginClass()).getConstructor(User.class, Credential.class);
				b = cs.newInstance(c.getUser(), c);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (b != null && c.getUser() != null && c.getUser().getId() != null) {
				sessions.put(c.getUser().getId(), b);
				Map<String, String> ret = b.getPreAuth();

				p.done("End", false, false).forEach(logger::info);
				return ret;
			} else {
				p.done("No Base Auth Helper", false, false).forEach(logger::error);
				aSvc.persist(new Alert(AlertType.CONFIGURATION, AlertSeverity.ERROR, "Authentication", "No Base Auth Helper", "No Base Auth Helper defined in hunter-core"));
				return null;
			}
		}
	}

	public User validate(Map<String, String> credential, String login) throws SecurityException {
		User ret = users.get(login);
		boolean hasMail = ret.getProperties().containsKey("mail");

		if (ret != null) {
			if (sessions.containsKey(ret.getId())) {
				if (sessions.get(ret.getId()).validate(credential)) {
					String salt = CryptoUtil.getRandomSalt();

					if (!hasMail && ret.getProperties().containsKey("mail"))
						uRep.persist(ret);
					ret.setSalt(salt);
					tokens.put(salt, ret.getId());

					logger.info(sessions.get(ret.getId()).getCred().getLoginClass());
					try {
						Constructor<BaseGroupHelper> ps = (Constructor<BaseGroupHelper>) Class.forName(sessions.get(ret.getId()).getCred().getPermClass()).getConstructor(UserRepository.class, GroupRepository.class, PermissionRepository.class);
						BaseGroupHelper bgh = ps.newInstance(uRep, gRep, pRep);

						perms.put(ret.getId(), bgh.crawlPermissions(sessions.get(ret.getId()).getUser()));
						userLoginEvent.fire(sessions.get(ret.getId()).getUser());
					} catch (Exception e) {
						e.printStackTrace();
					}
					//
				} else {
					throw new SecurityException("Invalid Operation 3");
					//					return null;
				}
			} else {
				throw new SecurityException("Invalid Operation 2");
				//				return null;
			}
		} else {
			throw new SecurityException("Invalid Operation 1");
			//			return null;
		}
		uEvent.select(new SuccessQualifier()).select(new AuthLoginQualifier()).fire(ret);
		logger.info("USERNAME: " + ret.getName());
		return ret;
	}

	public boolean valid(String token) {
		return tokens.containsKey(token);
	}

	public List<Permission> getPerms(String token) {
		return perms.get(tokens.get(token));
	}

	public User getUser(String token) {
		UUID ssId = tokens.get(token);
		BaseAuthHelper ss = sessions.get(ssId);

		return ss == null ? null : ss.getUser();
	}

	public List<UUID> getUGEntitiesByUser(User user) {
		List<UUID> ret = new ArrayList<UUID>();
		List<Group> lstGrp = new ArrayList<Group>();
		// GET UUID BY USER
		ret.add(user.getId());

		// GET GROUPS BY USER
		lstGrp.addAll(user.getGroups());

		// GET GROUPS BY GROUPS (RECURSIVE)
		for (Group g : user.getGroups()) {
			lstGrp.addAll(gRep.getFilhos(g));
		}

		// GET PERMISSION BY GROUPS
		for (Group g : lstGrp) {
			ret.add(g.getId());
		}

		// RETURN
		return ret;
	}

	public void removeSession(String token) {
		users.remove(token);
		UUID base = tokens.get(token);
		sessions.remove(base);
		tokens.remove(token);
	}
}
