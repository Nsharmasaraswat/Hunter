package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.PersonModelField;
import com.gtp.hunter.process.repository.PersonModelFieldRepository;

@Stateless
public class PersonModelFieldService {

	@Inject
	private PersonModelFieldRepository psmfRep;

	public List<PersonModelField> listAll() {
		return psmfRep.listAll();
	}

	public PersonModelField findById(UUID id) {
		return psmfRep.findById(id);
	}

	public PersonModelField findByMetaname(String metaName) {
		return psmfRep.findByMetaname(metaName);
	}

	public void deleteByID(UUID id) {
		if (id != null) delete(findById(id));
	}

	public void delete(PersonModelField psmf) {
		if (psmf != null && psmf.getStatus() != null && !psmf.getStatus().equals("CANCELADO")) {
			psmf.setStatus("CANCELADO");
			psmfRep.persist(psmf);
		}
	}

	public PersonModelField persist(PersonModelField personModelField) {
		return psmfRep.persist(personModelField);
	}
}
