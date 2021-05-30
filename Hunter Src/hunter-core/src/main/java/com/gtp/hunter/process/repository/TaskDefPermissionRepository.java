package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.TaskDefPermission;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class TaskDefPermissionRepository extends JPABaseRepository<TaskDefPermission, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager				em;

	@Inject
	private Event<TaskDefPermission>	tdpEvent;

	public TaskDefPermissionRepository() {
		super(TaskDefPermission.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public List<String> quickMetanameListByPermission(UUID permid) {
		List<String> ret = new ArrayList<String>();

		try (Connection con = initConnection();
						PreparedStatement ps = con.prepareStatement(
										"select td.metaname from taskdef td join taskdefpermission tdp on tdp.taskdef_id = td.id where tdp.permission = ?");) {

			ps.setString(1, permid.toString());
			try (ResultSet rs = ps.executeQuery();) {
				while (rs.next()) {
					ret.add(rs.getString("metaname"));
				}
				rs.close();
			}
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	protected Event<TaskDefPermission> getEvent() {
		return tdpEvent;
	}

}
