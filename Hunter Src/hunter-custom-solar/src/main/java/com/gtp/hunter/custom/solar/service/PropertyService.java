package com.gtp.hunter.custom.solar.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;

import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;

@Stateless
public class PropertyService {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");

	public Set<Property> getProperties(Thing t, PropertyModelField prmfSTRWGHT, String strtWeight, PropertyModelField prmfACTWGHT, String actualWeight, PropertyModelField prmfLOT, String lot, PropertyModelField prmfMAN, Date man, PropertyModelField prmfEXP, Date exp, PropertyModelField prmfQTY, String qty) {
		Set<Property> ret = new HashSet<>();
		Property plsw = new Property(t, prmfSTRWGHT, strtWeight);
		Property plaw = new Property(t, prmfACTWGHT, actualWeight);
		Property pllot = new Property(t, prmfLOT, lot);
		Property plman = new Property(t, prmfMAN, DATE_FORMAT.format(man));
		Property plexp = new Property(t, prmfEXP, DATE_FORMAT.format(exp));
		Property plqty = new Property(t, prmfQTY, qty);

		plaw.setStatus("NOVO");
		plaw.setCreatedAt(man);
		plaw.setUpdatedAt(man);
		plsw.setStatus("NOVO");
		plsw.setCreatedAt(man);
		plsw.setUpdatedAt(man);
		pllot.setStatus("NOVO");
		pllot.setCreatedAt(man);
		pllot.setUpdatedAt(man);
		plman.setStatus("NOVO");
		plman.setCreatedAt(man);
		plman.setUpdatedAt(man);
		plexp.setStatus("NOVO");
		plexp.setCreatedAt(man);
		plexp.setUpdatedAt(man);
		plqty.setStatus("NOVO");
		plqty.setCreatedAt(man);
		plqty.setUpdatedAt(man);
		ret.add(plsw);
		ret.add(plaw);
		ret.add(pllot);
		ret.add(plman);
		ret.add(plexp);
		ret.add(plqty);
		return ret;
	}

}
