package com.iot.control.infrastructure

import android.util.Log
import com.iot.control.infrastructure.repository.TimerRepository
import com.iot.control.model.Timer as MyTimer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TimerManager @Inject constructor(
    private val timerRepository: TimerRepository,
    private val scriptManager: ScriptManager
) {

    private val timers: MutableMap<UUID, TimerTask> = mutableMapOf()
    private val scheduler: Timer = Timer()

    fun has(id: UUID) = timers.containsKey(id)

    fun init() {
        MainScope().launch {
            val timers = timerRepository.getStartedOnBoot()
            for(timer in timers) {
                set(timer)
            }
        }
    }

    fun stop() {
        try {
            scheduler.cancel()
            scheduler.purge()
        } catch (_: Throwable) {}
    }

    fun toggleTimer(timer: MyTimer): Boolean {
        return if(timers.containsKey(timer.id)) {
            delete(timer)
            false
        } else {
            set(timer)
            true
        }
    }

    fun set(timer: MyTimer) {
        Log.d("TimerManager", "Try set timer")
        if(timers.containsKey(timer.id)) return

        val task = object : TimerTask() {
            override fun run() {
                Log.d("TimerManager", "Timer awake")
                scriptManager.resolveTimerEvent(timer)
                if(!timer.repeat) delete(timer)
            }
        }

        setTimerFor(timer, task)
        timers[timer.id] = task
    }

    fun delete(timer: MyTimer) {
        if(timers.containsKey(timer.id)) {
            val task = timers[timer.id]
            task?.cancel()
            timers.remove(timer.id)
            scheduler.purge()
        }
    }

    private fun setTimerFor(timer: MyTimer, action: TimerTask) {
        if(timer.date != null) {
            if(timer.repeat)
                scheduler.schedule(action, timer.date, timer.interval)
            else
                scheduler.schedule(action, timer.date)
        } else if(timer.repeat)
            scheduler.schedule(action, 1000, timer.interval)
    }
}