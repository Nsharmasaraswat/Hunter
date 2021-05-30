package com.gtpautomation.driver.api_services

import android.content.Context
import com.android.volley.Request
import com.gtpautomation.driver.R
import com.gtpautomation.driver.data_models.DriverSession
import com.gtpautomation.driver.utils.ApiRoutes
import com.gtpautomation.driver.utils.SharedPreferenceHelper
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * Created by Sunil Kumar on 23-12-2020 11:21 PM.
 */
class NotificationService {
    companion object {
        public fun sendNotificationTOSecurityGuard(
                context: Context, successListener: SuccessListener,
                failureListener: FailureListener,
        ) {
            val driverSession: DriverSession? = SharedPreferenceHelper.getDriverSession(context)
            val securityGuardId: String = SharedPreferenceHelper.getSecurityGuardId(context)
            val users = JSONArray()
            users.put(securityGuardId)

            val header: MutableMap<String, String> = HashMap()
            header["Authorization"] = driverSession!!.accessToken

            val body: JSONObject = JSONObject()
            body.put("text", String.format(context.getString(R.string.securityNotification), driverSession.user.name, driverSession.appointment.id))
            body.put("users", users)

            ApiCall.connect(
                    context,
                    Request.Method.POST,
                    ApiRoutes.notification,
                    body,
                    null,
                    header,
                    {
                        successListener.onSuccess(it)
                    },
                    { error ->
                        val customError: VolleyErrorResolver.CustomError =
                                VolleyErrorResolver.getErrorMessage(
                                        error
                                )
                        failureListener.onFailure(customError)
                    }
            )
        }
    }


    interface SuccessListener {
        fun onSuccess(response: String)
    }

    interface FailureListener {
        fun onFailure(customError: VolleyErrorResolver.CustomError)
    }
}