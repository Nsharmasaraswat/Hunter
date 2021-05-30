package com.gtp.hunter.custom.solar.soap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.validation.constraints.NotNull;

import com.gtp.hunter.custom.solar.service.SAPService;

@WebService(name = "SolarSOAP", serviceName = "HunterService", targetNamespace = "http://gtpautomation.com/hunter")
@SOAPBinding(style = Style.RPC)
@RequestScoped
public class SolarSOAP {

	private static final List<String> processing = new ArrayList<>();

	static {
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> processing.clear(), 1, 299, TimeUnit.SECONDS);
	}

	@Inject
	private SAPService svc;

	@WebMethod(operationName = "echo", action = "urn:Echo")
	public String echo(String text) {
		System.out.println("ENVIANDO TEXTO " + text);
		return text;
	}

	@WebMethod(operationName = "call", action = "urn:Call")
	public boolean call(@NotNull Integer code, @NotNull String controle) {
		if (!processing.contains(code + "-" + controle)) {
			processing.add(code + "-" + controle);
			System.out.println("SAP CALL: " + code + " - " + controle);
			return svc.call(code, controle);
		}
		return true;
	}

}
