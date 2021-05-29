package com.gtp.hunter.core.auth;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.GroupRepository;
import com.gtp.hunter.core.repository.PermissionRepository;
import com.gtp.hunter.core.repository.UserRepository;

public class InternalGroupHelper extends BaseGroupHelper {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public InternalGroupHelper(UserRepository uRep, GroupRepository gRep, PermissionRepository pRep) {
		super(uRep, gRep, pRep);
	}

	@Override
	public List<Permission> crawlPermissions(User user) {
		return internalCrawlPermissions(user);
	}

}
