package com.gtpautomation.securityguard.pages

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.gtpautomation.securityguard.R
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanActivity : Activity(), ZXingScannerView.ResultHandler {
    private var mScannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_scan)
        mScannerView = findViewById(R.id.scanner_view)
        mScannerView?.setFormats(listOf(BarcodeFormat.PDF_417))
        mScannerView?.setAspectTolerance(0.5f)

        if (checkPermission()) {
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
           return false
        } else true
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            101
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    val dialog: AlertDialog = AlertDialog.Builder(this)
                            .setMessage(getString(R.string.allow_cam_permission))
                            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermission()
                                }
                            }
                            .setNegativeButton(getString(R.string.no)) { _, _ ->
                                finish()
                            }
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    val textView = dialog!!.findViewById<View>(android.R.id.message) as TextView
                    val face = Typeface.createFromAsset(assets, "poppins.ttf")
                    textView.typeface = face
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        Log.e("TAG", "handleResult: ${rawResult.text}")
        onPause()
        startActivity(Intent(this@ScanActivity, AppointmentDetailsActivity::class.java).putExtra("appointment", rawResult.text))
        finish()
    }
}