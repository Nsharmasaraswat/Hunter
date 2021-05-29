package com.gtp.hunter.process.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import com.gtp.hunter.common.util.DBUtil;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.DocumentModel;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DocumentModelRepository extends JPABaseRepository<DocumentModel, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager			em;

	@Inject
	private Event<DocumentModel>	dmEvent;

	public DocumentModelRepository() {
		super(DocumentModel.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public DocumentModel getByDocument(UUID doc) {
		DocumentModel ret = null;

		try (Connection con = initConnection();
						PreparedStatement ps = con.prepareStatement("select dm.* from documentmodel dm join document d on d.documentmodel_id = dm.id where d.id = ?");) {

			ps.setString(1, doc.toString());
			try (ResultSet rs = ps.executeQuery();) {
				List<DocumentModel> lst = (List<DocumentModel>) DBUtil.resultSetToList(rs, DocumentModel.class);
				if (lst.size() > 0)
					ret = lst.get(0);
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
	protected Event<DocumentModel> getEvent() {
		return dmEvent;
	}

}
