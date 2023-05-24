package com.iot.control.infrastructure

import android.util.Log
import com.iot.control.infrastructure.repository.ScriptRepository
import com.iot.control.model.Event
import com.iot.control.model.Timer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptManager @Inject constructor(
    private val commandManager: CommandManager,
    private val eventManager: EventManager,
    private val scriptRepository: ScriptRepository
) {

    init {
        MainScope().launch {
            eventManager.events.collect(::resolveEvent)
        }
    }

    private fun resolveEvent(event: Event?) {
        if(event != null) {
            Log.d("ScriptManager", "Resolve scripts for $event")
            processScripts(event)
        }
    }

    private fun processScripts(event: Event) {
        MainScope().launch {
            val scripts = scriptRepository.getByEventId(event.id)

            for(script in scripts) {
                //TODO guard
                commandManager.executeByAction(script.deviceId, script.commandAction)
            }
        }
    }

    fun resolveTimerEvent(timer: Timer) {
        MainScope().launch {
            val scripts = scriptRepository.getByTimerId(timer.id)

            for(script in scripts) {
                commandManager.executeByAction(script.deviceId, script.commandAction)
            }
        }
    }
}