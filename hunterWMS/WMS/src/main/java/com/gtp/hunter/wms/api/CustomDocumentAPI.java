package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.IntegrationReturn;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CustomDocumentAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @POST(HunterURL.CUSTOM + "document")
    Call<IntegrationReturn> postDocument(@Body AGLDocument doc);
}
