package com.iot.control.model.enums

import com.iot.control.R

enum class DeviceType(val nameId: Int, val default: String) {
    Light(R.string.light_type_label, "off"),
    Button(R.string.button_type_label, ""),
    State(R.string.state_type_label, "none"),
    Switch(R.string.switch_type_label, "off");

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value } ?: throw IllegalStateException("Null pointer")
    }
}