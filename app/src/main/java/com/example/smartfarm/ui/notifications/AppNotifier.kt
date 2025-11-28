// AppNotifier.kt
package com.example.smartfarm.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartfarm.MainActivity
import com.example.smartfarm.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val channelId = "broiler_alerts"

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                channelId,
                "Broiler Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            mgr.createNotificationChannel(ch)
        }
    }

    fun showNotification(title: String, message: String) {
        Log.d("AppNotifier", "showNotification called title=$title message=$message")

        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.app_logo_full) // pastikan ada
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        mgr.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
