package com.gtp.hunter.ejbcommon.util;

import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class Resources {

	@Produces
	public Logger produceSLF4JLogger(InjectionPoint injectionPoint) {
		final Logger ret = LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass());

		return ret;
	}

	@Produces
	public FacesContext getFacesContext() {                                 // (3)
		return FacesContext.getCurrentInstance();
	}
}
