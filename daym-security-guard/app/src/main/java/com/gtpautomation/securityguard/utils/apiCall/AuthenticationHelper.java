package com.gtpautomation.securityguard.utils.apiCall;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.gtpautomation.securityguard.utils.Constants;
import com.gtpautomation.securityguard.utils.sharedPreferenceHelper.SharedPreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

public class AuthenticationHelper {
    public static void loginWithEmailAndPassword(final Context context, JSONObject body, final OnLoginSuccessListener onSuccessListener, final OnLoginFailureListener onFailureListener){
        ApiCall.Companion.connect(context,
                Request.Method.POST,
                Constants.AUTH_URL+"?$populate=gate",
                body,
                null,
                null,
                response -> {
            //Log.e("login",body.toString());
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        String accessToken = responseObject.getString("accessToken");
                        long time = Calendar.getInstance().getTimeInMillis();
                        AccessTokenHelper.Companion.setAccessToken(context,accessToken,time);
                        SharedPreferenceHelper.Companion.setUser(context, response);
                        onSuccessListener.onSuccess();
                    } catch (JSONException e) {
                        VolleyErrorResolver.Companion.CustomError error=new VolleyErrorResolver.Companion.CustomError(e.getMessage(),0);
                        onFailureListener.onFailure(error);
                    }
                }, error -> {
                    VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                    if(Objects.requireNonNull(customError).getStatusCode()==Constants.AUTH_ERROR_STATUS_CODE){
                        customError.setMessage("Invalid Username or Password");
                    }
                    onFailureListener.onFailure(customError);
                });
    }

    public interface OnLoginSuccessListener{
        void onSuccess();
    }
    public interface OnLoginFailureListener{
        void onFailure(VolleyErrorResolver.Companion.CustomError error);
    }
}
