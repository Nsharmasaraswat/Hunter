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

import com.gtp.hunter.common.util.DBUtil;
import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.Product;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class ProductRepository extends JPABaseRepository<Product, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Product>	prodEvent;

	public ProductRepository() {
		super(Product.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public List<Product> quickListByModelMetaname(String value) {
		EntityManager em = getEntityManager();

		//		List<Product> ret = em.createQuery("from Product p left join fetch p.fields flds left join fetch flds.model fmdl where p.model.metaname = :fld and fmdl.model = p.model").setParameter("fld", value).getResultList();
		List<Product> ret = new ArrayList<>();

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT p.* FROM product p JOIN productmodel pm on p.productmodel_id = pm.id where pm.metaname = ?");) {

			ps.setString(1, value);
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Product>) DBUtil.resultSetToList(rs, Product.class);
				rs.close();
			}
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Product> quickListByDocument(UUID documentId) {
		List<Product> ret = new ArrayList<>();

		//		logger.info("Document ID: " + documentId.toString());
		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT p.* FROM product p INNER JOIN documentitem di ON di.product_id = p.id WHERE di.document_id = ?");) {

			ps.setString(1, documentId.toString());
			try (ResultSet rs = ps.executeQuery();) {
				ret = (List<Product>) DBUtil.resultSetToList(rs, Product.class);
				rs.close();
			}
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public Product quickGetBySku(String sku) {
		Product ret = null;

		try (Connection con = initConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM product WHERE sku = ?");) {

			ps.setString(1, sku);
			try (ResultSet rs = ps.executeQuery();) {
				List<Product> lstret = (List<Product>) DBUtil.resultSetToList(rs, Product.class);
				if (lstret.size() > 1)
					ret = lstret.get(0);
				rs.close();
			}
			ps.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public List<Product> listByModelMetaname(String modelMeta) {
		return em.createQuery("from Product where model.metaname = :model", Product.class)
						.setParameter("model", modelMeta)
						.getResultList();
	}

	public List<Product> listByModelMetanameAndSiblings(String modelMeta) {
		return em.createQuery("from Product where (model.parent is null and model.metaname = :model) or (model.parent is not null and model.parent.metaname = :pmodel)", Product.class)
						.setParameter("model", modelMeta)
						.setParameter("pmodel", modelMeta)
						.getResultList();
	}

	@Override
	protected Event<Product> getEvent() {
		return prodEvent;
	}
}
