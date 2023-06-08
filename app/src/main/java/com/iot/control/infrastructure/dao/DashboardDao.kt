package com.iot.control.infrastructure.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iot.control.model.Dashboard
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    @Query("SELECT * FROM dashboard ORDER BY `order`")
    fun getAll(): Flow<List<Dashboard>>

    @Insert
    suspend fun add(dashboard: Dashboard)

    @Update
    suspend fun update(dashboard: Dashboard)

    @Delete
    suspend fun delete(dashboard: Dashboard)
}