package com.gtp.hunter.custom.eurofarma.rest.app;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DateDeserializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").parse(p.getText());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

}