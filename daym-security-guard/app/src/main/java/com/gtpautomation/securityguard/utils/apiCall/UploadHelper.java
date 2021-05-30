package com.gtpautomation.securityguard.utils.apiCall;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.gtpautomation.securityguard.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sunil Kumar on 05-04-2021 07:53 PM.
 */
class UploadHelper {
    public static void uploadFile(Context context, final byte[] buffer, OnUploadSuccessListener onUploadSuccessListener, OnUploadFailureListener onUploadFailureListener) {
        AccessTokenHelper.Companion.getAccessToken(context, accessToken -> {
            Map<String, String> header = new HashMap<>();
            header.put("Authorization", accessToken);
            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                    Constants.UPLOAD,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getBoolean("result")) {
                                onUploadSuccessListener.onSuccess(obj.getString("file"));
                            }
                        } catch (JSONException e) {
                            onUploadFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError(e.getMessage(), Constants.DEFAULT_ERROR_STATUS_CODE));
                        }
                    }, error -> {
                VolleyErrorResolver.Companion.CustomError customError = VolleyErrorResolver.Companion.getErrorMessage(error);
                assert customError != null;
                if (customError.getStatusCode() == Constants.AUTH_ERROR_STATUS_CODE) {
                    customError.setMessage("Something went wrong, Please login again to continue");
                }
                onUploadFailureListener.onFailure(customError);
            }) {

                @Override
                public Map<String, String> getHeaders() {
                    return header;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    long imageName = System.currentTimeMillis();
                    params.put("avatar", new DataPart(imageName + ".jpg", buffer));
                    return params;
                }
            };

            Volley.newRequestQueue(context).add(volleyMultipartRequest);
        }, () -> onUploadFailureListener.onFailure(new VolleyErrorResolver.Companion.CustomError("Something went wrong, please login to continue", Constants.AUTH_ERROR_STATUS_CODE)));
    }

    public interface OnUploadSuccessListener{
        void onSuccess(String response);
    }

    public interface OnUploadFailureListener{
        void onFailure(VolleyErrorResolver.Companion.CustomError error);
    }
}
