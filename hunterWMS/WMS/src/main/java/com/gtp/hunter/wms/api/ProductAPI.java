package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface ProductAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "product/all")
    Call<List<Product>> listAll();

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "product/stripFields")
    Call<List<Product>> listAllNoField();

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "product/fromupdated/{updated}")
    @Streaming
    Call<List<Product>> listFrom(@Path("updated") String updatedAt);
}
