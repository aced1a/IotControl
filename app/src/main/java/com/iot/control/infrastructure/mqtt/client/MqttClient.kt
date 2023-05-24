package com.iot.control.infrastructure.mqtt.client

import com.iot.control.infrastructure.mqtt.MqttConfigurator

interface MqttClient {

    fun connect()
    fun disconnect()
    fun publish(topic: String, payload: String, callback: () -> Unit, onFail: () -> Unit)
    fun subscribe(topic: String)
    fun unsubscribe(topic: String)

    companion object {
        fun configure(): MqttConfigurator = MqttConfigurator()
    }
}

