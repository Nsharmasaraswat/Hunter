package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.Document;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface DocumentAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.PROCESS + "document/bytypestatus/{docModelMeta}/{status}")
    Call<List<Document>> listByModelAndStatus(@Path("docModelMeta") String docModelMeta, @Path("status") String status);

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.CUSTOM + "document/productionOrder/{lineId}")
    Call<Document> getRunningPO(@Path("lineId") String line);
}
