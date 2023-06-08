package com.iot.control.infrastructure.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.iot.control.model.LogMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Query("SELECT * FROM logmessage ORDER BY date DESC")
    fun getAll(): Flow<List<LogMessage>>

    @Query("DELETE FROM logmessage")
    suspend fun deleteAll(): Unit

    @Insert
    suspend fun add(message: LogMessage)

    @Delete
    suspend fun delete(message: LogMessage)
}