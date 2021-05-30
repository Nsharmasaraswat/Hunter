package com.gtp.hunter.process.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.model.AddressModel;
import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.model.util.Addresses;
import com.gtp.hunter.process.repository.AddressFieldRepository;
import com.gtp.hunter.process.repository.AddressRepository;

@Stateless
public class AddressService {

	@Inject
	private AddressRepository		addrRep;

	@Inject
	private AddressFieldRepository	afRep;

	@Inject
	private Logger					logger;

	public List<Address> listAll() {
		return addrRep.listAll();
	}

	public List<Address> listById(Collection<UUID> idList) {
		return addrRep.listById(idList);
	}

	public Address findById(UUID idAddress) {
		return addrRep.findById(idAddress);
	}

	public Address findByMetaname(String metaName) {
		return addrRep.findByMetaname(metaName);
	}

	public List<Address> listByModelMetaname(String metaName) {
		return addrRep.listByModelMetaname(metaName);
	}

	public List<Address> quickListByModelMetaname(String metaName) {
		return addrRep.quickListByModelMetaname(metaName);
	}

	public List<Address> quickListByModelMetanameAndLocationId(String metaName, UUID locId) {
		return addrRep.quickListByModelMetanameAndLocationId(metaName, locId);
	}

	public void deleteById(UUID addrId) {
		if (addrId != null) delete(findById(addrId));
	}

	public void delete(Address a) {
		if (a != null && a.getStatus() != null && !a.getStatus().equals("CANCELADO")) {
			a.setStatus("CANCELADO");
			addrRep.persist(a);
		}
	}

	public Address persist(Address address) {
		return addrRep.persist(address);
	}

	public List<Address> listByLocation(UUID locationId) {
		return addrRep.listByLocation(locationId);
	}

	public void insertNewAddress2(JsonObject addr) {
		Address a = new Address();
		if (addr.containsKey("id")) {
			a = addrRep.findById(UUID.fromString(addr.get("id").toString().replaceAll("\"", "")));
			if (a == null) a = new Address();
		} else {
			//			if(addr.containsKey("model_id")) {
			//				
			//			}
		}
		//WKTReader wrdr = new WKTReader();
		StdDateFormat fmt = new StdDateFormat();
		if (a.getId() == null) a.setId(UUID.fromString(addr.get("id").toString().replaceAll("\"", "")));
		a.setMetaname(addr.get("metaname").toString().replaceAll("\"", ""));
		a.setStatus(addr.get("status").toString().replaceAll("\"", ""));
		a.setName(addr.get("name").toString().replaceAll("\"", ""));
		try {
			logger.info(addr.get("createdAt").toString().replaceAll("\"", ""));
			a.setCreatedAt(fmt.parse(addr.get("createdAt").toString().replaceAll("\"", "")));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		a.setWkt(addr.get("wkt").toString().replaceAll("\"", ""));
		if (a.getId() != null && !a.getId().toString().isEmpty()) {
			addrRep.persist(a);
		} else {
			addrRep.persist(a);
		}
	}

	public List<Address> listByLocationIdFromUpdated(UUID locId, Date fromUpdated) {
		//		return fillWktList(addrRep.listByLocationNewerThan(locId, fromUpdated));
		return addrRep.listByLocationNewerThan(locId, fromUpdated);
	}

	public List<Address> listFromUpdated(Date fromUpdated) {
		//		return fillWktList(addrRep.listNewerThan(fromUpdated));
		return addrRep.listNewerThan(fromUpdated);
	}

	public Address findByName(String name) {
		return addrRep.findByField("name", name);
	}

	public List<Address> listOrphanByLocation(UUID locId) {
		return addrRep.listOrphanByLocation(locId);
	}

	public void quickUpdateWKT(UUID addressId, String wkt) {
		addrRep.quickUpdateRegionFromWKT(addressId, wkt);
	}

	public List<Address> listByModelAndLocation(AddressModel model, Location loc) {
		return addrRep.listByModelAndLocation(model, loc);
	}

	public Address quickFindParent(UUID id) {
		return addrRep.quickFindParent(id);
	}

	public Address findRandomByModelFieldValue(String modelFieldMeta, String value) {
		AddressField af = afRep.findRandomByModelMetaValue(modelFieldMeta, value);

		//TODO:GAMMMBIIIISSSSSSS
		return af == null ? addrRep.findById(UUID.fromString("47b079a1-7c42-11ea-9d3c-005056a19775")) : af.getAddress();
	}

	public List<Address> listEmptyByModelMetanameFieldValue(String modelMeta, String fieldMeta, String value) {
		List<AddressField> afList = afRep.listByModelMetaValue(fieldMeta, value);
		List<Address> addrList = afList.parallelStream()
						.map(af -> af.getAddress())
						.distinct()
						.collect(Collectors.toList());

		return addrList.isEmpty() ? new ArrayList<Address>() : addrRep.listById(addrList.parallelStream()
						.map(a -> a.getId())
						.collect(Collectors.toList()))
						.parallelStream()
						.filter(a -> Addresses.getStringField(a, "OCCUPIED").equalsIgnoreCase("FALSE"))
						.collect(Collectors.toList());
	}

	public Address findNearestByModelAndOriginWithFieldValue(String type, UUID originId, String field, String value) {
		Address origin = addrRep.findById(originId);
		List<AddressField> afList = afRep.listByModelMetaValue(field, value);
		List<Address> addrList = afList.parallelStream()
						.map(af -> af.getAddress())
						.distinct()
						.collect(Collectors.toList());

		return addrList.isEmpty() ? null : addrList
						.parallelStream()
						.sorted((a1, a2) -> {
							double dist1 = origin.getRegion().getCentroid().distance(a1.getRegion().getCentroid());
							double dist2 = origin.getRegion().getCentroid().distance(a2.getRegion().getCentroid());
							if (dist1 > dist2) return -1;
							if (dist1 < dist2) return 1;
							return 0;
						})
						.findFirst()
						.orElse(null);
	}

	public void removeById(UUID id) {
		addrRep.removeById(id);
	}

	//	private List<Address> fillWktList(List<Address> ret) {
	//		return ret.parallelStream().map(a -> {
	//			a.setWkt(a.getRegion().toText());
	//			return a;
	//		}).collect(Collectors.toList());
	//	}

}
