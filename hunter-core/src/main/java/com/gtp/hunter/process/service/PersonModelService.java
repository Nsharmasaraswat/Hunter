package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.PersonModel;
import com.gtp.hunter.process.repository.PersonModelRepository;

@Stateless
public class PersonModelService {

	@Inject
	private PersonModelRepository psmRep;

	public List<PersonModel> getListAllPersonModel() {
		return psmRep.listAll();
	}

	public PersonModel findById(UUID id) {
		return psmRep.findById(id);
	}

	public void delete(UUID id) {
		PersonModel personModel = new PersonModel();

		personModel = findById(id);
		if (!"CANCELADO".equals(personModel.getStatus())) {
			personModel.setStatus("CANCELADO");
			psmRep.persist(personModel);
		}

	}

	public PersonModel persist(PersonModel personModel) {
		return psmRep.persist(personModel);
	}

	public PersonModel findByMetaname(String metaname) {
		return psmRep.findByMetaname(metaname);
	}

}
