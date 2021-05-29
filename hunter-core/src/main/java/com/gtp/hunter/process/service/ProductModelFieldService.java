package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.ProductModelField;
import com.gtp.hunter.process.repository.ProductModelFieldRepository;

@Stateless
public class ProductModelFieldService {

	@Inject
	ProductModelFieldRepository prdmf;

	public List<ProductModelField> listAll() {
		return prdmf.listAll();
	}

	public ProductModelField findByById(UUID idProductModelField) {
		return prdmf.findById(idProductModelField);
	}

	public List<ProductModelField> listByModelId(UUID idProductModelField) {
		return prdmf.listByModelId(idProductModelField);
	}

	public void deleteById(UUID idProductModelField) {
		ProductModelField prMField = new ProductModelField();
		prMField = findByById(idProductModelField);
		if ("CANCELADO".equals(prMField.getStatus())) {
			prMField.setStatus("CANCELADO");
			prdmf.persist(prMField);
		}

	}

	public ProductModelField persist(ProductModelField productModelField) {
		return prdmf.persist(productModelField);
	}

	public void removeById(UUID id) {
		prdmf.removeById(id);
	}
}
