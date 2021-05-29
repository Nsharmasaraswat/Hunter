package com.gtp.hunter.ejbcommon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil {

	private static final String							BASE_DIR		= System.getProperty("jboss.server.config.dir");
	private static final String							FILE_EXTENSION	= ".properties";
	private static final Map<String, OrderedProperties>	props			= new ConcurrentHashMap<>();

	private transient static final Logger				logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static void reloadAll() {
		props.keySet().forEach(f -> {
			load(f);
		});
	}

	public static void reloadIfExists(String config) {
		if (config.toLowerCase().endsWith(FILE_EXTENSION)) config = config.replace(FILE_EXTENSION, "");
		if (props.containsKey(config)) {
			load(config);
			logger.info("Reloaded configuration " + config);
		}
	}

	public static void load(String config) {
		try {
			if (config.toLowerCase().endsWith(FILE_EXTENSION)) config = config.replace(FILE_EXTENSION, "");
			OrderedProperties prop = new OrderedProperties();
			String data = BASE_DIR + "/" + config + FILE_EXTENSION;
			logger.debug("Loading file: " + data);

			if (!(new File(data).exists())) {
				new FileOutputStream(data).close();
			}
			prop.load(new FileInputStream(data));
			props.put(config, prop);
			for (Object obj : prop.keySet()) {
				String k = (String) obj;
				String v = prop.getProperty(k);

				logger.debug("\t- " + k + "=" + v);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void save(OrderedProperties p, String config) {
		try {
			String data = BASE_DIR + "/" + config + FILE_EXTENSION;
			FileOutputStream fos = new FileOutputStream(data);

			p.store(fos, "");
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static OrderedProperties getProps(String config) {
		if (!props.containsKey(config)) load(config);
		return props.get(config);
	}

	public static String get(String config, String key, String defaultValue) {
		String ret = null;

		//if there file is not in the map load it
		//if there's no attribute on the prop load it in case it was added later
		if (!props.containsKey(config) || !props.get(config).containsKey(key)) {
			load(config);
		}
		if (props.get(config).containsKey(key)) {
			ret = (String) props.get(config).get(key);
		} else {
			if (defaultValue != null) {
				OrderedProperties p = props.get(config);

				p.put(key, defaultValue);
				save(p, config);
			}
			ret = defaultValue;
		}
		return ret;
	}

	public static void put(String config, String key, String value) {

		if (!props.containsKey(config)) {
			load(config);
		}
		if (props.get(config).containsKey(key)) {
			props.get(config).put(key, value);
			save(props.get(config), config);
		} else {
			OrderedProperties p = props.get(config);
			p.put(key, value);
			save(p, config);
		}
	}
}
