package com.gtp.hunter.core.coap;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

import com.gtp.hunter.core.service.RawDataService;
import com.gtp.hunter.core.service.SourceService;

@ApplicationScoped
public class SourceCoapServer extends CoapServer {

	@Inject
	private RawDataService		rds;

	@Inject
	private SourceService		sSvc;

	private static final int	COAP_PORT	= NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

	private void addEndpoints() {
		for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
			if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {
				InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
				CoapEndpoint.Builder coapBuilder = new CoapEndpoint.Builder();

				coapBuilder.setInetSocketAddress(bindToAddress);
				addEndpoint(coapBuilder.build());
			}
		}
	}

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		setMessageDeliverer(new MultipathDeliverer(getRoot()));
		add(new SourceCoapResource(sSvc, rds));
		addEndpoints();
		// addInterceptors();
		start();
	}

	public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
		this.stop();
	}

	protected Resource createRoot() {
		return new HunterRoot();
	}

	/**
	 * Represents the root of a resource tree.
	 */
	private class HunterRoot extends CoapResource {

		// get version from Maven package
		private static final String	SPACE	= "                                               ";																							// 47 until line end
		private final String		VERSION	= CoapServer.class.getPackage().getImplementationVersion() != null ? "Cf " + CoapServer.class.getPackage().getImplementationVersion() : SPACE;
		//@formatter:off
		private final String		msg		= new StringBuilder().append("************************************************************\n").append("CoAP RFC 7252").append(SPACE.substring(VERSION.length())).append(VERSION).append("\n").append("************************************************************\n").append("hunter IoT Platform CoAP root resource\n").append("************************************************************").toString();
		//@formatter:on

		public HunterRoot() {
			super("hunter-core");
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(ResponseCode.CONTENT, msg);
		}

		public List<Endpoint> getEndpoints() {
			return SourceCoapServer.this.getEndpoints();
		}
	}

}
