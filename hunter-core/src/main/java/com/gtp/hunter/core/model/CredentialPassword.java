package com.gtp.hunter.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.gtp.hunter.common.util.CryptoUtil;

@Entity
@DiscriminatorValue("PWD")
public class CredentialPassword extends Credential {

	private static final String LOGINCLASS = "com.gtp.hunter.core.auth.CredentialPasswordHelper";
	private static final String PERMCLASS = "com.gtp.hunter.core.auth.InternalGroupHelper";

	private String password;

	private String salt;
	
	public CredentialPassword() {
		
	}
	
	public CredentialPassword(User usr, String login, String pwd) {
		this.setUser(usr);
		this.setLogin(login);
		this.setPassword(pwd);
	}

	@Override
	public String getLoginClass() {
		return LOGINCLASS;
	}

	@Override
	public String getPermClass() {
		return PERMCLASS;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	@Override
	protected void onInsert() {
		super.onInsert();
		if(salt == null) {
			salt = CryptoUtil.getRandomSalt();
			password = CryptoUtil.getPbkdf2(password, CryptoUtil.byteFromHex(salt));
		}
	}
	
}
