// MqttNotificationHandler.kt
package com.example.smartfarm.mqtt

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.smartfarm.util.AppNotifier
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttNotificationHandler @Inject constructor(
    private val mqtt: MqttSyncManager,
    private val notifier: AppNotifier
) {
    private var scope: CoroutineScope? = null
    private var topicNotif: String? = null
    private var currentUserId: String? = null

    fun start(userId: String) {
        // Kalau sudah jalan dengan userId yang sama, tidak usah restart
        if (userId == currentUserId && scope?.isActive == true) {
            return
        }

        // Matikan instance lama dulu
        stop()

        currentUserId = userId
        val topic = "be/broiler/notif/$userId"
        topicNotif = topic

        mqtt.connect()
        mqtt.subscribe(topic)

        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO).also { sc ->
            sc.launch {
                mqtt.messages.collect { msg ->
                    if (msg.topic == topic) {
                        handlePayload(msg.payload)
                    }
                }
            }
        }
    }

    fun stop() {
        topicNotif?.let { mqtt.unsubscribe(it) }
        topicNotif = null
        currentUserId = null
        scope?.cancel()
        scope = null
    }

    private fun handlePayload(payload: String) {
        try {
            val json = JSONObject(payload)

            // Ambil nama kandang dari key "cage_name"
            val cageName = json.optString("cage_name", null)

            // Ambil isi pesan kalau ada, kalau tidak pakai default
            val body = json.optString(
                "message",
                "Kandang $cageName sedang Abnormal!"
            )

            // Kalau cageName ada, tambahkan ke title
            val title = if (!cageName.isNullOrBlank()) {
                "SmartFarm - $cageName"
            } else {
                "SmartFarm"
            }

            notifier.showNotification(
                title = title,
                message = body
            )
        } catch (e: Exception) {
            // Kalau payload bukan JSON valid, fallback ke teks mentah
            notifier.showNotification(
                title = "SmartFarm",
                message = payload
            )
        }
    }

}



