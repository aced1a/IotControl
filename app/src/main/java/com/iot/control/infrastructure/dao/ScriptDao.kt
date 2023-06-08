package com.iot.control.infrastructure.dao

import androidx.room.*
import com.iot.control.model.Script
import com.iot.control.model.enums.EventType
import java.util.UUID

@Dao
interface ScriptDao {

    @Query("SELECT * FROM script")
    suspend fun getAll(): List<Script>

    @Query("SELECT * FROM script WHERE id = (:id)")
    suspend fun getById(id: UUID): Script?

    @Query("SELECT * FROM script WHERE event_type = (:type) AND timer_id is NULL")
    suspend fun getByEventType(type: EventType): List<Script>

    @Query("SELECT * FROM script WHERE timer_id = (:id)")
    suspend fun getByTimerId(id: UUID): List<Script>

    @Insert
    suspend fun add(script: Script)

    @Update
    suspend fun update(script: Script)

    @Delete
    suspend fun delete(script: Script)
}