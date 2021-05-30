package com.gtp.hunter.core.auth;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

import java.lang.invoke.MethodHandles;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.Credential;
import com.gtp.hunter.core.model.CredentialNTDS;
import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.GroupRepository;
import com.gtp.hunter.core.repository.PermissionRepository;
import com.gtp.hunter.core.repository.UserRepository;
import com.gtp.hunter.core.util.NTDSUtil;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.sun.jndi.ldap.LdapCtxFactory;

public class NTDSGroupHelper extends BaseGroupHelper {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static String					SERVER	= "";
	private static String					DC		= "";
	private static String					USER	= "";
	private static String					PWD		= "";
	private static String					PRF		= "";

	// private static DirContext SRVCONTEXT = null;

	public NTDSGroupHelper(UserRepository uRep, GroupRepository gRep, PermissionRepository pRep) {
		super(uRep, gRep, pRep);
		if (SERVER == "")
			SERVER = ConfigUtil.get("hunter-core", "NTDS-Server", "127.0.0.1:3268");
		if (DC == "")
			DC = ConfigUtil.get("hunter-core", "NTDS-Domain", "gtpautomation.com");
		if (USER == "")
			USER = ConfigUtil.get("hunter-core", "NTDS-User", "srvhunter");
		if (PWD == "")
			PWD = ConfigUtil.get("hunter-core", "NTDS-Password", "Globalsys123");
		if (PRF == "")
			PRF = ConfigUtil.get("hunter-core", "NTDS-GroupPreffix", "Hunter");

	}

	@Override
	public List<Permission> crawlPermissions(User user) {
		CredentialNTDS cd = null;
		for (Credential c : user.getCredentials()) {
			if (c instanceof CredentialNTDS) {
				cd = (CredentialNTDS) c;
				break;
			}
		}

		Hashtable<String, String> props = new Hashtable<String, String>();
		String principalName = USER + "@" + DC;
		props.put(Context.SECURITY_PRINCIPAL, principalName);
		props.put(Context.SECURITY_CREDENTIALS, PWD);

		DirContext SRVCONTEXT = null;
		try {
			SRVCONTEXT = LdapCtxFactory.getLdapCtxInstance("ldap://" + SERVER + "/", props);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (SRVCONTEXT != null) {
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SUBTREE_SCOPE);
			try {
				NamingEnumeration<SearchResult> renum = SRVCONTEXT.search(NTDSUtil.toDC(DC),
								"(& (userPrincipalName=" + cd.getLogin() + "@" + DC + ")(objectClass=user))", controls);
				if (!renum.hasMore()) {
					//					System.out.println("Cannot locate user information for " + cd.getLogin());
					//					System.exit(1);
					return null;
				}
				SearchResult result = renum.next();
				Attribute memberOf = result.getAttributes().get("memberOf");

				if (memberOf != null) {// null if this user belongs to no group at all
					for (int i = 0; i < memberOf.size(); i++) {
						Attributes atts = SRVCONTEXT.getAttributes(memberOf.get(i).toString());
						NamingEnumeration<String> ids = atts.getIDs();
						String groupName = (String) atts.get("distinguishedName").get();
						logger.info(groupName);
						if (groupName.indexOf(PRF) > -1) {
							NamingEnumeration<SearchResult> grpnum = SRVCONTEXT.search(NTDSUtil.toDC(DC),
											"(& (distinguishedName=" + groupName + ")(objectClass=*))", controls);
							SearchResult grpRes = grpnum.next();
							groupName = grpRes.getAttributes().get("CN").get().toString();
							Attribute groupSid = grpRes.getAttributes().get("objectGUID");
							Group g = gRep.findByMetaname(groupName);
							if (g == null) {
								g = new Group();
								g.setName(groupName);
								g.setMetaname(groupName);
								gRep.persist(g);
							} else {

							}
							user.getGroups().add(g);
						}
					}

					return internalCrawlPermissions(user);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
