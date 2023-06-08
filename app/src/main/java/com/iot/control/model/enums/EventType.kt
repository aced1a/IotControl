package com.iot.control.model.enums

import com.iot.control.R

enum class EventType(val view: Int) {
    On(R.string.on_event),
    Off(R.string.off_event),
    Inverse(R.string.inverse_event),
    Set(R.string.set_event),
    Alert(R.string.notification_label);

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value } ?: throw java.lang.IllegalStateException("EventType wrong view value")
    }
}