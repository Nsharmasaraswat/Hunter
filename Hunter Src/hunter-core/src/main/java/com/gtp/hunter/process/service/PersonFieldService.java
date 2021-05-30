package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.PersonField;
import com.gtp.hunter.process.repository.PersonFieldRepository;

@Stateless
public class PersonFieldService {

	@Inject
	private PersonFieldRepository psfRep;

	public List<PersonField> getListAllPersonField() {
		return psfRep.listAll();
	}

	public PersonField findById(UUID id) {
		return psfRep.findById(id);
	}

	public PersonField findByMetaname(String metaName) {
		return psfRep.findByMetaname(metaName);
	}

	public void deleteById(UUID id) {
		if (id != null) delete(findById(id));
	}

	public void delete(PersonField psf) {
		if (psf != null && psf.getStatus() != null && !psf.getStatus().equals("CANCELADO")) {
			psf.setStatus("CANCELADO");
			psfRep.persist(psf);
		}
	}

	public PersonField persist(PersonField personField) {
		return psfRep.persist(personField);
	}
}
