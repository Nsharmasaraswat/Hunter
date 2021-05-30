package com.gtpautomation.driver.pages.appointment_details

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.gtpautomation.driver.R
import com.gtpautomation.driver.api_services.AppointmentService
import com.gtpautomation.driver.api_services.AuthenticationService
import com.gtpautomation.driver.api_services.VolleyErrorResolver
import com.gtpautomation.driver.data_models.AppointmentDetails
import com.gtpautomation.driver.data_models.DriverSession
import com.gtpautomation.driver.pages.MainActivity
import com.gtpautomation.driver.pages.MapsActivity
import com.gtpautomation.driver.pages.profile.ProfileActivity
import com.gtpautomation.driver.services.LocationUpdateService
import com.gtpautomation.driver.utils.SharedPreferenceHelper
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AppointmentDetailsActivity : AppCompatActivity() {
    private var driverSession: DriverSession? = null
    private var appointmentDetails: AppointmentDetails? = null
    private lateinit var loaderLin: LinearLayout
    private lateinit var loaderProgressBar: ProgressBar
    private lateinit var loaderText: TextView
    private lateinit var loaderButton: MaterialButton
    private lateinit var detailsLayout: ScrollView
    private lateinit var completedOrdersText: TextView
    private lateinit var deliveryOnText: TextView
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var statusLin: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var serviceIntent: Intent
    private lateinit var pendingIntent: PendingIntent
    private lateinit var manager: AlarmManager
    private lateinit var endSessionBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details)
        setTheme(R.style.Theme_Driver)
        initViews()

        driverSession = SharedPreferenceHelper.getDriverSession(applicationContext)
        if (driverSession != null) {
            fetchData()
            startService()
        }
    }

    private fun startService() {
        try {
            serviceIntent = Intent(this, LocationUpdateService::class.java)
            pendingIntent = PendingIntent.getBroadcast(this, 0, serviceIntent, 0)
            manager = getSystemService(ALARM_SERVICE) as AlarmManager
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (
                    60000).toLong(), pendingIntent)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        pendingIntent.cancel()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appointment_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile_menu -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        supportActionBar?.title = resources.getString(R.string.appointment_details)

        loaderLin = findViewById(R.id.details_loader_layout)
        loaderProgressBar = findViewById(R.id.loader_progress_bar)
        loaderText = findViewById(R.id.loader_text)
        loaderButton = findViewById(R.id.loader_button)
        detailsLayout = findViewById(R.id.appointment_details_layout)
        completedOrdersText = findViewById(R.id.appointment_completed_orders)
        deliveryOnText = findViewById(R.id.appointment_delivery_time)
        swipeRefreshLayout = findViewById(R.id.appointment_details_swipe_refresh)
        statusLin = findViewById(R.id.status_lin)
        statusText = findViewById(R.id.status_text)
        endSessionBtn = findViewById(R.id.end_session_btn)
        endSessionBtn.isEnabled = false
        ordersRecyclerView = findViewById(R.id.appointment_orders_list)
        ordersRecyclerView.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout.setColorSchemeResources(R.color.primary_500)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            fetchData()
        }
    }

    private fun showLoader() {
        loaderProgressBar.visibility = View.VISIBLE
        loaderText.visibility = View.VISIBLE
        loaderButton.visibility = View.GONE
        detailsLayout.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
        loaderText.text = resources.getString(R.string.please_wait)
        loaderLin.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        swipeRefreshLayout.visibility = View.VISIBLE
        detailsLayout.visibility = View.VISIBLE
        loaderLin.visibility = View.GONE
    }

    private fun showError(message: String) {
        swipeRefreshLayout.isRefreshing = false
        loaderProgressBar.visibility = View.GONE
        loaderText.visibility = View.VISIBLE
        loaderButton.visibility = View.VISIBLE
        detailsLayout.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
        loaderText.text = message
        loaderLin.visibility = View.VISIBLE
        loaderButton.setOnClickListener(View.OnClickListener {
            fetchData()
        })
    }

    override fun onStart() {
        super.onStart()
        driverSession = SharedPreferenceHelper.getDriverSession(applicationContext)
        if (driverSession == null) {
            val intent = Intent(this@AppointmentDetailsActivity, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun fetchData() {
        showLoader()
        AppointmentService.getAppointmentDetails(applicationContext, driverSession!!.appointment.id,
                object : AppointmentService.AppointmentFetchSuccessListener {
                    override fun onSuccess(appointmentDetails: AppointmentDetails) {
                        hideLoader()
                        swipeRefreshLayout.isRefreshing = false
                        this@AppointmentDetailsActivity.appointmentDetails = appointmentDetails
                        completedOrdersText.text = appointmentDetails.completedOrderCount.toString()
                        val input =
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        val output = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        setStatus(appointmentDetails.status)
                        setupEndSession()
                        try {
                            val d = input.parse(appointmentDetails.deliveryOn)
                            deliveryOnText.text = output.format(d)
                            ordersRecyclerView.adapter = AppointmentOrderAdapter(
                                    this@AppointmentDetailsActivity,
                                    appointmentDetails.orders
                            )
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                    }
                },
                object : AppointmentService.AppointmentFetchFailureListener {
                    override fun onFailure(customError: VolleyErrorResolver.CustomError) {
                        showError(customError.message)
                    }
                })
    }

    private fun setupEndSession() {
        val gateCoordinate = SharedPreferenceHelper.getGateCoordinates(this)
        val currentCoordinate = SharedPreferenceHelper.getCurrentCoordinates(this)
        if (distance(gateCoordinate.latitude, gateCoordinate.longitude, currentCoordinate.latitude, currentCoordinate.longitude) < 10) {
            endSessionBtn.isEnabled = true
            endSessionBtn.setOnClickListener {
                endSession(it)
            }
        } else {
            endSessionBtn.isEnabled = false
        }
    }

    private fun setStatus(status: Int) {
        /**
         * 0. initiated
         * 1. pending
         * 2. changed By Supplier
         * 3. confirmed By Supplier
         * 4. scheduled
         * 5. accepted By Security guard
         * 6. rejected By Security guard
         * 7. truck entered
         * 8. completed
         * -1. cancelled
         */
        when (status) {
            0 -> {
                statusLin.setBackgroundColor(Color.argb(80, 64, 18, 255))
                statusText.setTextColor(Color.argb(255, 64, 18, 255))
                statusText.text = resources.getString(R.string.order_initiated)
            }
            1 -> {
                statusLin.setBackgroundColor(Color.argb(80, 255, 193, 24))
                statusText.setTextColor(Color.argb(255, 255, 193, 24))
                statusText.text = resources.getString(R.string.order_pending)
            }
            2 -> {
                statusLin.setBackgroundColor(Color.argb(80, 102, 255, 219))
                statusText.setTextColor(Color.argb(255, 102, 255, 219))
                statusText.text = getString(R.string.order_changed_by_supplier)
            }
            3 -> {
                statusLin.setBackgroundColor(Color.argb(80, 255, 241, 38))
                statusText.setTextColor(Color.argb(255, 255, 241, 38))
                statusText.text = getString(R.string.order_confirmed)
            }
            4 -> {
                statusLin.setBackgroundColor(Color.argb(255, 145, 36, 254))
                statusText.setTextColor(Color.argb(255, 145, 36, 254))
                statusText.text = getString(R.string.order_scheduled)
            }
            5 -> {
                statusLin.setBackgroundColor(Color.argb(80, 120, 203, 175))
                statusText.setTextColor(Color.argb(255, 120, 203, 175))
                statusText.text = getString(R.string.accepted_by_security_guard)
            }
            6 -> {
                statusLin.setBackgroundColor(Color.argb(80, 201, 130, 9))
                statusText.setTextColor(Color.argb(255, 201, 130, 9))
                statusText.text = getString(R.string.rejected_by_security_guard)
            }
            7 -> {
                statusLin.setBackgroundColor(Color.argb(80, 100, 35, 254))
                statusText.setTextColor(Color.argb(255, 100, 35, 254))
                statusText.text = getString(R.string.truck_entered)
            }
            8 -> {
                statusLin.setBackgroundColor(Color.argb(80, 49, 255, 35))
                statusText.setTextColor(Color.argb(255, 49, 255, 35))
                statusText.text = getString(R.string.order_completed)
            }
            -1 -> {
                statusLin.setBackgroundColor(Color.argb(80, 255, 111, 0))
                statusText.setTextColor(Color.argb(255, 255, 111, 0))
                statusText.text = getString(R.string.order_cancelled)
            }
        }
    }

    fun viewMap(view: View) {
        val intent = Intent(this@AppointmentDetailsActivity, MapsActivity::class.java)
        intent.putExtra("appointment", Gson().toJson(appointmentDetails))
        startActivity(intent)
    }

    private fun endSession(view: View) {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.end_session_alert_title))
                .setMessage(getString(R.string.end_session_alert_message))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    view.visibility = View.INVISIBLE
                    AuthenticationService.endDriverService(applicationContext,
                            object : AuthenticationService.SessionEndSuccessListener {
                                override fun onSuccess() {
                                    Snackbar.make(view, getString(R.string.session_completed_message), Snackbar.LENGTH_LONG).setAction("Ok") {}.show()
                                    SharedPreferenceHelper.clearPref(applicationContext)
                                    val intent = Intent(this@AppointmentDetailsActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finishAffinity()
                                }
                            }, object : AuthenticationService.SessionFailureListener {
                        override fun onFailure(customError: VolleyErrorResolver.CustomError) {
                            Toast.makeText(applicationContext, customError.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                .setNegativeButton(getString(R.string.no), null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    private fun distance(lat_a: Double, lng_a: Double, lat_b: Double, lng_b: Double): Double {
        val earthRadius = 3958.75
        val latDiff = Math.toRadians((lat_b - lat_a).toDouble())
        val lngDiff = Math.toRadians((lng_b - lng_a).toDouble())
        val a = sin(latDiff / 2) * sin(latDiff / 2) +
                cos(Math.toRadians(lat_a.toDouble())) * cos(Math.toRadians(lat_b.toDouble())) *
                sin(lngDiff / 2) * sin(lngDiff / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c
        val meterConversion = 1609
        return (distance * meterConversion.toDouble()).toDouble()
    }
}