package com.iot.control.infrastructure

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
        scheduler.cancel()
        scheduler.purge()
    }

    fun toggleTimer(timer: MyTimer) {
        if(timers.containsKey(timer.id))
            delete(timer)
        else
            set(timer)
    }

    fun set(timer: MyTimer) {
        if(timers.containsKey(timer.id)) return

        val task = object : TimerTask() {
            override fun run() {
                scriptManager.resolveTimerEvent(timer)
            }
        }

        setTimerFor(timer, task)
    }

    fun delete(timer: MyTimer) {
        if(timers.containsKey(timer.id)) {
            val task = timers[timer.id]
            task?.cancel()
            timers.remove(timer.id)
        }
    }

    private fun setTimerFor(timer: MyTimer, action: TimerTask) {
        if(timer.repeat)
            scheduler.schedule(action, 1000, timer.interval * 60 * 1000L)
        else
            scheduler.schedule(action, timer.timer)
    }
}