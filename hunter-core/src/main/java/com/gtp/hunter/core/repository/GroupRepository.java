package com.gtp.hunter.core.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.User;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class GroupRepository extends JPABaseRepository<Group, UUID> {

	@Inject
	//	@Named("CorePersistence")
	private EntityManager	em;

	@Inject
	private Event<Group>	groupEvent;

	public GroupRepository() {
		super(Group.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public List<Group> getFilhos(Group grp) {
		List<Group> tmpret = em.createNativeQuery("select g.* from groups g join groupjoin gj on gj.inside_id = g.id where gj.group_id = :id", Group.class)
						.setParameter("id", grp.getId())
						.getResultList();
		List<Group> ret = new ArrayList<Group>();
		for (Group g : tmpret) {
			ret.addAll(getFilhos(g));
		}
		ret.addAll(tmpret);

		return ret;

	}

	@SuppressWarnings("unchecked")
	public List<Group> getGroupsByUser(User u) {
		Query q = em.createQuery("from Group g join g.users users where users.id = :user").setParameter("user", u.getId());
		return q.getResultList();
	}

	@Override
	protected Event<Group> getEvent() {
		return groupEvent;
	}

}
