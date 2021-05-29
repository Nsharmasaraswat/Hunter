package com.gtp.hunter.ejbcommon.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;

public class OrderedProperties extends Properties {

	/**
	 * 
	 */
	private static final long			serialVersionUID	= 1268369456847374510L;
	private final LinkedHashSet<Object>	keys				= new LinkedHashSet<Object>();

	public Enumeration<Object> keys() {
		return Collections.<Object> enumeration(keys);
	}

	public Object put(Object key, Object value) {
		keys.add(key);
		return super.put(key, value);
	}

}
