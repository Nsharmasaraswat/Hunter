package com.gtp.hunter.core.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.GroupRepository;
import com.gtp.hunter.core.repository.PermissionRepository;
import com.gtp.hunter.core.repository.UserRepository;


@WebServlet("inituserdata")
public class InitUserData extends HttpServlet {

	@Inject
	private UserRepository uRep;
	
	@Inject
	private PermissionRepository pRep;
	
	@Inject
	private GroupRepository gRep;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		User u = uRep.findByField("name", "Admin");
		
		GsonBuilder builder = new GsonBuilder();
		builder.excludeFieldsWithoutExposeAnnotation();
		builder.setPrettyPrinting();
		Gson gs = builder.create();
		
		resp.getWriter().println(gs.toJson(crawlPermissions(u)));
		
	}
	
	private List<Permission> crawlPermissions(User user) {
		List<Permission> ret = new ArrayList<Permission>();
		List<Group> lstGrp = new ArrayList<Group>();

		if(user!=null)  {
			//GET PERMISSION BY USER
			ret.addAll(user.getPermissions());

			//GET GROUPS BY USER
			lstGrp.addAll(user.getGroups());
			
			//GET GROUPS BY GROUPS (RECURSIVE)
			for(Group g : lstGrp) {
				lstGrp.addAll(gRep.getFilhos(g));
			}
			
			//GET PERMISSION BY GROUPS
			for(Group g : lstGrp) {
				ret.addAll(g.getPermissions());
			}

		}
				
		//RETURN
		return ret;
	}
	
}
