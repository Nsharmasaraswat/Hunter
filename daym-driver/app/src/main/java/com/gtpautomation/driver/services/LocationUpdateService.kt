package com.gtpautomation.driver.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.gtpautomation.driver.api_services.NotificationService
import com.gtpautomation.driver.api_services.UpdateLocationHelper
import com.gtpautomation.driver.api_services.VolleyErrorResolver
import com.gtpautomation.driver.utils.SharedPreferenceHelper
import org.locationtech.jts.io.WKTReader


/**
 * Created by Sunil Kumar on 23-12-2020 07:00 PM.
 */
class LocationUpdateService : BroadcastReceiver() {
    private fun updateLocation(context: Context) {
        val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                context
        )
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mFusedLocationClient.requestLocationUpdates(
            locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.e("LOCATION_UPDATE", "$lat-$lon")

                    SharedPreferenceHelper.setCurrentCoordinates(context, lon, lat)

                    sendNotificationToSecurityGuard(location, context)

                    mFusedLocationClient.removeLocationUpdates(this)
                    UpdateLocationHelper.updateLocation(
                        context,
                        LatLng(lat, lon),
                        object : UpdateLocationHelper.OnSuccessListener {
                            override fun onSuccess() {}
                        },
                        object : UpdateLocationHelper.OnFailureListener {
                            override fun onFailure(error: String) {
                                Log.e("LOCATION_UPDATE", error)
                            }
                        })
                }
            }, Looper.myLooper()
        )
    }

    private fun sendNotificationToSecurityGuard(location: Location, context: Context) {

        val driverSession = SharedPreferenceHelper.getDriverSession(context)
        val reader = WKTReader()
        try {
            val gateGeometry = reader.read(driverSession!!.appointment.exitGate.wkt)

            val value =
                gateGeometry.contains(reader.read("LINESTRING(${location.latitude} ${location.longitude},${location.latitude} ${location.longitude})"))
            Log.e("LOCATION_UPDATE", "IS INSIDE $value sendNotificationToSecurityGuard")
            if (value) {

                NotificationService.sendNotificationTOSecurityGuard(
                    context,
                    object : NotificationService.SuccessListener {
                        override fun onSuccess(response: String) {

                        }
                    },
                    object : NotificationService.FailureListener {
                        override fun onFailure(customError: VolleyErrorResolver.CustomError) {

                        }
                    })
            }
        } catch (e: java.lang.Exception) {
            Log.e("LOCATION_UPDATE", "Error $e")
            e.printStackTrace()
        }
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        updateLocation(p0!!)
    }
}