package com.iot.control.infrastructure.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.iot.control.model.LogMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Dao
interface LogDao {

    @Query("SELECT * FROM logmessage")
    fun getAll(): Flow<List<LogMessage>>

    @Query("DELETE FROM logmessage")
    suspend fun deleteAll()

    @Inject
    suspend fun add(message: LogMessage)

    @Delete
    suspend fun delete(message: LogMessage)
}