package com.gtp.hunter.core.coap;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.interceptors.MessageInterceptorAdapter;

public class StatusControlCoapInterceptor extends MessageInterceptorAdapter {
	
	private SourceCoapServer srv;
	
	public StatusControlCoapInterceptor(SourceCoapServer srv) {
		this.srv = srv;
	}
	
	@Override
	public void receiveRequest(Request request) {
//		if(source.containsKey(request.getSource().getHostAddress())) {
//
//		} else {
//			source.put(request.getSource().getHostAddress(), UUID.randomUUID());
//		}

	}

	
	
}
