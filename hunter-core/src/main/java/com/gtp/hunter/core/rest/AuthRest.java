package com.gtp.hunter.core.rest;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gtp.hunter.common.util.CryptoUtil;
import com.gtp.hunter.core.model.Credential;
import com.gtp.hunter.core.model.CredentialPassword;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.CredentialRepository;
import com.gtp.hunter.core.repository.UserRepository;
import com.gtp.hunter.core.service.AuthService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;

@RequestScoped
@Path("/auth")
public class AuthRest {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AuthService						aSvc;

	@Inject
	private UserRepository					uRep;

	@Inject
	private CredentialRepository			crRep;

	@GET
	@Path("/preauth/{login}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPreAuth(@PathParam("login") String login) {
		return Response.ok(aSvc.getPreAuth(login)).build();
	}

	@POST
	@Path("/validate/{login}")
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response validate(String derived, @PathParam("login") String login) {
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, String> credentials = gson.fromJson(derived, type);
		Map<String, String> ret = new HashMap<String, String>();
		User u;

		try {
			u = aSvc.validate(credentials, login);
			if (u == null) {
				return Response.status(Status.UNAUTHORIZED).build();
			}
			ret.put("token", u.getSalt());
			ret.put("userid", u.getId().toString());
			ret.put("name", u.getName());
		} catch (Exception e) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		return Response.ok(ret).build();
	}

	@PUT
	@Path("/changePassword/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response changePassword(@PathParam("id") String userId, JsonObject changePasswordData) {
		User us = uRep.findById(UUID.fromString(userId));
		Map<String, String> ret = new HashMap<String, String>();

		logger.debug("Change Password for " + (us == null ? "INVALID USER" : us.getName() + " " + changePasswordData.toString()));
		String newPassword = changePasswordData.getString("new");
		String confirmationPassword = changePasswordData.getString("confirmation");

		if (us != null) {
			Optional<Credential> cd = us.getCredentials().stream().filter(c -> c.getClass().getSimpleName().equals("CredentialPassword")).findAny();
			if (cd.isPresent()) {
				CredentialPassword cred = (CredentialPassword) cd.get();
				String oldPwd = changePasswordData.getString("previous");

				if (oldPwd.equals(cred.getPassword())) {
					if (newPassword.equals(confirmationPassword)) {
						cred.setPassword(newPassword);
						crRep.multiPersist(cred);
						ret.put("success", "success");
					} else {
						ret.put("success", "failure");
						ret.put("message", "New Password Mismatch");
					}
				} else {
					ret.put("success", "failure");
					ret.put("message", "Old Password Mismatch");
				}
			} else {
				ret.put("success", "failure");
				ret.put("message", "User Doesn't Have Valid Password Credential");
			}
		} else {
			ret.put("success", "failure");
			ret.put("message", "User Doesn't Exist");
		}

		return Response.ok(ret).build();
	}

	@GET
	@Path("/logout/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(@Context HttpHeaders rs) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		aSvc.removeSession(token);
		return Response.ok(IntegrationReturn.OK).build();
	}

	@GET
	@Path("/test/{salt}/{password}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response test(@PathParam("salt") String salt, @PathParam("password") String password) {
		String pwd = CryptoUtil.getPbkdf2(password, CryptoUtil.byteFromHex(salt));
		logger.info(String.format("salt: %s password: %s pbkdf2: %s", salt, password, pwd));
		return Response.ok(IntegrationReturn.OK).build();
	}
}
