package com.gtpautomation.securityguard.utils.apiCall;

import android.content.Context;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gtpautomation.securityguard.pojos.appointment_details.AppointmentDetails;
import com.gtpautomation.securityguard.pojos.truck.Truck;
import com.gtpautomation.securityguard.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentDetailsHelper {
    public static void getAppointmentDetails(Context context, String appointmentId, OnAppointmentDetailsFetchSuccessListener onAppointmentsFetchSuccessListener, OnAppointmentDetailsFetchFailureListener onAppointmentsFetchFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);
            ApiCall.Companion.connect(context,
                    Request.Method.GET,
                    Constants.APPOINTMENT_URL + "/"+appointmentId+"?$populate=user&$populate=driver&$populate=supplier&$populate=entranceGate&$populate=exitGate&$populate=truck",
                    null, null,
                    header,
                    response -> {
                        AppointmentDetails appointmentDetails = new Gson().fromJson(response, new TypeToken<AppointmentDetails>() {
                        }.getType());
                        onAppointmentsFetchSuccessListener.onSuccess(appointmentDetails);
                    }, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        assert customError != null;
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onAppointmentsFetchFailureListener.onFailure(customError);
                    });
        }, () -> onAppointmentsFetchFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }

    public static void acceptDriver(Context context, String appointmentId, OnAppointmentDetailsFetchSuccessListener onAppointmentsFetchSuccessListener, OnAppointmentDetailsFetchFailureListener onAppointmentsFetchFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            JSONObject body = new JSONObject();
            try {
                body.put("status", 5);
            } catch (JSONException ignored) { }
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);
            ApiCall.Companion.connect(context,
                    Request.Method.PATCH,
                    Constants.APPOINTMENT_URL + "/"+appointmentId+"?$populate=user&$populate=driver&$populate=supplier&$populate=entranceGate&$populate=exitGate&$populate=truck",
                    body, null,
                    header,
                    response -> {
                        AppointmentDetails appointmentDetails = new Gson().fromJson(response, new TypeToken<AppointmentDetails>() {
                        }.getType());
                        onAppointmentsFetchSuccessListener.onSuccess(appointmentDetails);
                    }, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onAppointmentsFetchFailureListener.onFailure(customError);
                    });
        }, () -> onAppointmentsFetchFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }

    public static void rejectDriver(Context context, String appointmentId, OnAppointmentDetailsFetchSuccessListener onAppointmentsFetchSuccessListener, OnAppointmentDetailsFetchFailureListener onAppointmentsFetchFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            JSONObject body = new JSONObject();
            try {
                body.put("status", 6);
            } catch (JSONException ignored) { }
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);
            ApiCall.Companion.connect(context,
                    Request.Method.PATCH,
                    Constants.APPOINTMENT_URL + "/"+appointmentId+"?$populate=user&$populate=driver&$populate=supplier&$populate=entranceGate&$populate=exitGate&$populate=truck",
                    body, null,
                    header,
                    response -> {
                        AppointmentDetails appointmentDetails = new Gson().fromJson(response, new TypeToken<AppointmentDetails>() {
                        }.getType());
                        onAppointmentsFetchSuccessListener.onSuccess(appointmentDetails);
                    }, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onAppointmentsFetchFailureListener.onFailure(customError);
                    });
        }, () -> onAppointmentsFetchFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }

    public interface OnAppointmentDetailsFetchSuccessListener {
        void onSuccess(AppointmentDetails appointmentDetails);
    }
    public interface OnAppointmentDetailsFetchFailureListener {
        void onFailure(VolleyErrorResolver.Companion.CustomError error);
    }
}
