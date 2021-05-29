package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.repository.PropertyModelFieldRepository;

@Stateless
public class PropertyModelFieldService {

	@Inject
	private PropertyModelFieldRepository prmfRep;

	public List<PropertyModelField> listAll() {
		return prmfRep.listAll();
	}

	public List<PropertyModelField> listByModelId(UUID id) {
		return prmfRep.listByModel(id);
	}

	public PropertyModelField findById(UUID id) {
		return prmfRep.findById(id);
	}

	@Deprecated//use findByModelAndMetaname
	public PropertyModelField findByMetaname(String metaName) {
		return prmfRep.findByMetaname(metaName);
	}

	public void deleteById(UUID id) {
		if (id != null) delete(findById(id));
	}

	public void delete(PropertyModelField prmf) {
		if (prmf != null && prmf.getStatus() != null && !prmf.getStatus().equals("CANCELADO")) {
			prmf.setStatus("CANCELADO");
			prmfRep.persist(prmf);
		}
	}

	public PropertyModelField persist(PropertyModelField propertyModelField) {
		return prmfRep.persist(propertyModelField);
	}

	public List<PropertyModelField> listQuickPropertyModelFieldFromProduct(UUID prod) {
		return prmfRep.listQuickPropertyModelFieldFromProduct(prod);
	}

	public PropertyModelField findByModelAndMetaname(PropertyModel pm, String metaname) {
		return prmfRep.findByModelAndMetaname(pm, metaname);
	}

	public void removeById(UUID id) {
		prmfRep.removeById(id);
	}
}
