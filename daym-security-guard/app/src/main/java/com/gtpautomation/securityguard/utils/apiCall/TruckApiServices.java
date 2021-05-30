package com.gtpautomation.securityguard.utils.apiCall;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.google.gson.JsonArray;
import com.gtpautomation.securityguard.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sunil Kumar on 02-04-2021 08:59 PM.
 */
public class TruckApiServices {
    public static void getAllTrucks(Context context, String supplierId, OnTruckSuccessListener onTruckSuccessListener, OnTruckFailureListener onTruckFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);
            ApiCall.Companion.connect(context,
                    Request.Method.GET,
                    Constants.TRUCK_URL + "?supplier="+supplierId,
                    null, null,
                    header,
                    onTruckSuccessListener::onSuccess, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onTruckFailureListener.onFailure(customError);
                    });
        }, () -> onTruckFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }

    public static void addTruckToAppointment(Context context,String appointmentId, String truckId, OnTruckSuccessListener onTruckSuccessListener, OnTruckFailureListener onTruckFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);
            JSONObject body = new JSONObject();
            try {
                body.put("truck", truckId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ApiCall.Companion.connect(context,
                    Request.Method.PATCH,
                    Constants.APPOINTMENT_URL + "/"+appointmentId,
                    body, null,
                    header,
                    onTruckSuccessListener::onSuccess, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onTruckFailureListener.onFailure(customError);
                    });
        }, () -> onTruckFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }

    public static void geTruckFields(Context context, OnTruckSuccessListener onTruckSuccessListener, OnTruckFailureListener onTruckFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);

            ApiCall.Companion.connect(context,
                    Request.Method.GET,
                    Constants.TRUCK_FIELDS,
                    null, null,
                    header,
                    onTruckSuccessListener::onSuccess, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onTruckFailureListener.onFailure(customError);
                    });
        }, () -> onTruckFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));

    }

    public static void createTruck(Context context,JSONObject body, OnTruckSuccessListener onTruckSuccessListener, OnTruckFailureListener onTruckFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);

            ApiCall.Companion.connect(context,
                    Request.Method.POST,
                    Constants.TRUCK_URL,
                    body, null,
                    header,
                    onTruckSuccessListener::onSuccess, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onTruckFailureListener.onFailure(customError);
                    });
        }, () -> onTruckFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }

    public interface OnTruckSuccessListener{
        void onSuccess(String response);
    }
    public interface OnTruckFailureListener{
        void onFailure(VolleyErrorResolver.Companion.CustomError error);
    }
}
