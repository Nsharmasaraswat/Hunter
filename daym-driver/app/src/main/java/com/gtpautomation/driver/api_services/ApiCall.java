package com.gtpautomation.driver.api_services;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiCall {
    public static StringRequest connect(Context context,
                                        int method,
                                        String url,
                                        final JSONObject body,
                                        final Map<String, String> params,
                                        final Map<String, String> headers,
                                        final OnSuccessListener onSuccessListener,
                                        final OnFailureListener onFailureListener
    ) {
        StringRequest stringRequest = new StringRequest(
                method,
                url,
                onSuccessListener::onSuccess,
                onFailureListener::onFailure
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params == null ? super.getParams() : params;
            }

            @Override
            public byte[] getBody() {
                return body == null ? null : body.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers == null ? super.getHeaders() : headers;
            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                9000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.Companion.getInstance(context).addToRequestQueue(stringRequest, context);
        return stringRequest;
    }

    public interface OnSuccessListener {
        void onSuccess(String response);
    }

    public interface OnFailureListener {
        void onFailure(VolleyError error);
    }
}
