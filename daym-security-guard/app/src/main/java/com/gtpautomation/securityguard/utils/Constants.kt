package com.gtpautomation.securityguard.utils

class Constants {
    companion object{
      private const val API_URL = "http://api.daym.smarttersstudio.com/"
//        private const val API_URL = "http://api.test.daym.smarttersstudio.com/"

        private const val API_URL_V1 = API_URL + "v1/"
        const val AUTH_URL = API_URL + "authentication"
        const val APPOINTMENT_URL = API_URL_V1 + "appointment"
        const val TRUCK_URL = API_URL_V1 + "truck"
        const val TRUCK_FIELDS = API_URL_V1 + "truck-field"

        const val USER = API_URL_V1 + "user"
        const val USER_TYPE = API_URL_V1 + "user-type"
        const val USER_FIELDS = API_URL_V1 + "user-field"

        const val UPLOAD = API_URL_V1 + "upload"

        const val DEFAULT_ERROR_STATUS_CODE = 0
        const val AUTH_ERROR_STATUS_CODE = 401
    }
}