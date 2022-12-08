package com.sis.clightapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.sis.clightapp.R
import com.sis.clightapp.activity.MainEntryActivityNew
import com.sis.clightapp.model.GsonModel.FirebaseNotificationModel
import com.sis.clightapp.util.AppConstants
import com.sis.clightapp.util.CustomSharedPreferences
import java.lang.Exception


class FCMService : FirebaseMessagingService() {
    private val prefs = CustomSharedPreferences()
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            try {
                val notificationModel = Gson().fromJson(
                    remoteMessage.data["pwsUpdate"],
                    FirebaseNotificationModel::class.java
                )
                if (!notificationModel.invoice_label.isEmpty()) {
                    sendIncomingPaymentNotification(
                        notificationModel.title,
                        notificationModel.invoice_label,
                        notificationModel.body
                    )
                    val broadcastIntent = Intent(AppConstants.PAYMENT_RECEIVED_NOTIFICATION)
                    broadcastIntent.putExtra(
                        AppConstants.PAYMENT_INVOICE,
                        Gson().toJson(notificationModel)
                    )
                    applicationContext.sendOrderedBroadcast(broadcastIntent, null)
                } else {
                    sendNotification(
                        remoteMessage.notification!!
                            .title, remoteMessage.notification!!.body
                    )
                }
            } catch (e: Exception) {
                sendNotification(
                    remoteMessage.notification!!.title, remoteMessage.notification!!.body
                )
            }
        }
    }

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        prefs.setvalue(token, "FcmToken", applicationContext)
        prefs.setvalue("1", "IsTokenSet", applicationContext)
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainEntryActivityNew::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.newappicon)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.newappicon)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setLargeIcon(largeIcon)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendIncomingPaymentNotification(
        title: String,
        invoiceLabel: String,
        messageBody: String
    ) {
        val intent = Intent(this, MainEntryActivityNew::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_notification_regular)
        val channelId = getString(R.string.payment_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.newappicon)
            .setContentTitle(title)
            .setContentText(invoiceLabel)
            .setLargeIcon(largeIcon)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messageBody)
            )
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FCMService"
    }
}