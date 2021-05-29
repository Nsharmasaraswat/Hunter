package com.gtp.hunter.process;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.common.util.ReflectionUtil;
import com.gtp.hunter.process.model.Document;

public class SimpleTestRun {
	@Inject
	private static transient Logger logger;

	public static void main(String[] args) {
		logger.debug(ReflectionUtil.getPropertyType(Document.class, "things.thing.product.sku").toGenericString());
	}

}
