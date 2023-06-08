package com.iot.control.infrastructure

import android.util.Log
import com.iot.control.infrastructure.repository.ScriptRepository
import com.iot.control.model.Event
import com.iot.control.model.Script
import com.iot.control.model.Timer
import com.iot.control.model.enums.ScriptGuard
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

    private fun resolveEvent(event: EventWithValue?) {
        if(event != null) {
            Log.d("ScriptManager", "Resolve scripts for $event")
            processScripts(event.event, event.value)
        }
    }

    private fun processScripts(event: Event, value: String?) {
        MainScope().launch {
            val scripts = scriptRepository.getByEventType(event.type)

            for(script in scripts) {
                if(checkGuard(script, value))
                    commandManager.executeByAction(script.deviceId, script.commandAction, script.actionValue)
            }
        }
    }

    private fun checkGuard(script: Script, value: String?): Boolean {
        if (script.guard == ScriptGuard.No) return true
        if (value == null || script.guardValue == null) return false

        return compareWithGuard(value, script.guard, script.guardValue)
    }

    private fun compareWithGuard(value: String, guard: ScriptGuard, guardValue: String): Boolean {
        return try {
            val num = value.toDouble()
            val numGuard = guardValue.toDouble()

            val result = num.compareTo(numGuard)
            compare(guard, result)

        } catch (e: Throwable) {
            val result = value.compareTo(guardValue)
            compare(guard, result)
        }
    }

    private fun compare(guard: ScriptGuard, result: Int): Boolean {
        return when(guard) {
            ScriptGuard.Equal -> result == 0
            ScriptGuard.Greater -> result > 0
            ScriptGuard.Less -> result < 0
            ScriptGuard.GreaterOrEqual -> result >= 0
            ScriptGuard.LessOrEqual -> result <= 0
            else -> true
        }
    }


    fun resolveTimerEvent(timer: Timer) {
        MainScope().launch {
            val scripts = scriptRepository.getByTimerId(timer.id)

            for(script in scripts) {
                commandManager.executeByAction(script.deviceId, script.commandAction, script.actionValue)
            }
        }
    }
}