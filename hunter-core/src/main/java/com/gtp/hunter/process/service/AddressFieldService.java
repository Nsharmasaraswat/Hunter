package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.repository.AddressFieldRepository;

@Stateless
public class AddressFieldService {

	@Inject
	private AddressFieldRepository addFdRep;

	public List<AddressField> listAll() {
		return addFdRep.listAll();
	}

	public AddressField findById(UUID idAddressField) {
		return addFdRep.findById(idAddressField);
	}

	public void deleteById(UUID idAddressField) {
		AddressField addrsFd = new AddressField();
		addrsFd = findById(idAddressField);
		if (!"CANCELADO".equals(addrsFd.getStatus())) {
			addrsFd.setStatus("CANCELADO");
			addFdRep.persist(addrsFd);
		}

	}

	public List<AddressField> listByAddressId(UUID addressId) {
		return addressId != null ? addFdRep.listByField("address_id", addressId.toString()) : null;
	}

	public AddressField persist(AddressField addressField) {
		return addFdRep.persist(addressField);
	}
}
