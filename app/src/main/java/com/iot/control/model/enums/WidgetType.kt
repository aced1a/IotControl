package com.iot.control.model.enums

import com.iot.control.R

enum class WidgetType(val nameId: Int, val icon: Int) {
    Button(R.string.button_type_label, R.drawable.baseline_radio_button_checked_24),
    State(R.string.state_type_label, R.drawable.baseline_lightbulb_48),
    Value(R.string.value_type_label, R.drawable.baseline_pin_48),
    Color(R.string.color_type_label, R.drawable.baseline_color_lens_24),
    Switch(R.string.switch_type_label, R.drawable.baseline_toggle_on_48);
    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value } ?: throw IllegalStateException("Null pointer")
    }
}