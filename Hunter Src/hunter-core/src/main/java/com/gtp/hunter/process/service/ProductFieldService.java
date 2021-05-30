package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.repository.ProductFieldRepository;

@Stateless
public class ProductFieldService {

	@Inject
	ProductFieldRepository pfRep;

	public List<ProductField> listAll() {
		return pfRep.listAll();
	}

	public ProductField findById(UUID idProductField) {
		return pfRep.findById(idProductField);
	}

	public void deleteProductFieldByID(UUID idProductField) {
		ProductField prdField = new ProductField();

		prdField = findById(idProductField);
		if (!"CANCELADO".equals(prdField.getStatus())) {
			prdField.setStatus("CANCELADO");
			pfRep.persist(prdField);
		}

	}

	public ProductField persist(ProductField productField) {
		return pfRep.persist(productField);
	}

	public List<ProductField> listByProduct(Product prd) {
		return pfRep.listByField("product", prd);
	}

	public List<ProductField> quickListByProductId(UUID id) {
		return pfRep.quickListByProductId(id);
	}

	public void remove(ProductField pf) {
		if (pf != null && pf.getId() != null)
			pfRep.removeById(pf.getId());
	}

	public void quickUpdateModel(UUID pfId, UUID pfModelId) {
		pfRep.quickChangeModel(pfId, pfModelId);
	}

}
