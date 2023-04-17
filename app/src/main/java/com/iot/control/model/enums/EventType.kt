package com.iot.control.model.enums

import com.iot.control.R

enum class EventType(val view: Int) {
    ON(R.string.on_event),
    OFF(R.string.off_event),
    INVERSE(R.string.inverse_event),
    SET(R.string.set_event),
    CLICK(R.string.click_event),
    DCLICK(R.string.dclick_event);

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value } ?: throw java.lang.IllegalStateException("EventType wrong view value")
    }
}