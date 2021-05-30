package com.gtpautomation.driver.utils

/**
 * Created by Sunil Kumar on 11-12-2020 12:33 PM.
 */
class ApiRoutes {
    companion object {
        const val baseUrl = /*"http://192.168.15.40:3030"*/"http://api.daym.smarttersstudio.com"
        const val baseUrlV1 = "$baseUrl/v1"
        const val appointment = "$baseUrlV1/appointment"
        const val createDriverSession = "$baseUrlV1/create-driver-session"
        const val driverSession = "$baseUrlV1/driver-session"
        const val order = "$baseUrlV1/order"
        const val notification = "$baseUrlV1/notification"
    }
}