package com.iot.control.infrastructure.repository

import com.iot.control.infrastructure.dao.ScriptDao
import com.iot.control.model.Script
import java.util.UUID

class ScriptRepository(val dao: ScriptDao) {
    suspend fun getAll() = dao.getAll()

    suspend fun getById(id: UUID) = dao.getById(id)

    suspend fun getByEventId(id: UUID) = dao.getByEventId(id)

    suspend fun getByTimerId(id: UUID) = dao.getByTimerId(id)

    suspend fun add(script: Script) = dao.add(script)

    suspend fun update(script: Script) = dao.update(script)

    suspend fun delete(script: Script) = dao.delete(script)

}