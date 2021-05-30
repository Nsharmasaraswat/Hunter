package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.Auth;
import com.gtp.hunter.wms.model.PreAuth;
import com.gtp.hunter.wms.model.Validate;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthAPI {

    @GET(HunterURL.CORE + "auth/preauth/{login}")
    Call<PreAuth> preAuth(@Path("login") String login);

    @POST(HunterURL.CORE + "auth/validate/{login}")
    Call<Validate> validate(@Path("login") String login, @Body Auth body);
}