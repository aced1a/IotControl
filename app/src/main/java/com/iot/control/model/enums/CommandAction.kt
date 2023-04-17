package com.iot.control.model.enums

import com.iot.control.R

enum class CommandAction(val view: Int) {
    SYNC(R.string.sync_command),
    ON(R.string.on_command),
    OFF(R.string.off_command),
    SET(R.string.set_command);

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.ordinal == value } ?: throw java.lang.IllegalStateException("Command action wrong view value")
    }
}