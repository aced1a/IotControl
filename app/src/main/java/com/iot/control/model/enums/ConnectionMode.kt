package com.iot.control.model.enums

import com.iot.control.R

enum class ConnectionMode(val strId: Int) {
    Mqtt3(R.string.mqtt_v3), Mqtt5(R.string.mqtt_v5);

    companion object {
        fun mqttModes() = listOf(Mqtt3, Mqtt5)
        fun fromInt(mode: Int) = values().firstOrNull { it.ordinal == mode } ?: throw java.lang.IllegalStateException("Connection mode wrong value")
    }
}