package com.iot.control.infrastructure.mqtt

import io.moquette.broker.Server

class MqttBroker {
    private val clients: Set<String> = mutableSetOf()
    private val server: Server = Server()

    fun has(username: String?): Boolean = username != null && clients.contains(username)


    var running: Boolean = false
        private set

    fun start(){}
    fun stop(){}



    fun publish(topic: String, payload: String, callback: () -> Unit) {

    }
}