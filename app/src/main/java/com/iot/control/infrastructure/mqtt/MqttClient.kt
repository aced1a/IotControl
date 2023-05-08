package com.iot.control.infrastructure.mqtt

interface MqttClient {

    fun connect()
    fun disconnect()
    fun publish(topic: String, payload: String, callback: () -> Unit)
    fun subscribe(topic: String)
    fun unsubscribe(topic: String)

    enum class Version { Mqtt3, Mqtt5 }

    companion object {
        fun configure(): MqttConfigurator = MqttConfigurator()
    }
}

