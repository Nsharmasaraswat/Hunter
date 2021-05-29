package com.gtp.hunter.core.auth;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.Credential;
import com.gtp.hunter.core.model.CredentialNTDS;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.util.NTDSUtil;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.sun.jndi.ldap.LdapCtxFactory;

public class CredentialNTDSHelper extends BaseAuthHelper {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public CredentialNTDSHelper(User user, Credential cred) {
		super(user, cred);
		logger.info("Iniciando NTDSHelper");
	}

	@Override
	protected Map<String, String> getBasePreAuth() {
		return new HashMap<String, String>();
	}

	@Override
	public boolean validate(Map<String, String> values) {
		return validate(values, false);
	}

	public boolean validate(Map<String, String> values, boolean secondary) {
		boolean ret = false;
		String SERVER = ConfigUtil.get("hunter-core", secondary ? "NTDS-Server-secondary" : "NTDS-Server", "127.0.0.1:3268");
		String DC = ConfigUtil.get("hunter-core", "NTDS-Domain", "gtpautomation.com");
		Hashtable<String, String> props = new Hashtable<String, String>();
		String principalName = cred.getLogin() + "@" + DC;

		props.put(Context.SECURITY_PRINCIPAL, principalName);
		props.put(Context.SECURITY_CREDENTIALS, values.get("credential"));
		if (values.get("credential").equals(""))
			return false;
		try {
			DirContext ctx = LdapCtxFactory.getLdapCtxInstance("ldap://" + SERVER + "/", props);
			if (!user.getProperties().containsKey("mail"))
				updateUserAttributes(ctx, DC);
			ret = true;
			ctx.close();
		} catch (CommunicationException e) {
			//ERRO DE CONEXÃO COM LDAP
			logger.error("Error connecting to LDAP " + e.getLocalizedMessage() + " check trace");
			logger.trace(e.getLocalizedMessage(), e);
			if (!secondary)
				return validate(values, true);
		} catch (Exception e) {
			//ERRO DE SENHA
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
		logger.info("Validado: " + ret);
		return ret;
	}

	@Override
	public Credential notFound(String username) {
		String SERVER = ConfigUtil.get("hunter-core", "NTDS-Server", "127.0.0.1:3268");
		String DC = ConfigUtil.get("hunter-core", "NTDS-Domain", "gtpautomation.com");
		String USER = ConfigUtil.get("hunter-core", "NTDS-User", "srv_hunter");
		String PWD = ConfigUtil.get("hunter-core", "NTDS-Password", "cbGXLzsellMA*");
		Hashtable<String, String> props = new Hashtable<String, String>();
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SUBTREE_SCOPE);
		CredentialNTDS c = new CredentialNTDS();
		User u = new User();

		try {
			props.put(Context.SECURITY_PRINCIPAL, USER + "@" + DC);
			props.put(Context.SECURITY_CREDENTIALS, PWD);
			DirContext SRVCONTEXT = LdapCtxFactory.getLdapCtxInstance("ldap://" + SERVER + "/", props);
			NamingEnumeration<SearchResult> renum = SRVCONTEXT.search(NTDSUtil.toDC(DC), "(& (userPrincipalName=" + username + "@" + DC + ")(objectClass=user))", controls);

			if (!renum.hasMore()) {
				logger.warn("Cannot locate user information for " + username);
			} else {
				Attributes attrs = ((SearchResult) renum.next()).getAttributes();
				NamingEnumeration<String> attrIds = attrs.getIDs();
				String ssid = (String) attrs.get("objectSid").get();
				String name = (String) attrs.get("displayname").get();
				byte[] bsid = ssid.getBytes();

				u.setName(name);
				u.setStatus("ATIVO");
				u.getCredentials().add(c);
				while (attrIds.hasMore()) {
					String attrId = attrIds.next();

					if (attrId.equals("mail"))
						u.getProperties().put(attrId, (String) attrs.get(attrId).get());
					logger.info(attrId + " - " + (String) attrs.get(attrId).get());
				}

				c.setUser(u);
				c.setLogin(username);
				c.setSid(NTDSUtil.decodeSID(bsid));

				SRVCONTEXT.close();
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			return null;
		}

		return c;
	}

	private void updateUserAttributes(DirContext ctx, String domain) {
		try {
			SearchControls constraints = new SearchControls();
			String[] attrIDs = { "mail" };

			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			constraints.setReturningAttributes(attrIDs);
			NamingEnumeration<SearchResult> answer = ctx.search(NTDSUtil.toDC(domain), "sAMAccountName=" + cred.getLogin(), constraints);

			if (answer.hasMore()) {
				Attributes attrs = ((SearchResult) answer.next()).getAttributes();

				if (attrs.get("mail") != null) {
					user.getProperties().put("mail", ((String) attrs.get("mail").get()));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
