package com.iot.control.infrastructure.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iot.control.model.Timer
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timer")
    fun getAll(): Flow<List<Timer>>

    @Query("SELECT * FROM timer WHERE boot_init = 1")
    suspend fun getStartedOnBoot(): List<Timer>

    @Insert
    suspend fun add(timer: Timer)

    @Update
    suspend fun update(timer: Timer)

    @Delete
    suspend fun delete(timer: Timer)
}