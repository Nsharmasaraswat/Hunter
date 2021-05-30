package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.CORE + "user/")
    Call<User> getLogged();

    @Headers({"Content-Type: text/plain; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @POST(HunterURL.CORE + "user/property/{property_key}/{property_value}")
    Call<Void> postProperty(@Path("property_key") String key, @Path("property_value") String value);

    @Headers({"Content-Type: text/plain; charset=UTF-8", "Accept: application/json; charset=UTF-8"})
    @PUT(HunterURL.CORE + "user/property/{property_key}/{property_value}")
    Call<Void> putProperty(@Path("property_key") String key, @Path("property_value") String value);
}
