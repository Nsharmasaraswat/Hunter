package com.gtpautomation.driver.utils

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.gtpautomation.driver.data_models.DriverSession
import org.json.JSONArray
import org.json.JSONException

/**
 * Created by Sunil Kumar on 11-12-2020 01:10 PM.
 */
class SharedPreferenceHelper {
    companion object {
        const val sharedPreferencesKey = "gtp_auto_driver"
        const val driverSessionKey = "driverSession"
        const val securityGuardIdKey = "securityGuardIdKey"
        const val fcmIdKey = "fcmIdKey"
        const val coordinatesKey = "coordinates"
        const val currentCoordinatesKey = "currentCoordinates"

        fun getDriverSession(
                context: Context
        ): DriverSession? {
            val sharedPreferences = context.getSharedPreferences(
                    sharedPreferencesKey,
                    Context.MODE_PRIVATE
            )
            val response = sharedPreferences.getString(driverSessionKey, "")
            return if (response != null && response.isNotEmpty()) {
                val gson = Gson()
                gson.fromJson(response, DriverSession::class.java)
            } else null
        }

        fun setDriverSession(
                context: Context, driverSession: DriverSession
        ) {
            val gson = Gson()
            val sharedPreferences =
                    context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(driverSessionKey, gson.toJson(driverSession))
            editor.apply()
        }

        fun clearPref(context: Context) {
            val sharedPreferences =
                    context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }

        fun setSecurityGuardId(context: Context, id: String) {
            val sharedPreferences =
                    context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(securityGuardIdKey, id)
            editor.apply()
        }

        fun getSecurityGuardId(context: Context): String {
            val sharedPreferences = context.getSharedPreferences(
                    sharedPreferencesKey,
                    Context.MODE_PRIVATE
            )
            val id = sharedPreferences.getString(securityGuardIdKey, "")
            return id ?: ""
        }

        fun setFcmId(context: Context, fcmId: String) {
            val sharedPreferences =
                    context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(fcmIdKey, fcmId)
            editor.apply()
        }

        fun getFcmId(context: Context): String {
            val sharedPreferences = context.getSharedPreferences(
                    sharedPreferencesKey,
                    Context.MODE_PRIVATE
            )
            val id = sharedPreferences.getString(fcmIdKey, "")
            return id ?: ""
        }

        fun setGateCoordinates(context: Context, longitude: Double, latitude: Double) {
            val coordinates = JSONArray()
            try {
                coordinates.put(longitude)
                coordinates.put(latitude)
                val sharedPreferences = context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString(coordinatesKey, coordinates.toString())
                editor.apply()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        fun getGateCoordinates(context: Context): LatLng {
            val sharedPreferences = context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
            return try {
                val arr = JSONArray(sharedPreferences.getString(coordinatesKey, "[0,0]"))
                LatLng(arr.getDouble(1), arr.getDouble(0))
            } catch (e: JSONException) {
                LatLng(0.0, 0.0)
            }
        }

        fun setCurrentCoordinates(context: Context, longitude: Double, latitude: Double) {
            val coordinates = JSONArray()
            try {
                coordinates.put(longitude)
                coordinates.put(latitude)
                val sharedPreferences = context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString(currentCoordinatesKey, coordinates.toString())
                editor.apply()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        fun getCurrentCoordinates(context: Context): LatLng {
            val sharedPreferences = context.getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
            return try {
                val arr = JSONArray(sharedPreferences.getString(currentCoordinatesKey, "[0,0]"))
                LatLng(arr.getDouble(1), arr.getDouble(0))
            } catch (e: JSONException) {
                LatLng(0.0, 0.0)
            }
        }
    }
}