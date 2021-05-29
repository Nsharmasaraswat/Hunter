package com.gtp.hunter.core.coap;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.service.RawDataService;
import com.gtp.hunter.core.service.SourceService;

public class SourceCoapResource extends CoapResource {

	private RawDataService		rds;

	private SourceService		sSvc;

	private Gson				gson;

	private Map<String, String>	parameters	= new HashMap<>();

	public SourceCoapResource(SourceService sSvc, RawDataService rds) {
		super("source");
		this.sSvc = sSvc;
		this.rds = rds;
		this.gson = new GsonBuilder().create();
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		handleEvent(exchange);
	}

	@Override
	public void handlePUT(CoapExchange exchange) {
		handleEvent(exchange);
	}

	private void handleEvent(CoapExchange exchange) {
		for (String param : exchange.getRequestOptions().getUriQuery()) {
			String[] parts = param.split("=");
			parameters.put(parts[0], parts[1]);
		}
		if (sSvc.verifyToken(parameters.get("token"))) {
			ComplexData lst = gson.fromJson(new String(exchange.getRequestText()), ComplexData.class);
			rds.processRawData(lst);
			exchange.respond(ResponseCode.VALID);
		} else {
			LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()).info("Source not authenticated");
			exchange.respond(ResponseCode.UNAUTHORIZED);
		}
	}
}
