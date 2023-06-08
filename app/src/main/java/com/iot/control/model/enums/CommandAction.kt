package com.iot.control.model.enums

import com.iot.control.R

enum class CommandAction(val view: Int) {
    Get(R.string.sync_command),
    On(R.string.on_command),
    Off(R.string.off_command),
    Set(R.string.set_command),
    Send(R.string.set_command);

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value } ?: throw java.lang.IllegalStateException("Command action wrong value")
    }
}