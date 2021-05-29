package com.gtp.hunter.core.service;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.repository.DeviceRepository;

@Stateless
public class DeviceService {

	@Inject
	private DeviceRepository devRep;

	public Device findByMetaname(UUID src, String metaname) {
		return devRep.findByMetaname(src, metaname);
	}

	public Device findById(UUID id) {
		return devRep.findById(id);
	}
}
