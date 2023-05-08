package com.iot.control.infrastructure

import com.iot.control.model.Event
import com.iot.control.model.Timer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptManager @Inject constructor(
    private val commandManager: CommandManager,
    private val eventManager: EventManager
) {

    fun resolveEvent(event: Event) {

    }

    fun resolveTimerEvent(timer: Timer) {

    }
}