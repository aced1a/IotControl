package com.iot.control.infrastructure.repository

import com.iot.control.infrastructure.dao.TimerDao
import com.iot.control.model.Timer

class TimerRepository(val dao: TimerDao) {

    fun getAll() = dao.getAll()

    suspend fun getStartedOnBoot() = dao.getStartedOnBoot()

    fun getTimersMap() = dao.getTimersMap()

    suspend fun add(timer: Timer) = dao.add(timer)

    suspend fun update(timer: Timer) = dao.update(timer)

    suspend fun delete(timer: Timer) = dao.delete(timer)
}