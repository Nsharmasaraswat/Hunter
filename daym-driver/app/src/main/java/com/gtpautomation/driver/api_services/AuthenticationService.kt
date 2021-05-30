package com.gtpautomation.driver.api_services

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.gtpautomation.driver.data_models.DriverSession
import com.gtpautomation.driver.utils.ApiRoutes
import com.gtpautomation.driver.utils.SharedPreferenceHelper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


/**
 * Created by Sunil Kumar on 11-12-2020 01:01 PM.
 */
class AuthenticationService {
    companion object {
        /**
         * START DRIVER SESSION
         * When qr code scanned successfully
         * **/
        public fun startDriverService(
            context: Context,
            appointment: String,
            driverId: String,
            securityGuardId: String,
            entranceGate: String,
            truck: String,
            exitGate: String,
            coordinates: JSONArray,
            sessionSuccessListener: SessionSuccessListener,
            sessionFailureListener: SessionFailureListener,
        ) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmId: String = task.result
                    val body: JSONObject = JSONObject()
                    body.put("appointment", appointment)
                    body.put("driverId", driverId)
                    body.put("fcmId", fcmId)
                    body.put("securityGuardId", securityGuardId)
                    body.put("entranceGate", entranceGate)
                    body.put("truck", truck)
                    body.put("exitGate", exitGate)
                    body.put("coordinates", coordinates)


                    ApiCall.connect(
                            context,
                            Request.Method.POST,
                            ApiRoutes.createDriverSession + "?\$populate=exitGate",
                            body,
                            null,
                            null,
                            { response ->
                                try {

                                    val gson = Gson()
                                    val driverSession: DriverSession = gson.fromJson<DriverSession>(
                                            response,
                                            DriverSession::class.java
                                    )
                                    SharedPreferenceHelper.setDriverSession(context, driverSession)
                                    SharedPreferenceHelper.setSecurityGuardId(context, securityGuardId)
                                    sessionSuccessListener.onSuccess(driverSession)
                                } catch (e: JSONException) {
                                    Log.e("LOCATION_UPDATE", e.toString())

                                    sessionFailureListener.onFailure(
                                            VolleyErrorResolver.CustomError(
                                                    e.message,
                                                    0
                                            )
                                    )
                                }
                            },
                            { error ->
                                val customError: VolleyErrorResolver.CustomError =
                                        VolleyErrorResolver.getErrorMessage(error)
                                sessionFailureListener.onFailure(customError)
                            }
                    )
                }

            }.addOnFailureListener { e ->
                sessionFailureListener.onFailure(
                        VolleyErrorResolver.CustomError(
                                e.message,
                                0
                        )
                )
            }

        }

        /**
         * END DRIVER SESSION
         * When orders completed
         * **/
        public fun endDriverService(
                context: Context, sessionEndSuccessListener: SessionEndSuccessListener,
                sessionFailureListener: SessionFailureListener,
        ) {
            val driverSession: DriverSession? = SharedPreferenceHelper.getDriverSession(context)
            val header: MutableMap<String, String> = HashMap()
            header["Authorization"] = driverSession!!.accessToken

            ApiCall.connect(
                    context,
                    Request.Method.DELETE,
                    ApiRoutes.driverSession + "/${driverSession.sessionId}",
                    null,
                    null,
                    header,
                    {
                        try {
                            sessionEndSuccessListener.onSuccess()
                        } catch (e: JSONException) {
                            sessionFailureListener.onFailure(
                                    VolleyErrorResolver.CustomError(
                                            e.message,
                                            0
                                    )
                            )
                        }
                    },
                    { error ->
                        val customError: VolleyErrorResolver.CustomError =
                                VolleyErrorResolver.getErrorMessage(
                                        error
                                )
                        sessionFailureListener.onFailure(customError)
                    }
            )
        }

    }

    interface SessionSuccessListener {
        fun onSuccess(driverSession: DriverSession)
    }

    interface SessionFailureListener {
        fun onFailure(customError: VolleyErrorResolver.CustomError)
    }

    interface SessionEndSuccessListener {
        fun onSuccess()
    }
}