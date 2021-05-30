package com.gtpautomation.driver.api_services

import android.content.Context
import com.android.volley.Request
import com.google.android.gms.maps.model.LatLng
import com.gtpautomation.driver.data_models.DriverSession
import com.gtpautomation.driver.utils.ApiRoutes
import com.gtpautomation.driver.utils.SharedPreferenceHelper
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * Created by Sunil Kumar on 23-12-2020 07:37 PM.
 */
class UpdateLocationHelper {
    companion object {
        fun updateLocation(context: Context, latLng: LatLng, onSuccessListener: OnSuccessListener, onFailureListener: OnFailureListener) {
            val driverSession: DriverSession? = SharedPreferenceHelper.getDriverSession(context)
            val header: MutableMap<String, String> = HashMap()
            header["Authorization"] = driverSession!!.accessToken
            val coordinate: JSONArray = JSONArray()
            coordinate.put(latLng.longitude)
            coordinate.put(latLng.latitude)

            val body: JSONObject = JSONObject()
            body.put("coordinates", coordinate)

            ApiCall.connect(
                    context, Request.Method.PATCH, ApiRoutes.driverSession + "/${driverSession.sessionId}", body,
                    null, header, { _ ->
                onSuccessListener.onSuccess()
            },
                    { error ->
                        val customError: VolleyErrorResolver.CustomError =
                                VolleyErrorResolver.getErrorMessage(error)
                        onFailureListener.onFailure(customError.message)
                    }
            )
        }
    }

    interface OnSuccessListener {
        fun onSuccess()
    }

    interface OnFailureListener {
        fun onFailure(error: String)
    }
}