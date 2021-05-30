package com.gtpautomation.securityguard.pages.add_truck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.add_truck.adapter.AddTruckFieldAdapter
import com.gtpautomation.securityguard.pojos.truck.TruckField
import com.gtpautomation.securityguard.utils.apiCall.TruckApiServices
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class AddTruckActivity : AppCompatActivity() {
    private var fieldListAdapter: AddTruckFieldAdapter? = null
    private lateinit var nameText: EditText
    private lateinit var licenseText: EditText
    private lateinit var fieldsListView: RecyclerView
    private lateinit var createButton: MaterialButton
    private lateinit var loaderLayout: LinearLayout
    private var supplier: String? = null

    private var truckFields: List<TruckField>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_truck)
        supplier = intent.getStringExtra("supplier")

        initViews()
        fetchFields()
    }

    private fun initViews() {
        supportActionBar?.title = resources.getString(R.string.create_truck)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameText = findViewById(R.id.add_truck_name)
        licenseText = findViewById(R.id.add_truck_license)
        fieldsListView = findViewById(R.id.form_list_lin)
        createButton = findViewById(R.id.create_button)
        loaderLayout = findViewById(R.id.loader_layout)
        createButton.setOnClickListener {
            validateAndPost()
        }
        hideLoader()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoader(){
        loaderLayout.visibility = View.VISIBLE
        createButton.visibility = View.GONE
    }
    private fun hideLoader(){
        loaderLayout.visibility = View.GONE
        createButton.visibility = View.VISIBLE
    }
    private fun validateAndPost() {
        val name = nameText.text.trim()
        val license = licenseText.text.trim()
        if(name.isEmpty()){
            Toast.makeText(applicationContext, "Truck name is required", Toast.LENGTH_SHORT).show()
            return
        }else if(license.isEmpty()){
            Toast.makeText(applicationContext, "Truck license is required", Toast.LENGTH_SHORT).show()
            return
        }else {
            if(fieldListAdapter!=null) {
                val fields = fieldListAdapter!!.getFields()
                val body = JSONObject()
                try {
                    body.put("supplier", supplier)
                    body.put("name", name)
                    body.put("licensePlate", license)
                    body.put("fields", fields)
                    showLoader()
                    TruckApiServices.createTruck(applicationContext,body,
                            {
                                hideLoader()
                                val intent = Intent()
                                intent.putExtra("truck", it)
                                setResult(10,intent)
                                finish()
                            }, {
                        hideLoader()
                        Toast.makeText(applicationContext,it.message,Toast.LENGTH_SHORT).show()
                    })
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchFields() {
        TruckApiServices.geTruckFields(applicationContext, {
            truckFields = Gson().fromJson<List<TruckField>>(
                    it,
                    object : TypeToken<List<TruckField?>?>() {}.type
            )
            fieldListAdapter = AddTruckFieldAdapter(this@AddTruckActivity, truckFields as ArrayList<TruckField>)

            fieldsListView.setHasFixedSize(true)
            fieldsListView.layoutManager = LinearLayoutManager(this)
            fieldsListView.adapter = fieldListAdapter
        },
                { Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show() })
    }
}