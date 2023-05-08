package com.iot.control.infrastructure.mqtt

import android.util.Log
import com.iot.control.infrastructure.DbContext
import com.iot.control.infrastructure.EventManager
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.EventRepository
import com.iot.control.model.Connection
import com.iot.control.model.enums.ConnectionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttConnections @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val eventRepository: EventRepository,
    private val eventManager: EventManager
) {
    companion object {
        const val TAG = "MqttConnections"
    }

    private val connections: MutableMap<String, MqttClient> = mutableMapOf()

    val running get() = connections.isNotEmpty()

    fun has(key: String) = connections.containsKey(key)
    fun get(key: String) = connections[key]

    suspend fun start() {
        Log.d(TAG, "Starting mqtt connections")
        for(connection in connectionRepository.getByType(ConnectionType.MQTT)) {
            val client = connect(connection)

            if(client != null)
                for(event in eventRepository.getByConnectionId(connection.id))
                    client.subscribe(event.topic)
        }
    }
    suspend fun stop() {
        Log.d(TAG, "Stopping mqtt connections")
        for(item in connections.values) item.disconnect()
        connections.clear()
    }

    fun connect(connection: Connection): MqttClient? {
        if(connections.containsKey(connection.address)) return get(connection.address);

        Log.d(TAG, "Try connect to ${connection.address}")

        val client = MqttClient.configure()
            .mqtt(MqttClient.Version.Mqtt5)
            .address(connection.address, connection.port)
            .ssl(connection.isSsl)
            .auth(connection.username, connection.password)
            .build(eventManager)

        connections[connection.address] = client
        client.connect()

        return client
    }

    fun disconnect(connection: Connection) {
        Log.d(TAG, "Disconnecting from ${connection.address}")
        val client = get(connection.address)

        if(client != null) {
            client.disconnect()
            connections.remove(connection.address)
        }
    }
}