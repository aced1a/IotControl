package com.iot.control.model.enums

import com.iot.control.R

enum class ConnectionType(val labelId: Int) {
    HTTP(0),
    SMS(R.string.sms_label),
    MQTT(R.string.mqtt_label),
    LOCAL_MQTT(R.string.local_mqtt_label);

    fun value(): ConnectionType {
        return if(this == LOCAL_MQTT) MQTT
                else this
    }

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value } ?: throw java.lang.IllegalStateException("Wrong ConnectionType id value")
    }
}