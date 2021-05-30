package com.gtpautomation.securityguard.pages

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.utils.apiCall.AuthenticationHelper
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var userNameText: EditText
    private lateinit var passwordText:EditText
    private lateinit  var userName: String
    private lateinit var password:String
    private lateinit var body: JSONObject
    private lateinit var progressDialog: ProgressBar
    private lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViews()
    }

    private fun initViews() {
        userNameText = findViewById(R.id.login_user_name)
        passwordText = findViewById(R.id.login_password)
        loginBtn = findViewById(R.id.login_btn)
        body = JSONObject()
        progressDialog = findViewById(R.id.login_progress)
        loginBtn.setOnClickListener {
            doLogin()
        }

    }


    /**
     * Empty checking validation is done
     * wrong inputs are handled
     * checking of the terms and conditions check box is looked after
     */
    private fun validate(): Boolean {
        userName = userNameText.text.toString().trim { it <= ' ' }
        password = passwordText.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userName)) {
            userNameText.requestFocus()
            userNameText.error = getString(R.string.user_name_empty_message)
            return false
        } else if (TextUtils.isEmpty(password)) {
            passwordText.requestFocus()
            passwordText.error = getString(R.string.password_empty_message)
            return false
        }
//        else if (!validatePassword(password!!)) {
//            passwordText!!.requestFocus()
//            passwordText!!.error = "Your password must contain at least a lowercase alphabet, an uppercase alphabet and a special character"
//            return false
//        }
        return true
    }
//
//    /**
//     * password is validated
//     * where a user must have to input one alphabet , one number and one special character
//     */
//    private fun validatePassword(password: String): Boolean {
//        val pattern: Pattern
//        val matcher: Matcher
//        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
//        pattern = Pattern.compile(passwordPattern)
//        matcher = pattern.matcher(password)
//        return matcher.matches()
//    }

    private fun fetchFCMId() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val fcmId: String? = task.result
                body.put("fcmId", fcmId)
                handleSignIn()
            }
        }.addOnFailureListener { e ->
            progressDialog.visibility = View.GONE
            loginBtn.visibility = View.VISIBLE
            Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * API call is handled where credentials are send via body and the access token
     * is fetched as response
     */
    private fun handleSignIn() {
        AuthenticationHelper.loginWithEmailAndPassword(this@LoginActivity,
            body,
            {
                progressDialog.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }, { error ->
                progressDialog.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun doLogin() {
        if (validate()) {
            try {
                progressDialog.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
                body.put("userName", userName)
                body.put("password", password)
                body.put("strategy", "local")
                fetchFCMId()
            } catch (ignored: JSONException) { }
        }
    }
}