package com.gtp.hunter.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import com.google.gson.Gson;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.core.model.ComplexData;

public class GETClient {

	/*
	 * Application entry point.
	 * 
	 */
	
	private static Gson g = new Gson();
	
	public static void main(String args[]) {

		URI uri = null;
		try {
			uri = new URI("coap://127.0.0.1:5683/control");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		CoapClient client = new CoapClient(uri);
		client.useNONs();
		for (int l = 0; l < 10; l++) {
			List<ComplexData> lst = new ArrayList<ComplexData>();
			long cnt = Math.round(Math.random() * 250);
			for (int x = 0; x < cnt; x++) {
				ComplexData r = new ComplexData();
				r.setSource(UUID.randomUUID());
				r.setDevice(UUID.randomUUID());
				r.setPort(0);
				r.setTagId(UUID.randomUUID().toString());
				r.setType(RawDataType.IDENT);
				r.setPayload("L: " + l + " - X: " + x);
				r.setTs(new Date().getTime());
				lst.add(r);
				
				client.put(g.toJson(r), MediaTypeRegistry.TEXT_PLAIN);
			}

		}
	}

}
