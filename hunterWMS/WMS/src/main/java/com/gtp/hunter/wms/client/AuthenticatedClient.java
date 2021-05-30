package com.gtp.hunter.wms.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.structure.json.AddressDeserializer;
import com.gtp.hunter.structure.json.ProductDeserializer;
import com.gtp.hunter.wms.api.HunterURL;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.Product;

import java.util.Observable;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public abstract class AuthenticatedClient extends Observable {
    private static final long PING_INTERVAL = 30000;
    private static final long CONNECT_TIMEOUT = 90000;
    private static final long READ_TIMEOUT = 90000;
    private static final long WRITE_TIMEOUT = 90000;
    private static final boolean RETRY_OK_HTTP = false;

    Retrofit retrofit() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapter(Address.class, new AddressDeserializer())
                .registerTypeAdapter(Product.class, new ProductDeserializer())
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(PING_INTERVAL, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(RETRY_OK_HTTP)
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + HunterMobileWMS.getToken())
                            .build();
                    return chain.proceed(newRequest);
                }).build();


        return new Retrofit.Builder()
                .client(client)
                .baseUrl(HunterURL.BASE)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
