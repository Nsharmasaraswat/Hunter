package com.gtpautomation.securityguard.utils

import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.gtpautomation.securityguard.pages.LoginActivity
import com.gtpautomation.securityguard.pages.MainActivity
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.utils.sharedPreferenceHelper.SharedPreferenceHelper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setWindow()
        checkUser()
    }

    /**
     * Set the activity transition and make it fullscreen
     */
    private fun setWindow() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.enterTransition = Explode()
        window.exitTransition = Explode()
    }

    private fun checkUser() {
        if (SharedPreferenceHelper.getUser(this@SplashActivity) != null){
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }
}