package com.gtp.hunter.core.auth;

import java.util.Map;

import com.gtp.hunter.core.model.Credential;
import com.gtp.hunter.core.model.User;

public abstract class BaseAuthHelper {

	protected User user;
	
	protected Credential cred;
	
	public BaseAuthHelper(User user, Credential cred) {
		this.user = user;
		this.cred = cred;
	}
	
	protected abstract Map<String, String> getBasePreAuth();
	
	public abstract boolean validate(Map<String,String> values);
	
	public abstract Credential notFound(String username);
	
	public Map<String,String> getPreAuth() {
		Map<String,String> ret = getBasePreAuth();
		ret.put("id", cred.getId().toString());
		ret.put("type", cred.getClass().getSimpleName());
		return ret;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Credential getCred() {
		return cred;
	}

	public void setCred(Credential cred) {
		this.cred = cred;
	}
	
	
}
