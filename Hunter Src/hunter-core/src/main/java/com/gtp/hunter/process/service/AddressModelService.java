package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.AddressModel;
import com.gtp.hunter.process.repository.AddressModelRepository;

@Stateless
public class AddressModelService {

	@Inject
	AddressModelRepository addMdRep;

	public List<AddressModel> getListAllAddressModel() {
		return addMdRep.listAll();
	}

	public AddressModel findById(UUID idAddressModel) {
		return addMdRep.findById(idAddressModel);
	}

	public void deleteById(UUID idAddressModel) {
		AddressModel addrsMd = new AddressModel();

		addrsMd = findById(idAddressModel);
		if (!"CANCELADO".equals(addrsMd.getStatus())) {
			addrsMd.setStatus("CANCELADO");
			addMdRep.persist(addrsMd);
		}

	}

	public AddressModel persist(AddressModel addressModel) {
		return addMdRep.persist(addressModel);
	}

	public AddressModel findByMetaname(String modelMeta) {
		return addMdRep.findByMetaname(modelMeta);
	}

	public void removeById(UUID id) {
		addMdRep.removeById(id);
	}
}
