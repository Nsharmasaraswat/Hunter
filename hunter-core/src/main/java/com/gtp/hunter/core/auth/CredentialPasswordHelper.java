package com.gtp.hunter.core.auth;

import java.util.HashMap;
import java.util.Map;

import com.gtp.hunter.common.util.CryptoUtil;
import com.gtp.hunter.core.model.Credential;
import com.gtp.hunter.core.model.CredentialPassword;
import com.gtp.hunter.core.model.User;

public class CredentialPasswordHelper extends BaseAuthHelper {

	private String sessionSalt;

	public CredentialPasswordHelper(User user, Credential cred) {
		super(user, cred);
		this.sessionSalt = CryptoUtil.getRandomSalt();
	}

	@Override
	public Map<String, String> getBasePreAuth() {
		Map<String, String> ret = new HashMap<String, String>();
		CredentialPassword c = (CredentialPassword) cred;

		if (c != null) {
			ret.put("login", c.getLogin());
			ret.put("salt", c.getSalt());
			ret.put("session", sessionSalt);
		}
		return ret;
	}

	@Override
	public boolean validate(Map<String, String> values) {
		boolean ret = false;

		if (values.containsKey("credential")) {
			ret = CryptoUtil.getPbkdf2(((CredentialPassword) cred).getPassword(), CryptoUtil.byteFromHex(sessionSalt)).equals(values.get("credential"));
		}
		return ret;
	}

	@Override
	public Credential notFound(String username) {
		return null;
	}

}
