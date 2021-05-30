package com.gtpautomation.securityguard.utils.apiCall

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError

class AccessTokenHelper {
    companion object{
        fun getAccessToken(
            context: Context,
            onSuccessListener: OnSuccessListener,
            notFoundListener: NotFoundListener
        ) {
            val sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE)
            val accessToken = sharedPreferences.getString("accessToken", "")
            if (accessToken == "") {
                notFoundListener.onAccessTokenNotFound()
            } else {
                Log.d("accessToken", accessToken!!)
                onSuccessListener.onSuccess(accessToken)
                ////Log.e("accessToken",accessToken);
            }
        }


        fun setAccessToken(context: Context, accessToken: String?, time: Long) {
            val sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("accessToken", accessToken)
            editor.putLong("time", time)
            editor.putInt("level", 1)
            editor.apply()
        }

        fun getAccessTokenInStr(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences("login", Context.MODE_PRIVATE)
            return sharedPreferences.getString("accessToken", "")
        }

    }
    interface OnSuccessListener {
        fun onSuccess(accessToken: String?)
    }

    interface OnFailureListener {
        fun onFailure(error: VolleyError?)
    }

    interface NotFoundListener {
        fun onAccessTokenNotFound()
    }
}