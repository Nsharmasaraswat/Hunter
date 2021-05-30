package com.gtp.hunter.wms.api;

import com.gtp.hunter.wms.model.Permission;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface PermissionAPI {

    @Headers({"Content-Type: application/json; charset=UTF-8","Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.CORE + "permission/user/{userId}")
    Call<List<Permission>> listUserPermissions(@Path("userId")String userId);

    @Headers({"Content-Type: application/json; charset=UTF-8","Accept: application/json; charset=UTF-8"})
    @GET(HunterURL.CORE + "user/permission")
    Call<List<Permission>> listPermissions();
}
