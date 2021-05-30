package com.gtpautomation.securityguard.pages.add_driver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.add_driver.adapter.AddDriverAdapter
import com.gtpautomation.securityguard.pojos.driver.UserType
import com.gtpautomation.securityguard.pojos.userModel.UserField
import com.gtpautomation.securityguard.utils.apiCall.DriverApiServices
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class AddDriverActivity : AppCompatActivity() {
    private var userType: List<UserType>?=null
    private var fieldListAdapter: AddDriverAdapter? = null

    private lateinit var nameText: EditText
    private lateinit var userNameText: EditText
    private lateinit var passwordText: EditText
    private lateinit var fieldsListView: RecyclerView
    private lateinit var createButton: MaterialButton
    private lateinit var loaderLayout: LinearLayout
    private var supplier: String? = null
    private var userFields: List<UserField>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_driver)
        supplier = intent.getStringExtra("supplier")

        initViews()
        fetchFields()
    }

    private fun initViews() {
        supportActionBar?.title = "Add driver"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameText = findViewById(R.id.add_driver_name)
        userNameText = findViewById(R.id.add_driver_username)
        passwordText = findViewById(R.id.add_driver_password)

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
        val userName = userNameText.text.trim()
        val password = passwordText.text.trim()
        when {
            name.isEmpty() -> {
                Toast.makeText(applicationContext, "Driver name is required", Toast.LENGTH_SHORT).show()
                return
            }
            userName.isEmpty() -> {
                Toast.makeText(applicationContext, "Driver username is required", Toast.LENGTH_SHORT).show()
                return
            }
            password.isEmpty() -> {
                Toast.makeText(applicationContext, "Driver password is required", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                val body = JSONObject()
                try {
                    body.put("parent", supplier)
                    body.put("name", name)
                    body.put("userName", userName)
                    body.put("password", password)
                    body.put("userType", userType!!.first().id)
                    body.put("avatar","https://st4.depositphotos.com/4329009/19956/v/600/depositphotos_199564354-stock-illustration-creative-vector-illustration-default-avatar.jpg")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                if(fieldListAdapter!=null) {
                    val fields = fieldListAdapter!!.getFields() ?: return
                    body.put("fields", fields)
                }
                showLoader()
                DriverApiServices.createDriver(applicationContext, body,
                        {
                            Log.e("TAG", "validateAndPost: $it")
                            hideLoader()
                            val intent = Intent()
                            intent.putExtra("driver", it)
                            setResult(11, intent)
                            finish()
                        }, {
                    hideLoader()
                    Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                })

            }
        }
    }

    private fun fetchFields() {
        DriverApiServices.getDriverUserType(applicationContext, {
           userType = Gson().fromJson<List<UserType>>(
                it,
                object : TypeToken<List<UserType?>?>() {}.type
            )
            if(userType!!.isEmpty())return@getDriverUserType
            DriverApiServices.getDriverUserFields(applicationContext, userType!!.first().id,
                { fieldsRes ->
                    userFields = Gson().fromJson<List<UserField>>(
                        fieldsRes,
                        object : TypeToken<List<UserField?>?>() {}.type
                    )
                    fieldListAdapter = AddDriverAdapter(this@AddDriverActivity, userFields as ArrayList<UserField>)

                    fieldsListView.setHasFixedSize(true)
                    fieldsListView.layoutManager = LinearLayoutManager(this)
                    fieldsListView.adapter = fieldListAdapter
                }, { error ->
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                })
        }, {
            Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
        })
    }
//    fun addDriverPicture(view: View) {}
}