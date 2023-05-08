package com.iot.control.infrastructure.mqtt

import android.util.Log
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.iot.control.infrastructure.EventManager
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

class Mqtt3Client(
    val client: com.hivemq.client.mqtt.mqtt3.Mqtt3Client,
    val address: String,
    val manager: EventManager
) : MqttClient {
    companion object {
        const val TAG = "Mqtt3Client"
    }
    private val subscribes: MutableSet<String> = mutableSetOf()

    override fun connect() {
        client.toAsync().connectWith()
            .keepAlive(60)
            .send()
            .whenComplete { _, throwable ->
                if(throwable != null) {}

            }
    }

    override fun disconnect() {
        if(client.state.isConnected) client.toAsync().disconnect()
    }

    override fun publish(topic: String, payload: String, callback: () -> Unit) {
        client.toAsync().publishWith()
            .topic(topic)
            .payload(payload.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
            .whenComplete { _, throwable ->
                if(throwable != null) {
                    Log.d(Mqtt5Client.TAG, "Failed publish in $topic with error, ${throwable.message}")
                } else {
                    Log.d(Mqtt5Client.TAG, "Successfully publish in $topic")
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