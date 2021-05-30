package com.gtpautomation.securityguard.pages

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.gtpautomation.securityguard.R
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.json.JSONObject
import java.util.*

class QRCodeActivity : AppCompatActivity() {
    private lateinit var qrCodeView: ImageView
    private lateinit var appointment: String
    private lateinit var entranceGate: String
    private lateinit var exitGate: String
    private lateinit var truck: String
    private lateinit var driver: String
    private lateinit var securityGuard: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r_code)
        setToolBar()
        qrCodeView = findViewById(R.id.qr_code_image_view)
        appointment = intent.getStringExtra("appointment").toString()
        entranceGate = intent.getStringExtra("entranceGate").toString()
        exitGate = intent.getStringExtra("exitGate").toString()
        truck = intent.getStringExtra("truck").toString()
        driver = intent.getStringExtra("driver").toString()
        securityGuard = intent.getStringExtra("securityGuard").toString()

        val jsonObj = JSONObject()
        jsonObj.put("appointment", appointment)
        jsonObj.put("entranceGate", entranceGate)
        jsonObj.put("exitGate", exitGate)
        jsonObj.put("truck", truck)
        jsonObj.put("driverId", driver)
        jsonObj.put("securityGuardId", securityGuard)

        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(jsonObj.toString(), BarcodeFormat.QR_CODE, 200, 200)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            qrCodeView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun setToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.title = getString(R.string.qr_code)
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        Objects.requireNonNull(toolbar.navigationIcon)!!.setColorFilter(
            resources.getColor(R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}