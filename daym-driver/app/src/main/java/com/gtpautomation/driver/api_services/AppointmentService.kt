package com.gtpautomation.driver.api_services

import android.content.Context
import com.android.volley.Request
import com.google.gson.Gson
import com.gtpautomation.driver.data_models.AppointmentDetails
import com.gtpautomation.driver.data_models.DriverSession
import com.gtpautomation.driver.utils.ApiRoutes
import com.gtpautomation.driver.utils.SharedPreferenceHelper
import org.json.JSONException

/**
 * Created by Sunil Kumar on 11-12-2020 03:31 PM.
 */
class AppointmentService {
    companion object {
        /**
         * Get details of a appointment
         */
        public fun getAppointmentDetails(
            context: Context,
            appointmentId: String,
            appointmentFetchSuccessListener: AppointmentFetchSuccessListener,
            appointmentFetchFailureListener: AppointmentFetchFailureListener
        ) {
            val driverSession: DriverSession? = SharedPreferenceHelper.getDriverSession(context)
            val header: MutableMap<String, String> = HashMap()
            header["Authorization"] = driverSession!!.accessToken

            ApiCall.connect(
                context,
                Request.Method.GET,
                ApiRoutes.appointment + "/${driverSession.appointment!!.id}?\$populate=exitGate",
                null,
                null,
                header,
                {
                    try {
                        val gson = Gson()
                        val appointmentDetails: AppointmentDetails =
                            gson.fromJson<AppointmentDetails>(
                                it,
                                AppointmentDetails::class.java
                            )
                        appointmentFetchSuccessListener.onSuccess(appointmentDetails)
                    } catch (e: JSONException) {
                        appointmentFetchFailureListener.onFailure(
                            VolleyErrorResolver.CustomError(
                                e.message,
                                0
                            )
                        )
                    }
                },
                {
                    val customError: VolleyErrorResolver.CustomError =
                        VolleyErrorResolver.getErrorMessage(
                            it
                        )
                    appointmentFetchFailureListener.onFailure(customError)
                })
        }
    }

    interface AppointmentFetchSuccessListener {
        fun onSuccess(appointmentDetails: AppointmentDetails)
    }

    interface AppointmentFetchFailureListener {
        fun onFailure(customError: VolleyErrorResolver.CustomError)
    }

}