package com.iot.control.infrastructure.mqtt

import com.iot.control.infrastructure.EventManager
import com.iot.control.infrastructure.NotificationManager
import com.iot.control.infrastructure.mqtt.client.Mqtt3Client
import com.iot.control.infrastructure.mqtt.client.Mqtt5Client
import com.iot.control.infrastructure.mqtt.client.MqttClient
import com.iot.control.model.enums.ConnectionMode
import java.util.UUID

class MqttConfigurator() {
    private var builder = com.hivemq.client.mqtt.MqttClient.builder()
                            .identifier("iot-control-${UUID.randomUUID()}")
                            .automaticReconnect()
                            .applyAutomaticReconnect()

    lateinit var address: String
    private var version = ConnectionMode.Mqtt5
    private var login: String? = null
    private var pwd: String? = null

    fun address(address: String, port: Int): MqttConfigurator {
        this.address = address
        builder = builder.serverHost(address).serverPort(port)
        return this
    }
    fun mqtt(version: ConnectionMode): MqttConfigurator {
        this.version = version
        return this
    }
    fun ssl(on: Boolean): MqttConfigurator {
        if(on) builder = builder.sslConfig().applySslConfig()
        return this
    }
    fun auth(username: String?, password: String?): MqttConfigurator {
        login = username
        pwd = password
        return this
    }
    fun build(
        manager: EventManager,
        notificationManager: NotificationManager,
        onDisconnected: () -> Unit
    ): MqttClient {
        return if(version == ConnectionMode.Mqtt3)
                buildMqtt3(manager, notificationManager, onDisconnected)
            else
                buildMqtt5(manager, notificationManager, onDisconnected)
    }

    private fun buildMqtt3(
        manager: EventManager,
        notificationManager: NotificationManager,
        onDisconnected: () -> Unit
    ): MqttClient {
        var client = builder.useMqttVersion3()
        if(login != null && pwd != null) {
            client = client.simpleAuth()
                    .username(login!!)
                    .password(pwd!!.toByteArray(Charsets.UTF_8))
                    .applySimpleAuth()
        }
        return Mqtt3Client(client, address, manager, notificationManager, onDisconnected)
    }

    private fun buildMqtt5(
        manager: EventManager,
        notificationManager: NotificationManager,
        onDisconnected: () -> Unit
    ): MqttClient {
        var client = builder.useMqttVersion5()
        if(login != null && pwd != null) {
            client = client.simpleAuth()
                .username(login!!)
                .password(pwd!!.toByteArray(Charsets.UTF_8))
                .applySimpleAuth()
        }
        return Mqtt5Client(client, address, manager, notificationManager, onDisconnected)
    }
}

