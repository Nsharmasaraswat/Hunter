package com.gtpautomation.securityguard.ui.todays_appointments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pojos.appointment.AppointmentResponse
import com.gtpautomation.securityguard.ui.todays_appointments.adapter.TodaysAppointmentsAdapter
import com.gtpautomation.securityguard.utils.apiCall.TodaysAppointmentsHelper
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TodaysAppointmentsFragment : Fragment()  {

    private var driverAddReceiver: BroadcastReceiver?=null
    private lateinit var progressLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var appointmentRecycler: RecyclerView
    private lateinit var tvError: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var retryBtn: Button
    private var todayInMillis: Long = 0
    private var tomorrowInMillis: Long = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_todays_appointments, container, false)
        initViews(root)
        setUpReceivers()
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(driverAddReceiver)
    }
    private fun setUpReceivers() {
        driverAddReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                setUpRecyclerView()
            }
        }
        context?.registerReceiver(driverAddReceiver, IntentFilter("driver-add-receiver"))

    }

        private fun initViews(root: View) {
        progressLayout = root.findViewById(R.id.circular_progress_layout)
        errorLayout = root.findViewById(R.id.error_layout)
        tvError = root.findViewById(R.id.error_text_view)
        appointmentRecycler = root.findViewById(R.id.todays_appointment_recycler)
        swipeRefreshLayout = root.findViewById(R.id.todays_appointment_swipe)
        retryBtn = root.findViewById(R.id.retry_button)
        swipeRefreshLayout.setOnRefreshListener {
            setUpRecyclerView()
            Handler(Looper.getMainLooper()).postDelayed({
                swipeRefreshLayout.isRefreshing = false
            }, 2000)
        }
        setUpRecyclerView()
        retryBtn.setOnClickListener {
            setUpRecyclerView()
        }
    }

    private fun setUpRecyclerView(){
        appointmentRecycler.visibility = View.GONE
        errorLayout.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
        getDatesInMillis()
        TodaysAppointmentsHelper.getAllTodaysAppointments(
                context,
                todayInMillis,
                tomorrowInMillis,
                { appointments ->
                    appointmentRecycler.visibility = View.VISIBLE
                    errorLayout.visibility = View.GONE
                    progressLayout.visibility = View.GONE
                    val appointmentsArray: Array<AppointmentResponse> = appointments.toTypedArray()
                    if (appointmentsArray.isEmpty()) {
                        manageError("No more appointments for today")
                    } else {
                        val adapter = TodaysAppointmentsAdapter(context, appointmentsArray)
                        appointmentRecycler.setHasFixedSize(true)
                        appointmentRecycler.layoutManager = LinearLayoutManager(context)
                        appointmentRecycler.adapter = adapter
                    }
                },
                { error ->
                    error.message?.let { manageError(it) }
                })
    }

    private fun manageError(errorMessage: String){
        appointmentRecycler.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        progressLayout.visibility = View.GONE
        tvError.text = errorMessage
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDatesInMillis(){
        val pattern = "dd-M-yyyy 00:00:00"
        val df: DateFormat = SimpleDateFormat(pattern)
        val instance = Calendar.getInstance()
//        instance.add(Calendar.DAY_OF_YEAR, 1)

        val today = instance.time
        val todayAsString = df.format(today)

        val gc = GregorianCalendar()
        gc.add(Calendar.DATE, 1)

        val tomorrow = gc.time
        val tomorrowAsString = df.format(tomorrow)

        val sdf = SimpleDateFormat("dd-M-yyyy HH:mm:ss")
        try {
            val currentDate = sdf.parse(todayAsString)
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            todayInMillis = calendar.timeInMillis

            val tmrwDate = sdf.parse(tomorrowAsString)
            val calendar2 = Calendar.getInstance()
            calendar2.time = tmrwDate
            tomorrowInMillis = calendar2.timeInMillis
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
}