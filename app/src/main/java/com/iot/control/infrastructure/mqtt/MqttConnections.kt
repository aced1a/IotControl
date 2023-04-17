package com.iot.control.infrastructure.mqtt

import com.iot.control.model.Connection

class MqttConnections {
    private val connections: MutableMap<String, MqttClient> = mutableMapOf()

    fun get(key: String) = connections[key]

    fun connect(connection: Connection) {
        if(connections.containsKey(connection.address)) return;

        val client = MqttClient.configure()
            .mqtt(MqttClient.Version.Mqtt5)
            .address(connection.address, connection.port)
            .ssl(connection.isSsl)
            .auth(connection.username, connection.password)
            .build()
    }
}