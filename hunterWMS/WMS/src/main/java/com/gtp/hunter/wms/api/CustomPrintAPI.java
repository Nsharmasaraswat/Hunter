package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.PrintPayload;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CustomPrintAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @POST(HunterURL.CUSTOM + "print/sugar")
    Call<IntegrationReturn> printSugar(@Body PrintPayload payload);
}
