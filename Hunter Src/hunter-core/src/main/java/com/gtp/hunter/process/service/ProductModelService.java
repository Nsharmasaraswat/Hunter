package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.repository.ProductModelRepository;

@Stateless
public class ProductModelService {

	@Inject
	private ProductModelRepository pmRep;

	public void delete(ProductModel pm) {
		if (pm != null && pm.getStatus() != null && !pm.getStatus().equals("CANCELADO")) {
			pm.setStatus("CANCELADO");
			pmRep.persist(pm);
		}
	}

	public void deleteById(UUID pmId) {
		if (pmId != null) delete(findById(pmId));
	}

	public ProductModel findById(UUID idProductModel) {
		return pmRep.findById(idProductModel);
	}

	public ProductModel findByMetaname(String metaname) {
		return pmRep.findByMetaname(metaname);
	}

	public List<ProductModel> listAll() {
		return pmRep.listAll();
	}

	public ProductModel persist(ProductModel productModel) {
		return pmRep.persist(productModel);
	}

	public List<ProductModel> listByParentId(UUID parent_id) {
		List<ProductModel> ret = pmRep.listByField("parent.id", parent_id);

		return ret;
	}
}
