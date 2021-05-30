package com.gtpautomation.securityguard.utils.sharedPreferenceHelper

import android.content.Context
import com.google.gson.Gson
import com.gtpautomation.securityguard.pojos.userModel.UserResponse

class SharedPreferenceHelper {
    companion object{
        fun getUser(context: Context): UserResponse? {
            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val gson = Gson()
            return gson.fromJson(
                sharedPreferences.getString("user_info", ""),
                UserResponse::class.java
            )
        }

        fun setUser(context: Context, user: UserResponse?) {
            val gson = Gson()
            val str = gson.toJson(user)
            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("user_info", str)
            editor.apply()
        }

        fun setUser(context: Context, response: String?) {
            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("user_info", response)
            editor.apply()
        }

        fun clearPref(context: Context) {
            val sharedPreferences =
                context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }
    }
}