package com.gtpautomation.driver.api_services;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class VolleyErrorResolver {
    public static CustomError getErrorMessage(VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            return new CustomError("Unable to connect to server", 0);
        } else if (error instanceof AuthFailureError) {
            return new CustomError("Something went wrong please login to continue", 401);
        } else if (error.networkResponse != null) {

            switch (error.networkResponse.statusCode) {
                case 409:
                case 400:
                    try {
                        String badUrlResponse = new JSONObject(new String(error.networkResponse.data)).getString("message");
                        return new CustomError(badUrlResponse, error.networkResponse.statusCode);
                    } catch (JSONException ignored) {
                        return new CustomError("Unknown Error Occurred", 1);
                    }
                case 401:
                    return new CustomError("Something went wrong please login to continue", 401);
                case 404:
                    return new CustomError("Unable to connect to server", error.networkResponse.statusCode);
                case 413:
                    return new CustomError("File size too large", 413);
                case 500:
                    try {
                        String five_hundred_error = new JSONObject(new String(error.networkResponse.data)).getString("message");
                        return new CustomError(five_hundred_error, error.networkResponse.statusCode);
                    } catch (JSONException ignored) {
                    }
                default:
                    return new CustomError("Some error occurred, please try again later", error.networkResponse.statusCode);
            }
        } else
            return new CustomError("Unknown Error Occurred", 1);
    }

    public static class CustomError {
        private String message;
        private int statusCode;

        public CustomError() {
        }

        public CustomError(String message, int statusCode) {
            this.message = message;
            this.statusCode = statusCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }
    }
}

