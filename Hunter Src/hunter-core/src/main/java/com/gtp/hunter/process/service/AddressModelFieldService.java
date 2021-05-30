package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.AddressModel;
import com.gtp.hunter.process.model.AddressModelField;
import com.gtp.hunter.process.repository.AddressModelFieldRepository;

@Stateless
public class AddressModelFieldService {

	@Inject
	AddressModelFieldRepository addMdFdRep;

	public List<AddressModelField> listAll() {
		return addMdFdRep.listAll();
	}

	public AddressModelField getById(UUID idAddressModelField) {
		return addMdFdRep.findById(idAddressModelField);
	}

	public void deleteAddressModelFieldByID(UUID idAddressModelField) {
		AddressModelField addrsMdFd = new AddressModelField();
		addrsMdFd = getById(idAddressModelField);
		if (!"CANCELADO".equals(addrsMdFd.getStatus())) {
			addrsMdFd.setStatus("CANCELADO");
			addMdFdRep.persist(addrsMdFd);
		}

	}

	public AddressModelField persist(AddressModelField amf) {
		return addMdFdRep.persist(amf);
	}

	public List<AddressModelField> listByModel(AddressModel model) {
		return addMdFdRep.listByField("model", model);
	}

	public void removeById(UUID id) {
		addMdFdRep.removeById(id);
	}
}
