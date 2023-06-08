package com.iot.control.model.enums

enum class CommandMode {
    Async, Match, Set, Confirm, Ignore;

    companion object {
        fun fromInt(value: Int) = CommandMode.values().firstOrNull { it.ordinal == value } ?: throw java.lang.IllegalStateException("Command mode wrong value")
        fun syncCommands() = listOf(Match, Set, Confirm, Ignore)
    }
}