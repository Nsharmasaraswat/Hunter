package com.gtpautomation.securityguard.utils.apiCall

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.gtpautomation.securityguard.utils.apiCall.VolleySingleton.Companion.getInstance
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.*

class ApiCall {
    companion object{
        fun connect(
            context: Context?,
            method: Int,
            url: String?,
            body: JSONObject?,
            params: Map<String?, String?>?,
            headers: Map<String?, String?>?,
            onSuccessListener: OnSuccessListener,
            onFailureListener: OnFailureListener
        ): StringRequest? {
            val stringRequest: StringRequest = object : StringRequest(
                method,
                url,
                Response.Listener { response: String? ->
                    onSuccessListener.onSuccess(
                        response
                    )
                },
                Response.ErrorListener { error: VolleyError? ->
                    onFailureListener.onFailure(
                        error
                    )
                }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String?, String?> {
                    return params ?: super.getParams()
                }

                override fun getBody(): ByteArray? {
                    return body?.toString()?.toByteArray(StandardCharsets.UTF_8)
                }

                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String?, String?> {
                    return headers ?: super.getHeaders()
                }
            }
            Objects.requireNonNull(getInstance(context!!))
                ?.addToRequestQueue(stringRequest, context)
            stringRequest.retryPolicy =
                DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            return stringRequest
        }
    }
    interface OnSuccessListener {
        fun onSuccess(response: String?)
    }

    interface OnFailureListener {
        fun onFailure(error: VolleyError?)
    }
}