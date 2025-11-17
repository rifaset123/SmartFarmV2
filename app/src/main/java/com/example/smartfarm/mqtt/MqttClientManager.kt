package com.example.smartfarm.mqtt

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttClientManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // change to your broker
    private val serverUri = "tcp://10.120.32.98:1883"

    private val clientId = "android-" + UUID.randomUUID().toString().take(8)

    private val mqttClient: MqttAndroidClient by lazy {
        MqttAndroidClient(context, serverUri, clientId)
    }

    private var connected = false

    fun connect(
        onConnected: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        if (connected) {
            onConnected()
            return
        }

        val options = MqttConnectOptions().apply {
            isCleanSession = false
            keepAliveInterval = 30
            // set username/password here if broker needs it
        }

        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                // we will forward it via listener interface
                val payload = message?.toString() ?: return
                notifyListeners(topic ?: "", payload)
            }

            override fun connectionLost(cause: Throwable?) {
                connected = false
                Log.e("MQTT", "connection lost ${cause?.message}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                connected = true
                onConnected()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                connected = false
                onError(exception ?: RuntimeException("MQTT connect failed"))
            }
        })
    }

    fun subscribe(topic: String) {
        if (!connected) return
        mqttClient.subscribe(topic, 1, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "subscribed $topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "subscribe failed", exception)
            }
        })
    }

    fun publish(topic: String, payload: String) {
        if (!connected) return
        val msg = MqttMessage().apply {
            this.payload = payload.toByteArray()
            qos = 1
        }
        mqttClient.publish(topic, msg)
    }

    // simple listener fan out
    private val listeners = mutableSetOf<(topic: String, payload: String) -> Unit>()

    fun addListener(listener: (String, String) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (String, String) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners(topic: String, payload: String) {
        listeners.forEach { it(topic, payload) }
    }
}
