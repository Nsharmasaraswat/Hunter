package com.gtpautomation.driver.api_services

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Created by Sunil Kumar on 11-12-2020 12:38 PM.
 */

class VolleySingleton private constructor(context: Context) {
    private var mRequestQueue: RequestQueue?
    private fun getRequestQueue(mCtx: Context): RequestQueue? {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.applicationContext)
        }
        return mRequestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>?, mCtx: Context) {
        getRequestQueue(mCtx)!!.add(req)
    }

    companion object {
        private var mInstance: VolleySingleton? = null

        @Synchronized
        fun getInstance(context: Context): VolleySingleton? {
            if (mInstance == null) {
                mInstance = VolleySingleton(context)
            }
            return mInstance
        }
    }

    init {
        mRequestQueue = getRequestQueue(context)
    }
}