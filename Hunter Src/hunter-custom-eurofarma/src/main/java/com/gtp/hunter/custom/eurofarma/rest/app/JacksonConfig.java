package com.gtp.hunter.custom.eurofarma.rest.app;

import java.util.TimeZone;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
	private final ObjectMapper objectMapper;

	public JacksonConfig() throws Exception {
		objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.setDateFormat(new StdDateFormat().withTimeZone(TimeZone.getTimeZone("America/Sao_Paulo")));
	}

	@Override
	public ObjectMapper getContext(Class<?> arg0) {
		return objectMapper;
	}
}