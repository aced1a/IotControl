package com.iot.control.infrastructure.mqtt

class MqttClient {
    private val subscription: MutableSet<String> = mutableSetOf()

    fun connect() {}
    fun publish(topic: String, payload: String) {}
    fun subscribe(topic: String) {}
    fun unsubscribe(){}

    enum class Version { Mqtt3, Mqtt5 }

    companion object {
        fun configure(): MqttConfigurator = MqttConfigurator()
    }
}