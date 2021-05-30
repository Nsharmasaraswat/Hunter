package com.gtpautomation.driver.pages.profile

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.driver.R
import com.gtpautomation.driver.data_models.DriverSession
import com.gtpautomation.driver.utils.SharedPreferenceHelper

class ProfileActivity : AppCompatActivity() {
    private var driverSession: DriverSession? = null
    private lateinit var nameText: TextView
    private lateinit var userNameText: TextView
    private lateinit var fieldsView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        driverSession = SharedPreferenceHelper.getDriverSession(applicationContext)

        initViews()
    }

    private fun initViews() {
        supportActionBar?.title = "Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameText = findViewById(R.id.profile_name)
        userNameText = findViewById(R.id.profile_username)
        fieldsView = findViewById(R.id.profile_fields_list)
        fieldsView.layoutManager = LinearLayoutManager(this)
        inflateData()
    }

    private fun inflateData() {
        nameText.text = driverSession?.user?.name
        userNameText.text = driverSession?.user?.userName
        fieldsView.adapter = ProfileFieldsAdapter(this, driverSession!!.user.fields)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}