package com.gtpautomation.driver.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.gtpautomation.driver.R
import com.gtpautomation.driver.api_services.AuthenticationService.Companion.startDriverService
import com.gtpautomation.driver.api_services.AuthenticationService.SessionFailureListener
import com.gtpautomation.driver.api_services.AuthenticationService.SessionSuccessListener
import com.gtpautomation.driver.api_services.VolleyErrorResolver.CustomError
import com.gtpautomation.driver.data_models.DriverSession
import com.gtpautomation.driver.pages.appointment_details.AppointmentDetailsActivity
import com.gtpautomation.driver.utils.SharedPreferenceHelper
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private val REQUEST_CAMERA_PERMISSION = 201
    private val REQUEST_LOCATION_PERMISSION = 202
    private lateinit var barcodeText: TextView
    private lateinit var loaderView: LinearLayout
    private var scannerView: ZXingScannerView? = null
    private var scanResult: JSONObject? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Driver)
        setContentView(R.layout.activity_main)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        if (SharedPreferenceHelper.getDriverSession(applicationContext) != null) {
            val intent = Intent(this@MainActivity, AppointmentDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initViews() {
        barcodeText = findViewById(R.id.barcodeText)
        loaderView = findViewById(R.id.main_loader)
        scannerView = findViewById(R.id.scanner_view)
        hideLoader()
    }

    private fun setupScannerView() {
        try {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                scannerView?.apply {
                    setAutoFocus(true)
                    setFormats(listOf(BarcodeFormat.QR_CODE))
                    setResultHandler(this@MainActivity)
                    startCamera()
                }
            } else {
                ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun hideLoader() {
        loaderView.visibility = View.INVISIBLE
    }

    private fun showLoader() {
        loaderView.visibility = View.VISIBLE
    }

    //    private fun initTheme() {
//        when (AppCompatDelegate.getDefaultNightMode()) {
//            AppCompatDelegate.MODE_NIGHT_YES -> {
//                barcodeText.setTextColor(resources.getColor(R.color.white,theme))
//                findViewById<TextView>(R.id.title).setTextColor(resources.getColor(R.color.white,theme))
//            }
//            AppCompatDelegate.MODE_NIGHT_NO -> {
//                barcodeText.setTextColor(resources.getColor(R.color.black,theme))
//                findViewById<TextView>(R.id.title).setTextColor(resources.getColor(R.color.black,theme))
//            }
//        }
//    }

    override fun handleResult(rawResult: Result?) {
        Log.e("SCAN_RESULT", rawResult!!.text)
        try {
            scanResult = JSONObject(rawResult.text)
            requestLocationPermissions()
        } catch (e: Exception) {
            Log.e("SCAN_RESULT", e.message.toString())
            Toast.makeText(
                    applicationContext,
                    getString(R.string.unable_to_scan_qr),
                    Toast.LENGTH_SHORT
            ).show()
            setupScannerView()
        }
    }

    private fun requestLocationPermissions() {
        if ((ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                        != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                        != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ), REQUEST_LOCATION_PERMISSION
            )
        } else {
            showLoader()
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                this
        )
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
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
                    val coordinates = JSONArray()
                    coordinates.put(location.longitude)
                    coordinates.put(location.latitude)
                    mFusedLocationClient.removeLocationUpdates(this)
                    SharedPreferenceHelper.setGateCoordinates(
                        this@MainActivity,
                        location.longitude,
                        location.latitude
                    )

                    requestDriverSession(
                        scanResult!!.getString("appointment"),
                        scanResult!!.getString("driverId"),
                        scanResult!!.getString(
                            "securityGuardId"
                        ),
                        scanResult!!.getString("entranceGate"),
                        scanResult!!.getString("truck"),
                        scanResult!!.getString("exitGate"),
                        coordinates
                    )

                }
            }, Looper.myLooper()
        )
    }

    private fun requestDriverSession(
        appointment: String,
        driverId: String,
        securityGuardId: String,
        entranceGate: String,
        truck: String,
        exitGate: String,
        coordinates: JSONArray,
    ) {
        startDriverService(applicationContext,
            appointment,
            driverId, securityGuardId,
            entranceGate, truck, exitGate,
            coordinates,
            object : SessionSuccessListener {
                override fun onSuccess(driverSession: DriverSession) {
                    hideLoader()

                    val intent = Intent(this@MainActivity, AppointmentDetailsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }, object : SessionFailureListener {
                override fun onFailure(customError: CustomError) {
                    Log.e("LOCATION_UPDATE", customError.message)

                    Toast.makeText(applicationContext, customError.message, Toast.LENGTH_SHORT)
                        .show()
                    hideLoader()
                    setupScannerView()
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                            applicationContext,
                            getString(R.string.allow_camera_permission),
                            Toast.LENGTH_SHORT
                    ).show()
                    return
                } else {
                    setupScannerView()
                    scannerView?.setResultHandler(this)
                    scannerView?.startCamera()
                }
            } else {
                Toast.makeText(
                        applicationContext,
                        getString(R.string.allow_camera_permission),
                        Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                            applicationContext,
                            getString(R.string.allow_location_permission),
                            Toast.LENGTH_SHORT
                    ).show()
                    return
                } else {
                    showLoader()
                    getCurrentLocation()
                }
            } else {
                Toast.makeText(
                        applicationContext,
                        getString(R.string.allow_location_permission),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        scannerView?.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        setupScannerView()
    }
}