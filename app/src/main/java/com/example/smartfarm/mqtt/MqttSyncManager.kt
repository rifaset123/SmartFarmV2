package com.example.smartfarm.mqtt

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttSyncManager @Inject constructor(
    @ApplicationContext context: Context
) {
    // change to your broker
    private val serverUri = "tcp://10.94.215.155"
    private val clientId = "android-" + UUID.randomUUID().toString().take(8)

    private val client = MqttAndroidClient(context, serverUri, clientId)

    // Track what we want subscribed
    private val wantedTopics = mutableSetOf<String>()
    // Track what the broker currently has
    private val activeTopics = mutableSetOf<String>()

    private val pendingTopics = mutableSetOf<String>()
    @Volatile private var connected = false

    private val _messages = MutableSharedFlow<MqttMsg>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<MqttMsg> = _messages

    data class MqttMsg(
        val topic: String,
        val payload: String,
        val qos: Int,
        val retained: Boolean
    )

    init {
        client.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (topic != null && message != null) {
                    _messages.tryEmit(
                        MqttMsg(
                            topic,
                            message.toString(),
                            message.qos,
                            message.isRetained
                        )
                    )
                }
            }

            override fun connectionLost(cause: Throwable?) {
                connected = false
                Log.e("MQTT", "lost: ${cause?.message}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
    }

    fun connect() {
        if (connected) return
        val opts = MqttConnectOptions().apply {
            isCleanSession = false
            keepAliveInterval = 30
        }
        client.connect(opts, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                connected = true
                // Re-sync desired vs actual subscriptions after connect
                resubscribeWanted()
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "connect fail", exception)
            }
        })
    }

    /** Subscribe and remember it */
    fun subscribe(topic: String, qos: Int = 1) {
        wantedTopics.add(topic)
        if (!connected) return  // will be applied on next connect
        client.subscribe(topic, qos, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                activeTopics.add(topic)
                Log.d("MQTT", "subscribed $topic")
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "sub fail $topic", exception)
            }
        })
    }

    /** Unsubscribe NOW (if connected) and forget it so it won’t come back later */
    fun unsubscribe(topic: String) {
        wantedTopics.remove(topic)
        if (!connected) {
            activeTopics.remove(topic)
            return
        }
        if (!activeTopics.contains(topic)) return
        client.unsubscribe(topic, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                activeTopics.remove(topic)
                Log.d("MQTT", "unsubscribed $topic")
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "unsub fail $topic", exception)
            }
        })
    }

    /** Optional helper */
    fun unsubscribeAll() {
        val copy = activeTopics.toList()
        copy.forEach { unsubscribe(it) }
        wantedTopics.clear()
    }

    /** Re-apply desired subscriptions after (re)connect */
    private fun resubscribeWanted() {
        // Unsubscribe anything active-but-not-wanted
        (activeTopics - wantedTopics).toList().forEach { topic ->
            unsubscribe(topic)
        }
        // Subscribe anything wanted-but-not-active
        (wantedTopics - activeTopics).toList().forEach { topic ->
            subscribe(topic)
        }
    }
}