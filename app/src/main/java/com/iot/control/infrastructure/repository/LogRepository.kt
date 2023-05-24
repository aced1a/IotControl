package com.iot.control.infrastructure.repository

import com.iot.control.infrastructure.dao.LogDao
import com.iot.control.model.LogMessage
import kotlinx.coroutines.flow.Flow


class LogRepository(val dao: LogDao) {

    fun getAll(): Flow<List<LogMessage>> = dao.getAll()

    suspend fun add(message: LogMessage) = dao.add(message)

    suspend fun delete(message: LogMessage) = dao.delete(message)

    fun deleteAll() {}
}
