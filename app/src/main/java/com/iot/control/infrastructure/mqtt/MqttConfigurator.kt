package com.iot.control.infrastructure.mqtt

class MqttConfigurator {
    fun address(address: String, port: Int): MqttConfigurator {
        return this
    }
    fun mqtt(version: MqttClient.Version): MqttConfigurator {
        return this
    }
    fun ssl(on: Boolean): MqttConfigurator {
        return this
    }
    fun auth(username: String?, password: String?): MqttConfigurator {
        return this
    }
    fun build():MqttClient {
        return MqttClient()
    }
}

