package com.gtpautomation.driver.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gtpautomation.driver.R
import com.gtpautomation.driver.api_services.NotificationService
import com.gtpautomation.driver.api_services.VolleyErrorResolver
import com.gtpautomation.driver.pages.MainActivity
import com.gtpautomation.driver.utils.SharedPreferenceHelper


class DriverFirebaseNotificationService : FirebaseMessagingService() {
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(TAG, "Data: ${remoteMessage.data}")

        if (remoteMessage.data["orderCompleted"] == "true") {
            Handler(Looper.getMainLooper()).postDelayed({
                Log.e(TAG, "Data after 10 minute: ${remoteMessage.data}")
                NotificationService.sendNotificationTOSecurityGuard(this, object : NotificationService.SuccessListener {
                    override fun onSuccess(response: String) {

                    }
                }, object : NotificationService.FailureListener {
                    override fun onFailure(customError: VolleyErrorResolver.CustomError) {

                    }
                })
            }, 1000 * 60 * 1)
        }
        sendNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
    }
    override fun onNewToken(token: String) {
        Log.e(TAG, "Refreshed token: $token")
        SharedPreferenceHelper.setFcmId(this, token)
    }

    private fun sendNotification(title: String?, body: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    "Default channel",
                    NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FirebaseMsgService"
    }
}