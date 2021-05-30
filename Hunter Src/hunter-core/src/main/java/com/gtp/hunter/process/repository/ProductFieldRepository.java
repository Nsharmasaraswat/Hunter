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
import javax.persistence.EntityManager;

import com.gtp.hunter.common.util.DBUtil;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.ProductField;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class ProductFieldRepository extends JPABaseRepository<ProductField, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager		em;

	@Inject
	private Event<ProductField>	pfEvent;

	public ProductFieldRepository() {
		super(ProductField.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<ProductField> getEvent() {
		return pfEvent;
	}

	public List<ProductField> quickListByProductId(UUID productId) {
		List<ProductField> ret = new ArrayList<>();

		try (Connection con = initConnection();
						PreparedStatement ps = con
										.prepareStatement("SELECT pf.* FROM productfield pf WHERE pf.product_id = ?");) {
			ps.setString(1, productId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<ProductField>) DBUtil.resultSetToList(rs, ProductField.class);
				rs.close();
			}

			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public void quickChangeModel(UUID pfId, UUID pfModelId) {
		StringBuilder query = new StringBuilder("UPDATE productfield");

		query.append(" SET productmodelfield_id = ?");
		query.append(" WHERE id = ?");
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement(query.toString());) {

			ps.setString(1, pfModelId.toString());
			ps.setString(2, pfId.toString());
			ps.executeUpdate();
			ps.close();
			closeConnection(con);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
