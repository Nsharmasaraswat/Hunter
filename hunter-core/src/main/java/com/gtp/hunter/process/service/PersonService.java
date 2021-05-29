package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Person;
import com.gtp.hunter.process.model.PersonModel;
import com.gtp.hunter.process.repository.PersonRepository;

@Stateless
public class PersonService {

	@Inject
	private PersonRepository psRep;

	public void delete(UUID idAddress) {
		Person person = new Person();
		person = findById(idAddress);
		if (!"CANCELADO".equals(person.getStatus())) {
			person.setStatus("CANCELADO");
			psRep.persist(person);
		}

	}

	public boolean contains(Person ps) {
		return psRep.contains(ps);
	}

	public Person findByCode(String code) {
		return psRep.findByField("code", code);
	}

	public Person findById(UUID idAddress) {
		return psRep.findById(idAddress);
	}

	public Person findByMetaname(String metaName) {
		return psRep.findByMetaname(metaName);
	}

	public Person getByModelAndCode(String model, String code) {
		return psRep.getByModelAndCode(model, code);
	}

	public List<Person> getByPropModelAndMetaname(String type, String prop, String value) {
		return psRep.getByPropModelAndMetaname(type, prop, value);
	}

	public List<Person> getListAllPerson() {
		return psRep.listAll();
	}

	public List<Person> listByModel(PersonModel model) {
		return psRep.listByField("model", model);
	}

	public Person persist(Person person) {
		return psRep.persist(person);
	}

	public List<Person> listByModelMetaname(String string) {
		return psRep.listByModelMetaname(string);
	}
}
