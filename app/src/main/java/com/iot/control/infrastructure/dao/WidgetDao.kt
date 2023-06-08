package com.iot.control.infrastructure.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.iot.control.model.Widget
import com.iot.control.model.WidgetAndDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {

    @Transaction
    @Query("SELECT * FROM widget")
    fun getAll(): Flow<List<WidgetAndDevice>>

    @Insert
    suspend fun add(widget: Widget)

    @Update
    suspend fun update(widget: Widget)

    @Delete
    suspend fun delete(widget: Widget)
}