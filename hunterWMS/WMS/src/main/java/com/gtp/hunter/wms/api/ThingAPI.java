package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.Thing;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface ThingAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "thing/bytagid/{tagId}")
    Call<Thing> findByTagId(@Path("tagId") String tagId);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "thing/asyncListByDocument/{documentId}")
    Call<List<Thing>> listByDocument(@Path("documentId") String docId);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "thing/byproductid/{productId}")
    Call<List<Thing>> listByProduct(@Path("productId") String prdId);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "thing/quickByProduct/{productId}")
    Call<List<Thing>> listQuickByProduct(@Path("productId") String prdId);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "thing/byproductidandnotstatus/{productId}/{status}")
    Call<List<Thing>> listByProductIdAndNotStatus(@Path("productId") String prdId, @Path("status") String status);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "thing/byaddress/{addressId}")
    Call<List<Thing>> listByAddress(@Path("addressId") String prdId);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "thing/byaddresschildren/{addressId}")
    Call<List<Thing>> listByAddressChildren(@Path("addressId") String prdId);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @DELETE(HunterURL.PROCESS + "thing/remove/{id}")
    Call<Thing> removeById(@Path("tagId") String id);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "thing/parent/{id}")
    Call<Thing> findParent(@Path("id") String id);
}
