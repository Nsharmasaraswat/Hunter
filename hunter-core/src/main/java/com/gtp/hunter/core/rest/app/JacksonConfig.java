package com.gtp.hunter.core.rest.app;

import java.util.TimeZone;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
	private final ObjectMapper objectMapper;

	public JacksonConfig() throws Exception {
		objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.registerModule(new Hibernate5Module().configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true));
		objectMapper.setDateFormat(new StdDateFormat().withTimeZone(TimeZone.getTimeZone("America/Sao_Paulo")));
	}

	@Override
	public ObjectMapper getContext(Class<?> arg0) {
		return objectMapper;
	}
}