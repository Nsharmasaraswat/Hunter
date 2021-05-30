package com.gtpautomation.securityguard.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.add_driver.AddDriverActivity
import com.gtpautomation.securityguard.pages.all_items.AllItemsActivity
import com.gtpautomation.securityguard.pages.search_truck.SearchTruckActivity
import com.gtpautomation.securityguard.pojos.appointment_details.AppointmentDetails
import com.gtpautomation.securityguard.pojos.driver.Driver
import com.gtpautomation.securityguard.pojos.order_items.Products
import com.gtpautomation.securityguard.pojos.truck.Truck
import com.gtpautomation.securityguard.utils.CustomTypefaceSpan
import com.gtpautomation.securityguard.utils.apiCall.AppointmentDetailsHelper
import com.gtpautomation.securityguard.utils.apiCall.DriverApiServices
import com.gtpautomation.securityguard.utils.apiCall.TruckApiServices
import com.gtpautomation.securityguard.utils.sharedPreferenceHelper.SharedPreferenceHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AppointmentDetailsActivity : AppCompatActivity() {

    private var appointmentDetails: AppointmentDetails? = null
    private lateinit var appointmentId: String
    private lateinit var toolbar: Toolbar
    private lateinit var tvAppointmentStatus: TextView
    private lateinit var tvOwnerName: TextView
    private lateinit var tvSupplierName: TextView
    private lateinit var tvDriverName: TextView
    private lateinit var tvGateNo: TextView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvOrderStatus1: TextView
    private lateinit var tvOrderStatus2: TextView
    private lateinit var tvProductName1: TextView
    private lateinit var tvProductCode1: TextView
    private lateinit var tvWarehouseName1: TextView
    private lateinit var tvDockName1: TextView
    private lateinit var tvProductName2: TextView
    private lateinit var tvProductCode2: TextView
    private lateinit var tvWarehouseName2: TextView
    private lateinit var tvDockName2: TextView
    private lateinit var rejectBtn: TextView
    private lateinit var acceptBtn: TextView
    private lateinit var generateQrCodeBtn: TextView
    private lateinit var orderRejectedLabel: TextView
    private lateinit var bottomActionsLayout: LinearLayout
    private lateinit var bottomLoaderLayout: LinearLayout
    private lateinit var loaderLayout: LinearLayout
    private lateinit var mainLayout: ScrollView
    private lateinit var errorLayout: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var retryBtn: Button
    private lateinit var orderCard2: CardView
    private lateinit var orderCompletedLabel: TextView
    private lateinit var viewAllBtn: Button
    private lateinit var thisDetails: AppointmentDetails
    private lateinit var products: ArrayList<Products>
    private var ticketUrl: String = ""

    private lateinit var truckNameTv :TextView
    private lateinit var truckLicenseTv :TextView
    private lateinit var truckDetailsLin :LinearLayout
    private lateinit var addTruckButton :MaterialButton

    private lateinit var driverDetailsLin :LinearLayout
    private lateinit var addDriverButton :MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_details)
        appointmentId = intent.getStringExtra("appointment").toString()
        initViews()
        setToolBar()
        getAppointmentDetailsData()
        rejectBtn.setOnClickListener {
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.are_you_sure))
                .setMessage(getString(R.string.reject_driver_alert_msg))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    rejectTheDriver()
                }
                .setNegativeButton(getString(R.string.no), null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
            val textView = dialog!!.findViewById<View>(android.R.id.message) as TextView
            val face = Typeface.createFromAsset(assets, "poppins.ttf")
            textView.typeface = face
        }
        acceptBtn.setOnClickListener {
            if(appointmentDetails!!.truck==null){
                Toast.makeText(applicationContext, getString(R.string.no_truck_found), Toast.LENGTH_LONG).show()
            }else if(appointmentDetails!!.driver==null){
                Toast.makeText(applicationContext, getString(R.string.no_driver_found), Toast.LENGTH_LONG).show()
            }else {
                val dialog: AlertDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.are_you_sure))
                    .setMessage(getString(R.string.accept_driver_alert_msg))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        acceptTheDriver()
                    }
                    .setNegativeButton(getString(R.string.no), null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                val textView = dialog!!.findViewById<View>(android.R.id.message) as TextView
                val face = Typeface.createFromAsset(assets, "poppins.ttf")
                textView.typeface = face
            }
        }
        generateQrCodeBtn.setOnClickListener {
            val intent = Intent(this, QRCodeActivity::class.java)
            intent.putExtra("appointment", appointmentId)
            intent.putExtra("entranceGate", thisDetails.entranceGate.id)
            intent.putExtra("exitGate", thisDetails.exitGate.id)
            intent.putExtra("truck", thisDetails.truck.id)
            intent.putExtra("driver", thisDetails.driver.id)
            intent.putExtra("securityGuard", SharedPreferenceHelper.getUser(this)!!.user.id)
            startActivity(intent)
        }
        viewAllBtn.setOnClickListener {
            val intent = Intent(this, AllItemsActivity::class.java)
            intent.putParcelableArrayListExtra("items", products)
            startActivity(intent)
        }
    }

    private fun acceptTheDriver(){

        showBottomLoader()
        AppointmentDetailsHelper.acceptDriver(this, appointmentId, {
            Toast.makeText(this, "Order Accepted", Toast.LENGTH_LONG).show()
            showQRCodeButton()
            startActivity(
                    Intent(this@AppointmentDetailsActivity, QRCodeActivity::class.java)
                            .putExtra("appointment", appointmentId)
                            .putExtra("entranceGate", thisDetails.entranceGate.id)
                            .putExtra("exitGate", thisDetails.exitGate.id)
                            .putExtra("truck", thisDetails.truck.id)
                            .putExtra("driver", thisDetails.driver.id)
                            .putExtra(
                                    "securityGuard",
                                    SharedPreferenceHelper.getUser(this@AppointmentDetailsActivity)!!.user.id
                            )
            )
            updateStatus(it.status)
        }, {
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun updateStatus(status: Int) {
        when (status) {
            4 -> {
                tvAppointmentStatus.text = getString(R.string.scheduled)
                tvAppointmentStatus.setTextColor(resources.getColor(R.color.order_pending))
                showBottomActionButtons()
            }
            5 -> {
                tvAppointmentStatus.text = getString(R.string.accepted)
                tvAppointmentStatus.setTextColor(resources.getColor(R.color.order_pending))
                showQRCodeButton()
            }
            6 -> {
                tvAppointmentStatus.text = getString(R.string.rejected)
                tvAppointmentStatus.setTextColor(resources.getColor(R.color.order_rejected))
                showOrderRejected()
            }
            7 -> {
                tvAppointmentStatus.text = getString(R.string.truck_entered)
                tvAppointmentStatus.setTextColor(resources.getColor(R.color.order_pending))
                showTruckEntered()
            }
            8 -> {
                tvAppointmentStatus.text = getString(R.string.completed)
                tvAppointmentStatus.setTextColor(resources.getColor(R.color.order_completed))
                showOrderCompleted()
            }
        }
    }


    private fun rejectTheDriver(){
        showBottomLoader()
        AppointmentDetailsHelper.rejectDriver(this, appointmentId, {
            Toast.makeText(this, "Order Rejected", Toast.LENGTH_LONG).show()
            showOrderRejected()
            updateStatus(it.status)
        }, {
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun getAppointmentDetailsData() {
        showLoaderLayout()
        AppointmentDetailsHelper.getAppointmentDetails(this, appointmentId, { appointmentDetails ->
            showMainLayout()
            this.appointmentDetails = appointmentDetails
            setDataToViews(appointmentDetails)
        }, { error ->
            error.message?.let { manageError(it) }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setDataToViews(appointmentDetails: AppointmentDetails?) {
        thisDetails = appointmentDetails!!
        ticketUrl = thisDetails.ticket
        products = ArrayList()
        appointmentDetails.orders.forEach { item ->
            val product = Products()
            product.status = item.status
            product.dock = item.dock.name
            product.warehouse = item.warehouse.name
            product.productName = item.product.name
            product.productCode = item.product.productCode
            products.add(product)
        }
        //tvAppointmentId.text = appointmentDetails.id
        updateStatus(appointmentDetails.status)
        if(appointmentDetails.user!=null)
            tvOwnerName.text = appointmentDetails.user.name
        else
            tvOwnerName.text = "User not added"

        tvSupplierName.text = appointmentDetails.supplier.name
        tvTotalItems.text = appointmentDetails.orderCount.toString()
        tvProductName1.text = appointmentDetails.orders[0].product.name
        tvProductCode1.text = appointmentDetails.orders[0].product.productCode
        tvWarehouseName1.text = appointmentDetails.orders[0].warehouse.name
        tvDockName1.text = appointmentDetails.orders[0].dock.name
        when(appointmentDetails.orders[0].status){
            1 -> {
                tvOrderStatus1.text = getString(R.string.status_pending)
                tvOrderStatus1.setBackgroundColor(resources.getColor(R.color.order_pending))
            }
            2 -> {
                tvOrderStatus1.text = getString(R.string.status_completed)
                tvOrderStatus1.setBackgroundColor(resources.getColor(R.color.order_completed))
            }
        }
        if(appointmentDetails.orderCount <= 2){
            viewAllBtn.visibility = View.GONE
        }
        if(appointmentDetails.orderCount < 2){
            orderCard2.visibility = View.GONE
        }else{
            tvProductName2.text = appointmentDetails.orders[1].product.name
            tvProductCode2.text = appointmentDetails.orders[1].product.productCode
            tvWarehouseName2.text = appointmentDetails.orders[1].warehouse.name
            tvDockName2.text = appointmentDetails.orders[1].dock.name
            when(appointmentDetails.orders[1].status){
                1 -> {
                    tvOrderStatus2.text = getString(R.string.status_pending)
                    tvOrderStatus2.setBackgroundColor(resources.getColor(R.color.order_pending))
                }
                2 -> {
                    tvOrderStatus2.text = getString(R.string.status_completed)
                    tvOrderStatus2.setBackgroundColor(resources.getColor(R.color.order_completed))
                }
            }
        }

        checkForTruck()
        checkForDriver()
    }

    private fun checkForDriver() {
        if(appointmentDetails==null) return
        if(appointmentDetails!!.driver == null){
            addDriverButton.visibility = View.VISIBLE
            driverDetailsLin.visibility = View.GONE

            addDriverButton.setOnClickListener(View.OnClickListener {
                val intent: Intent = Intent(
                        this@AppointmentDetailsActivity,
                        AddDriverActivity::class.java
                )
                intent.putExtra("supplier", appointmentDetails!!.supplier.id)
                startActivityForResult(intent, 11)
            })
        }else{
            addDriverButton.visibility = View.GONE
            driverDetailsLin.visibility = View.VISIBLE
            tvDriverName.text = appointmentDetails!!.driver.name
        }
    }

    private fun checkForTruck() {
        if(appointmentDetails==null) return
        if(appointmentDetails!!.truck == null){
            addTruckButton.visibility = View.VISIBLE
            truckDetailsLin.visibility = View.GONE

            addTruckButton.setOnClickListener(View.OnClickListener {
                val intent: Intent = Intent(
                        this@AppointmentDetailsActivity,
                        SearchTruckActivity::class.java
                )
                intent.putExtra("supplier", appointmentDetails!!.supplier.id)
                startActivityForResult(intent, 12)
            })
        }else{
            addTruckButton.visibility = View.GONE
            truckDetailsLin.visibility = View.VISIBLE
            truckNameTv.text = appointmentDetails!!.truck.name
            truckLicenseTv.text = appointmentDetails!!.truck.licensePlate
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 12) {
            if(data?.getStringExtra("truck") == null)return

            val truck: Truck = Gson().fromJson(data.getStringExtra("truck"),
                    object : TypeToken<Truck>() {}.type
            )
            showLoaderLayout()
            TruckApiServices.addTruckToAppointment(applicationContext, appointmentId, truck.id,
                    {
                        showMainLayout()
                        appointmentDetails!!.truck = truck
                        Toast.makeText(applicationContext, getString(R.string.vehicle_added_to_appointment), Toast.LENGTH_SHORT).show()
                        checkForTruck()
                    }, {
                showMainLayout()
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
            })
        }else if(requestCode == 11){
            if(data?.getStringExtra("driver") == null)return
            val driver: Driver = Gson().fromJson(data.getStringExtra("driver"),
                    object : TypeToken<Driver>() {}.type
            )
            showLoaderLayout()
            DriverApiServices.addDriverToAppointment(applicationContext, appointmentId, driver.id,
                    {
                        showMainLayout()
                        appointmentDetails!!.driver = driver
                        Toast.makeText(applicationContext, "Driver successfully added to appointment.", Toast.LENGTH_SHORT).show()
                        checkForDriver()

                        sendBroadcast(Intent("driver-add-receiver"))
                    }, {
                showMainLayout()
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun showLoaderLayout(){
        mainLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
        bottomActionsLayout.visibility = View.GONE
        loaderLayout.visibility = View.VISIBLE
    }

    private fun showMainLayout(){
        bottomActionsLayout.visibility = View.VISIBLE
        mainLayout.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        loaderLayout.visibility = View.GONE
    }

    private fun showBottomLoader(){
        bottomLoaderLayout.visibility = View.GONE
        orderRejectedLabel.visibility = View.GONE
        orderCompletedLabel.visibility = View.GONE
        generateQrCodeBtn.visibility = View.GONE
        bottomActionsLayout.visibility = View.GONE
        bottomLoaderLayout.visibility = View.VISIBLE
    }
    private fun showBottomActionButtons(){
        bottomLoaderLayout.visibility = View.GONE
        orderRejectedLabel.visibility = View.GONE
        orderCompletedLabel.visibility = View.GONE
        generateQrCodeBtn.visibility = View.GONE
        bottomLoaderLayout.visibility = View.GONE
        bottomActionsLayout.visibility = View.VISIBLE
    }

    private fun showOrderCompleted(){
        bottomLoaderLayout.visibility = View.GONE
        orderRejectedLabel.visibility = View.GONE
        bottomActionsLayout.visibility = View.GONE
        generateQrCodeBtn.visibility = View.GONE
        bottomLoaderLayout.visibility = View.GONE
        orderCompletedLabel.visibility = View.VISIBLE
    }

    private fun showOrderRejected(){
        bottomLoaderLayout.visibility = View.GONE
        bottomActionsLayout.visibility = View.GONE
        orderCompletedLabel.visibility = View.GONE
        generateQrCodeBtn.visibility = View.GONE
        bottomLoaderLayout.visibility = View.GONE
        orderRejectedLabel.visibility = View.VISIBLE
    }

    private fun showQRCodeButton(){
        bottomLoaderLayout.visibility = View.GONE
        bottomActionsLayout.visibility = View.GONE
        orderCompletedLabel.visibility = View.GONE
        orderRejectedLabel.visibility = View.GONE
        bottomLoaderLayout.visibility = View.GONE
        generateQrCodeBtn.visibility = View.VISIBLE
    }

    private fun showTruckEntered(){
        bottomLoaderLayout.visibility = View.GONE
        bottomActionsLayout.visibility = View.GONE
        orderCompletedLabel.visibility = View.GONE
        bottomLoaderLayout.visibility = View.GONE
        orderRejectedLabel.visibility = View.VISIBLE
        orderRejectedLabel.text = getString(R.string.truck_entered)
        orderRejectedLabel.setTextColor(resources.getColor(R.color.order_pending))
    }

    private fun manageError(errorMessage: String){
        loaderLayout.visibility = View.GONE
        mainLayout.visibility = View.GONE
        bottomActionsLayout.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        tvError.text = errorMessage
        retryBtn.setOnClickListener {
            getAppointmentDetailsData()
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvAppointmentStatus = findViewById(R.id.tv_appointment_status)
        tvOwnerName = findViewById(R.id.tv_owner_name)
        tvSupplierName = findViewById(R.id.tv_supplier_name)
        tvDriverName = findViewById(R.id.tv_driver_name)
        tvGateNo = findViewById(R.id.tv_gate_no)
        tvTotalItems = findViewById(R.id.tv_total_items)
        tvOrderStatus1 = findViewById(R.id.tv_order_status_1)
        tvOrderStatus2 = findViewById(R.id.tv_order_status_2)
        tvProductName1 = findViewById(R.id.tv_product_name_1)
        tvProductName2 = findViewById(R.id.tv_product_name_2)
        tvProductCode1 = findViewById(R.id.tv_product_code_1)
        tvProductCode2 = findViewById(R.id.tv_product_code_2)
        tvDockName1 = findViewById(R.id.tv_dock_name_1)
        tvDockName2 = findViewById(R.id.tv_dock_name_2)
        tvWarehouseName1 = findViewById(R.id.tv_warehouse_name_1)
        tvWarehouseName2 = findViewById(R.id.tv_warehouse_name_2)
        rejectBtn = findViewById(R.id.reject_btn)
        acceptBtn = findViewById(R.id.accept_btn)
        bottomActionsLayout = findViewById(R.id.bottom_actions)
        bottomLoaderLayout = findViewById(R.id.bottom_loader)
        loaderLayout = findViewById(R.id.loader_layout)
        mainLayout = findViewById(R.id.main_layout)
        errorLayout = findViewById(R.id.error_layout)
        tvError = findViewById(R.id.error_text_view)
        retryBtn = findViewById(R.id.retry_button)
        generateQrCodeBtn = findViewById(R.id.generate_qr_code_button)
        orderRejectedLabel = findViewById(R.id.order_rejected_label)
        orderCard2 = findViewById(R.id.order_card_2)
        orderCompletedLabel = findViewById(R.id.order_completed_label)
        viewAllBtn = findViewById(R.id.view_all_button)
        truckNameTv = findViewById(R.id.truck_name)
        truckLicenseTv = findViewById(R.id.truck_license)
        truckDetailsLin = findViewById(R.id.truck_details_lin)
        addTruckButton = findViewById(R.id.add_truck_btn)
        val face = Typeface.createFromAsset(assets, "poppins.ttf")
        truckNameTv.typeface = face
        addTruckButton.typeface = face
        truckLicenseTv.typeface = face

        addDriverButton = findViewById(R.id.add_driver_btn)
        addDriverButton.typeface = face
        driverDetailsLin = findViewById(R.id.driver_details_lin)
    }

    private fun setToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.title = getString(R.string.appointment_details)
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        Objects.requireNonNull(toolbar.navigationIcon)!!.setColorFilter(
                resources.getColor(R.color.white),
                PorterDuff.Mode.SRC_ATOP
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appointment_details_menu, menu)
        for (i in 0 until menu.size()){
            val mi = menu.getItem(i)

            val subMenu = mi.subMenu
            if (subMenu != null && subMenu.size() > 0) {
                for (j in 0 until subMenu.size()) {
                    val subMenuItem = subMenu.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }

            applyFontToMenuItem(mi)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_view_ticket -> {
                if (ticketUrl.isNotEmpty()) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(thisDetails.ticket))
                    startActivity(browserIntent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "poppins.ttf")
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(
                CustomTypefaceSpan("", font),
                0,
                mNewTitle.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        mi.title = mNewTitle
    }
}