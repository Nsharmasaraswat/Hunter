package com.gtpautomation.securityguard.utils.apiCall;

import android.content.Context;
import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gtpautomation.securityguard.pojos.appointment.AppointmentResponse;
import com.gtpautomation.securityguard.utils.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CompletedAppointmentsHelper {
    public static void getAllCompletedAppointments(Context context, long todayInMillis, long tomorrowInMillis, OnAppointmentsFetchSuccessListener onAppointmentsFetchSuccessListener, OnAppointmentsFetchFailureListener onAppointmentsFetchFailureListener){
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String,String> header = new HashMap<>();
            header.put("Authorization",accessToken);
            ApiCall.Companion.connect(context,
                    Request.Method.GET,
                    Constants.APPOINTMENT_URL + "?deliveryOn[$gte]=" + todayInMillis + "&deliveryOn[$lt]=" + tomorrowInMillis + "&status=8&$populate=supplier&$populate=user&$populate=driver&$populate=entranceGate&$populate=exitGate&$populate=truck&$sort[completedOn]=-1",
                    null, null,
                    header,
                    response -> {
                        ArrayList<AppointmentResponse> completedAppointments = new Gson().fromJson(response, new TypeToken<ArrayList<AppointmentResponse>>() {
                        }.getType());
                        onAppointmentsFetchSuccessListener.onSuccess(completedAppointments);
                    }, error -> {
                        VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                        if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                            customError.setMessage("Something went wrong, Please login again to continue");
                        }
                        onAppointmentsFetchFailureListener.onFailure(customError);
                    });
        }, () -> onAppointmentsFetchFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }
    public interface OnAppointmentsFetchSuccessListener {
        void onSuccess(ArrayList<AppointmentResponse>completedAppointments);
    }
    public interface OnAppointmentsFetchFailureListener {
        void onFailure(VolleyErrorResolver.Companion.CustomError error);
    }
}
