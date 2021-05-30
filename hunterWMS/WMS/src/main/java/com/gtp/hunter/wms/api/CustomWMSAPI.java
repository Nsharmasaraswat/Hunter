package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.Thing;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CustomWMSAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @DELETE(HunterURL.CUSTOM + "wms/removePallet/{thingId}")
    Call<IntegrationReturn> removeThing(@Path("thingId") UUID id);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @POST(HunterURL.CUSTOM + "wms/transportPallet/{origin_id}/{destination_id}/{quantity}")
    Call<IntegrationReturn> createTransport(@Path("origin_id") UUID originId, @Path("destination_id") UUID destId, @Path("quantity") int quantity);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @POST(HunterURL.CUSTOM + "wms/transportPallets/{destination_id}")
    Call<IntegrationReturn> createTransport(@Body List<UUID> thIdList, @Path("destination_id") UUID destId);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.CUSTOM + "wms/stkAddressList/{address_id}")
    Call<List<Thing>> loadAllocation(@Path("address_id") UUID id);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.CUSTOM + "wms/stkDockAllocation")
    Call<List<Thing>> listDockAllocation();
}
