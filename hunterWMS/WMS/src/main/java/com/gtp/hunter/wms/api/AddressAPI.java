package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.Address;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface AddressAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "address/all")
    Call<List<Address>> listAll();

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "address/stripFields")
    Call<List<Address>> listAllNoField();

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "address/fromupdated/{updated}")
    Call<List<Address>> listFrom(@Path("updated") String updatedAt);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "address/bylocation/fromupdated/{locationid}/{updated}")
    @Streaming
    Call<List<Address>> listByLocationFrom(@Path("locationid") String locId, @Path("updated") String updatedAt);
}
