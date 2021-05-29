package com.gtp.hunter.process.jsonstubs;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLDocModelResources {

	@Expose
	private Set<AGLProd>			products	= new ConcurrentSkipListSet<>();

	@Expose
	private AGLTruck				truck;

	@Expose
	private Set<AGLAddressProps>	addresses	= new ConcurrentSkipListSet<>();

	public Set<AGLProd> getProducts() {
		return products;
	}

	public void setProducts(Set<AGLProd> products) {
		this.products = products;
	}

	public AGLTruck getTruck() {
		return truck;
	}

	public void setTruck(AGLTruck truck) {
		this.truck = truck;
	}

	public Set<AGLAddressProps> getAddresses() {
		return addresses;
	}

	public void setAddresses(Set<AGLAddressProps> addresses) {
		this.addresses = addresses;
	}

}
