package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.repository.PropertyRepository;

@Stateless
public class PropertyService {

	@Inject
	private PropertyRepository prRep;

	public List<Property> listAll() {
		return prRep.listAll();
	}

	public Property findById(UUID id) {
		return prRep.findById(id);
	}

	public Property findByMetaname(String metaName) {
		return prRep.findByMetaname(metaName);
	}

	public void deletePropertyByID(UUID id) {
		Property property = new Property();
		property = findById(id);
		if (!"CANCELADO".equals(property.getStatus())) {
			property.setStatus("CANCELADO");
			prRep.persist(property);
		}

	}

	public Property persist(Property property) {
		return prRep.persist(property);
	}

	public void quickInsert(UUID thing_id, UUID propmodel_id, String value) {
		prRep.quickInsert(thing_id, propmodel_id, value);
	}

	public List<Property> listByThing(Thing t) {
		return prRep.listByField("Thing", t);
	}

	public void quickUpdateValue(Property pr) {
		prRep.quickInsert(pr.getThing().getId(), pr.getField().getId(), pr.getValue());
	}

	public void remove(Property prDel) {
		if (prDel.getId() != null)
			prRep.removeById(prDel.getId());
	}
}
