package com.iot.control.infrastructure.mqtt.client

import android.util.Log
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder
import com.iot.control.R
import com.iot.control.infrastructure.EventManager
import com.iot.control.infrastructure.NotificationManager
import kotlinx.coroutines.launch
import kotlin.jvm.optionals.getOrNull

class Mqtt3Client(
    builder: Mqtt3ClientBuilder,
//    val client: com.hivemq.client.mqtt.mqtt3.Mqtt3Client,
    val address: String,
    private val manager: EventManager,
    private val notificationManager: NotificationManager,
    val onDisconnected: () -> Unit
) : MqttClient {
    companion object {
        const val TAG = "Mqtt3Client"
    }

    private var reconnection: Boolean = false
    private var counter: Int = 0
    private var wasConnected = false
    private val client = builder
        .addConnectedListener {
            reconnection = false
            counter = 0
        }
        .addDisconnectedListener {
            Log.d(TAG, "Disconnected by ${it.source.name}: ${it.cause.message}")
            if(!wasConnected && counter >= 4) {
                it.reconnector.reconnect(false)
                onDisconnected()
                notificationManager.notify(R.string.disconnected_label, R.string.disconnected_text, address)

            } else if(!reconnection && counter >= 3) {
                reconnection = true
                notificationManager.notify(R.string.reconnection_label, R.string.reconnection_text, address)
            }
            counter++
        }
        .build()

    private val subscribes: MutableSet<String> = mutableSetOf()

    override fun connect() {
        client.toAsync().connectWith()
            .keepAlive(60)
            .send()
            .whenComplete { _, throwable ->
                if(throwable != null) {
                    onDisconnected()
                    notificationManager.notify(R.string.failed_connection_label, R.string.failed_connection_text, address)

                    Log.d(TAG, "Failed connecting to $address")
                } else {
                    wasConnected = true
                    notificationManager.notify(R.string.connected_label, R.string.connected_text, address)
                    Log.d(TAG, "Successful connecting to $address")
                }
            }
    }

    override fun disconnect() {
        if(client.state.isConnected) {
            wasConnected = false
            counter = 99
            client.toAsync().disconnect()
        }
    }

    override fun publish(topic: String, payload: String, callback: () -> Unit, onFail: () -> Unit) {
        client.toAsync().publishWith()
            .topic(topic)
            .payload(payload.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
            .whenComplete { _, throwable ->
                if(throwable != null) {
                    Log.d(TAG, "Failed publish in $topic with error, ${throwable.message}")
                } else {
                    Log.d(TAG, "Successfully publish in $topic")
                    callback()
                }
            }
    }

    override fun subscribe(topic: String) {
        if(subscribes.contains(topic)) return
        else subscribes.add(topic)

        client.toAsync().subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                val buf = publish.payload.getOrNull()
                val payload = if(buf != null) Charsets.UTF_8.decode(buf).toString() else return@callback

                kotlinx.coroutines.MainScope().launch {
                    manager.resolveMqttEvent(address, publish.topic.toString(), payload)
                }
            }
            .send()
            .whenComplete { _, throwable ->
                if(throwable != null) {
                    Log.d(TAG, "Failed to subscribe on topic: $topic")
                } else {
                    Log.d(TAG, "Successfully subscribed on topic: $topic")
                }
            }
    }

    override fun unsubscribe(topic: String) {
        subscribes.remove(topic)
        client.toAsync().unsubscribeWith()
            .topicFilter(topic)
            .send()
            .whenComplete { _, throwable ->
                if(throwable != null) {
                    Log.d(TAG, "Failed to unsubscribe from topic: $topic")
                } else {
                    Log.d(TAG, "Successfully unsubscribe from topic: $topic")
                }
            }
    }
}