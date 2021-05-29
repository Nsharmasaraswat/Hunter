package com.gtp.hunter.custom.eurofarma.process;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.custom.eurofarma.json.BaseMessage;
import com.gtp.hunter.custom.eurofarma.json.CPIIntegrationMessage;
import com.gtp.hunter.custom.eurofarma.json.ProductCheckingMessage;
import com.gtp.hunter.custom.eurofarma.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.util.Addresses;
import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.process.wf.process.interfaces.ExternalProcessor;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;
import com.gtp.hunter.ui.json.process.GenericMessage;

public class ProductCheckingExternalProcessor<T extends CPIIntegrationMessage<S>, S extends BaseMessage> implements ExternalProcessor {
	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String				_TYPE	= "PALLET_CHECK";

	private String							base_url;
	private String							method;
	private String							verb;
	private String							userName;
	private String							password;
	private IntegrationService				is;

	public ProductCheckingExternalProcessor(IntegrationService is) {
		this.is = is;
	}

	@Override
	public BaseProcessMessage process(BaseProcess proc, Object data) {
		String wm = proc.getModel().getOrigin().getParams();
		base_url = (String) proc.getParametros().get("base-url");
		method = (String) proc.getParametros().get("method");
		verb = (String) proc.getParametros().get("verb");
		userName = (String) proc.getParametros().get("username");
		password = (String) proc.getParametros().get("password");
		JsonObject ret = null;
		JsonReader jsonReader = Json.createReader(new StringReader(buildMessage((String) data, wm).toString()));

		ret = jsonReader.readObject();
		jsonReader.close();
		return sendMessageReturn(ret, wm);
	}

	@Override
	public String getType() {
		return _TYPE;
	}

	//TODO: Return T
	private CPIIntegrationMessage<ProductCheckingMessage> buildMessage(String tagId, String wm) {
		CPIIntegrationMessage<ProductCheckingMessage> cm = new CPIIntegrationMessage<>();
		ProductCheckingMessage trm = new ProductCheckingMessage();

		trm.setCodigo(tagId);
		trm.setData(Calendar.getInstance().getTime());
		trm.setWm(wm);
		cm.setCommand("ESPECIFICACAO");
		cm.setData(trm);
		return cm;
	}

	GenericMessage sendMessageReturn(JsonObject jo, String wm) {
		GenericMessage iRet = new GenericMessage("CANCEL");
		String path = base_url + (method.startsWith("/") ? method : "/" + method);
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

		try (CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).disableContentCompression().build();) {
			ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpClient);
			ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
							.establishConnectionTimeout(new Long(ConfigUtil.get("hunter-ejb-common", "rest-connect-timeout", "3000")), TimeUnit.MILLISECONDS)
							.socketTimeout(new Long(ConfigUtil.get("hunter-ejb-common", "rest-read-timeout", "3000")), TimeUnit.MILLISECONDS)
							.httpEngine(engine);
			if (userName != null && password != null)
				clientBuilder.register(new BasicAuthentication(userName, password));
			ResteasyClient client = clientBuilder.build();
			ResteasyWebTarget target = client.target(UriBuilder.fromPath(path));
			IntegrationReturn resp = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jo), IntegrationReturn.class);

			logger.info("Request: " + jo.toString() + "\n Response " + resp.toString());
			if (resp.isResult()) {
				String productType = resp.getMessage();
				List<Address> addList = is.getRegSvc().getAddSvc().listEmptyByModelMetanameFieldValue("RACK", "PRODUCT_TYPE", productType);

				addList.removeIf(a -> !Addresses.getStringField(a, "WM_SAP").equals(wm));
				if (addList.isEmpty()) {
					iRet = new GenericMessage("CANCEL");
					iRet.setData("SEM ENDEREÇO DISPONÍVEL");
				} else {
					Address add = addList.get(0);
					Unit un = add.getUnits().parallelStream().findAny().get();

					iRet = new GenericMessage("TRANSPORT_PALLET");
					iRet.setData(un == null ? add.getName() : un.getTagId());
				}
			} else {
				iRet = new GenericMessage("CANCEL");
				iRet.setData(resp.getMessage());
			}
			engine.finalize();
		} catch (Throwable e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return iRet;
	}
}
