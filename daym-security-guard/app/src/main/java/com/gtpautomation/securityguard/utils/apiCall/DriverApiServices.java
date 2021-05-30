package com.gtpautomation.securityguard.utils.apiCall;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.gtpautomation.securityguard.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sunil Kumar on 05-04-2021 07:44 PM.
 */
public class DriverApiServices {
    public static void getDriverUserType(Context context, OnDriverSuccessListener onDriverSuccessListener, OnDriverFailureListener onDriverFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);

            ApiCall.Companion.connect(context,
                    Request.Method.GET,
                    Constants.USER_TYPE+"?role=6",
                    null, null,
                    header,
                    onDriverSuccessListener::onSuccess, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onDriverFailureListener.onFailure(customError);
                    });
        }, () -> onDriverFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }
    public static void getDriverUserFields(Context context,String userType, OnDriverSuccessListener onDriverSuccessListener, OnDriverFailureListener onDriverFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);

            ApiCall.Companion.connect(context,
                    Request.Method.GET,
                    Constants.USER_FIELDS+"?userType="+userType,
                    null, null,
                    header,
                    onDriverSuccessListener::onSuccess, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onDriverFailureListener.onFailure(customError);
                    });
        }, () -> onDriverFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }
    public static void createDriver(Context context, JSONObject body, OnDriverSuccessListener onDriverSuccessListener, OnDriverFailureListener onDriverFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);
            Log.e("TAG", "createDriver: "+Constants.USER+""+ body.toString());
            ApiCall.Companion.connect(context,
                    Request.Method.POST,
                    Constants.USER,
                    body, null,
                    header,
                    onDriverSuccessListener::onSuccess, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onDriverFailureListener.onFailure(customError);
                    });
        }, () -> onDriverFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }
    public static void addDriverToAppointment(Context context,String appointmentId, String driverId, OnDriverSuccessListener onDriverSuccessListener, OnDriverFailureListener onDriverFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);
            JSONObject body = new JSONObject();
            try {
                body.put("driver", driverId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ApiCall.Companion.connect(context,
                    Request.Method.PATCH,
                    Constants.APPOINTMENT_URL + "/"+appointmentId,
                    body, null,
                    header,
                    onDriverSuccessListener::onSuccess, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onDriverFailureListener.onFailure(customError);
                    });
        }, () -> onDriverFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }

    public interface OnDriverSuccessListener{
        void onSuccess(String response);
    }
    public interface OnDriverFailureListener{
        void onFailure(VolleyErrorResolver.Companion.CustomError error);
    }
}
