package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.repository.PropertyModelRepository;

@Stateless
public class PropertyModelService {

	@Inject
	private PropertyModelRepository prmRep;

	public List<PropertyModel> listAll() {
		return prmRep.listAll();
	}

	public PropertyModel findById(UUID id) {
		return prmRep.findById(id);
	}

	public PropertyModel findByMetaname(String metaName) {
		return prmRep.findByMetaname(metaName);
	}

	public void deleteById(UUID id) {
		if (id != null) delete(findById(id));
	}

	public void delete(PropertyModel propertyModel) {
		if (!"CANCELADO".equals(propertyModel.getStatus())) {
			propertyModel.setStatus("CANCELADO");
			prmRep.persist(propertyModel);
		}
	}

	public void persist(PropertyModel propertyModel) {
		prmRep.persist(propertyModel);
	}

	public void removeById(UUID id) {
		prmRep.removeById(id);
	}
}
