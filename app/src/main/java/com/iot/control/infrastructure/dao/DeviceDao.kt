package com.iot.control.infrastructure.dao

import androidx.room.*
import com.iot.control.model.Device
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DeviceDao {

    @Query("SELECT * FROM device")
    suspend fun getAll(): List<Device>

    @Query("SELECT * FROM device WHERE id = (:id)")
    suspend fun getDeviceById(id: UUID): Device?

    @Query("SELECT * FROM device WHERE mqtt_id = (:id) OR sms_id = (:id)")
    fun getByConnectionId(id: UUID): Flow<List<Device>>

    @Query("SELECT * FROM device WHERE is_displayable = (:isDisplayable)")
    fun getByDisplayableState(isDisplayable: Boolean): Flow<List<Device>>

    @Query("SELECT * FROM device WHERE mqtt_id IS NULL AND sms_id IS NULL")
    suspend fun getWidows(): List<Device>

    @Query("DELETE FROM device WHERE mqtt_id IS NULL AND sms_id IS NULL")
    suspend fun deleteWidows()

    @Insert
    suspend fun add(device: Device)

    @Update
    suspend fun update(device: Device)

    @Delete
    suspend fun delete(device: Device)
}