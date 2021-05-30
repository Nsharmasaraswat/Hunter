package com.gtp.hunter.process.service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.repository.ProductRepository;
import com.gtp.hunter.process.repository.ThingRepository;

@Stateless
public class ProductService {

	@Inject
	private ProductRepository	prdRep;

	@Inject
	private ThingRepository		thRep;

	public void deleteProductByID(UUID id) {
		if (id != null) delete(findById(id));
	}

	public void delete(Product prd) {
		if (prd != null && prd.getStatus() != null && !prd.getStatus().equals("CANCELADO")) {
			prd.setStatus("CANCELADO");
			prdRep.persist(prd);
		}
	}

	public Product findById(UUID idProduct) {
		return prdRep.findById(idProduct);
	}

	public Product findByMetaname(String metaName) {
		return prdRep.findByMetaname(metaName);
	}

	public Product findBySKU(String sku) {
		return prdRep.findByField("sku", sku);
	}

	public List<Product> listAll() {
		return prdRep.listAll();
	}

	public List<Product> listByDocumentId(UUID documentId) {
		return prdRep.quickListByDocument(documentId);
	}

	public List<Product> listByModelMetaname(String metaName) {
		return prdRep.listByModelMetaname(metaName);
	}

	public List<Product> listByModelMetanameAndSiblings(String metaName) {
		return prdRep.listByModelMetanameAndSiblings(metaName);
	}

	public List<Product> quickListByModelMetaname(String metaName) {
		return prdRep.quickListByModelMetaname(metaName);
	}

	public List<Product> listFromUpdated(Date fromUpdated) {
		return prdRep.listNewerThan(fromUpdated);
	}

	public Product persist(Product product) {
		prdRep.persist(product);
		return product;
	}

	//Product most present in address
	public Product findByAddressParent(UUID addressId) {
		Optional<Entry<Product, Long>> optRet = thRep.listByChildAddressId(addressId)
						.stream()
						.collect(Collectors.groupingBy(Thing::getProduct, Collectors.counting()))
						.entrySet()
						.stream()
						.sorted(Comparator.comparing(Entry<Product, Long>::getValue).reversed())
						.findFirst();

		return optRet.isPresent() ? optRet.get().getKey() : null;
	}

	//Product most present in address
	public Product findByAddress(UUID addressId) {
		Optional<Entry<Product, Long>> optRet = thRep.listByAddressIdNoOrphan(addressId)
						.stream()
						.collect(Collectors.groupingBy(Thing::getProduct, Collectors.counting()))
						.entrySet()
						.stream()
						.sorted(Comparator.comparing(Entry<Product, Long>::getValue).reversed())
						.findFirst();

		return optRet.isPresent() ? optRet.get().getKey() : null;
	}
}