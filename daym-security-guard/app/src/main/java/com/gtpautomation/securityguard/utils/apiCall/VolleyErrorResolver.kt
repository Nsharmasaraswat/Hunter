package com.gtpautomation.securityguard.utils.apiCall

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.gtpautomation.securityguard.utils.Constants
import org.json.JSONException
import org.json.JSONObject

class VolleyErrorResolver {
    companion object{
        fun getErrorMessage(error: VolleyError): CustomError? {
            return if (error is TimeoutError || error is NoConnectionError) {
                CustomError("Unable to connect to server", Constants.DEFAULT_ERROR_STATUS_CODE)
            } else if (error is AuthFailureError) {
                CustomError(
                    "Something went wrong please login to continue",
                    Constants.AUTH_ERROR_STATUS_CODE
                )
            } else if (error.networkResponse != null) {
                Log.e("VolleyErrorResolver", "getErrorMessage: "+error.networkResponse.data.contentToString(), )
                when (error.networkResponse.statusCode) {
                    409, 400 -> {
                        return try {
                            val badUrlResponse =
                                JSONObject(String(error.networkResponse.data)).getString("message")
                            CustomError(badUrlResponse, error.networkResponse.statusCode)
                        } catch (ignored: JSONException) {
                            CustomError("Unknown Error Occurred", 1)
                        }
                    }
                    401 -> CustomError(
                        "Something went wrong please login to continue",
                        Constants.AUTH_ERROR_STATUS_CODE
                    )
                    404 -> CustomError(
                        "Unable to connect to server",
                        error.networkResponse.statusCode
                    )
                    413 -> CustomError("File size too large", 413)
                    500 -> {
                        try {
                            val five_hundred_error =
                                JSONObject(String(error.networkResponse.data)).getString("message")
                            //Log.e("error_500",five_hundred_error);
                            return CustomError(five_hundred_error, error.networkResponse.statusCode)
                        } catch (ignored: JSONException) {
                        }
                        CustomError(
                            "Some error occurred, please try again later",
                            error.networkResponse.statusCode
                        )
                    }
                    else -> CustomError(
                        "Some error occurred, please try again later",
                        error.networkResponse.statusCode
                    )
                }
            } else CustomError("Unknown Error Occurred", 1)
        }

        class CustomError {
            var message: String? = null
            var statusCode = 0

            constructor() {}
            constructor(message: String?, statusCode: Int) {
                this.message = message
                this.statusCode = statusCode
            }
        }
    }
}