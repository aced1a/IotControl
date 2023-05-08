package com.iot.control.infrastructure.mqtt

import com.hivemq.client.mqtt.MqttClientBuilderBase
import com.iot.control.infrastructure.EventManager
import java.nio.charset.Charset
import java.util.UUID

class MqttConfigurator {
    private var builder = com.hivemq.client.mqtt.MqttClient.builder().identifier("iot-control-${UUID.randomUUID()}")
    lateinit var address: String
    private var version = MqttClient.Version.Mqtt5
    private var login: String? = null
    private var pwd: String? = null

    fun address(address: String, port: Int): MqttConfigurator {
        this.address = address
        builder = builder.serverHost(address).serverPort(port)
        return this
    }
    fun mqtt(version: MqttClient.Version): MqttConfigurator {
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
    fun build(manager: EventManager):MqttClient {
        return if(version == MqttClient.Version.Mqtt3)
                buildMqtt3(manager)
            else
                buildMqtt5(manager)
    }

    private fun buildMqtt3(manager: EventManager): MqttClient {
        var client = builder.useMqttVersion3()
        if(login != null && pwd != null) {
            client = client.simpleAuth()
                    .username(login!!)
                    .password(pwd!!.toByteArray(Charsets.UTF_8))
                    .applySimpleAuth()
        }
        return Mqtt3Client(client.build(), address, manager)
    }

    private fun buildMqtt5(manager: EventManager): MqttClient {
        var client = builder.useMqttVersion5()
        if(login != null && pwd != null) {
            client = client.simpleAuth()
                .username(login!!)
                .password(pwd!!.toByteArray(Charsets.UTF_8))
                .applySimpleAuth()
        }
        return Mqtt5Client(client.build(), address, manager)
    }
}

