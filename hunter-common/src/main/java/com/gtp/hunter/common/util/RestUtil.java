package com.gtp.hunter.common.util;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

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

import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

public class RestUtil {

	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final AtomicLong				count		= new AtomicLong(0);
	private final ExecutorService			executor	= Executors.newCachedThreadPool(runnable -> {
															Thread thread = new Thread(runnable);

															thread.setName("REST-INTEGRATION-" + count.getAndIncrement());
															return thread;
														});
	private String							base_url;

	public RestUtil(String base) {
		base_url = base;
	}

	public Future<IntegrationReturn> sendAsync(JsonObject jo, String method, String verb, String id) {
		return sendAsync(jo, method, verb, id, null, null);
	}

	public Future<IntegrationReturn> sendAsync(JsonObject jo, String method, String verb, String id, String userName, String password) {
		Callable<IntegrationReturn> task = () -> {
			Profiler prof = new Profiler("REST-UTIL");
			IntegrationReturn iRet = null;
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
				logger.info("Executing " + verb + " on " + target.getUri().toString() + (id == null || id.isEmpty() ? "" : "?id=" + id));

				switch (verb) {
					case "POST":
						iRet = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), IntegrationReturn.class);
						break;
					case "PUT":
						iRet = target.queryParam("id", id).request(MediaType.APPLICATION_JSON).put(Entity.json(jo), IntegrationReturn.class);
						break;
					case "DELETE":
						iRet = target.queryParam("id", id).request(MediaType.APPLICATION_JSON).delete(IntegrationReturn.class);
						break;
				}

				if (iRet.isResult()) {
					List<String> log = prof.done("Sucessoooo!!!!", false, false);
					logger.info(log.get(log.size() - 1));
				} else {
					List<String> log = prof.done("Falhouuuuuuu¡¡¡¡¡ - " + iRet.getMessage(), false, false);
					logger.info(log.get(log.size() - 1));
				}
				engine.finalize();
			} catch (Throwable e) {
				logger.error(e.getLocalizedMessage(), e);
				iRet = new IntegrationReturn(false, e.getLocalizedMessage());
			}
			return iRet;
		};

		return executor.submit(task);
	}

	public IntegrationReturn sendSync(String js, String method, String verb, String id) {
		JsonReader jsonReader = Json.createReader(new StringReader(js));
		JsonObject toSend = jsonReader.readObject();

		jsonReader.close();
		return sendSync(toSend, method, verb, id);
	}

	public IntegrationReturn sendSync(JsonObject jo, String method, String verb, String id) {
		Future<IntegrationReturn> future = sendAsync(jo, method, verb, id);

		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.trace(e.getLocalizedMessage(), e);
			return null;
		}
	}

	public IntegrationReturn sendSync(JsonObject jo, String method) {
		return sendSync(jo, method, "POST", null);
	}

	public IntegrationReturn sendSyncWithTimeout(JsonObject jo, String method, long timeout) throws TimeoutException {
		Future<IntegrationReturn> future = sendAsync(jo, method, "POST", null);

		try {
			return future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException e) {
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	public String getBaseURL() {
		return this.base_url;
	}
}
