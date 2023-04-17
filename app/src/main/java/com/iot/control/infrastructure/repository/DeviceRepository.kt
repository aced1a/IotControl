package com.iot.control.infrastructure.repository

import androidx.annotation.WorkerThread
import com.iot.control.infrastructure.dao.DeviceDao
import com.iot.control.model.Device
import java.util.UUID

class DeviceRepository(private val dao: DeviceDao) {

    @WorkerThread
    suspend fun getDeviceById(id: UUID): Device? = dao.getDeviceById(id)

    suspend fun getAll() = dao.getAll()

    fun getByConnectionId(id: UUID) = dao.getByConnectionId(id)

    fun getByDisplayableState(isDisplayable: Boolean) = dao.getByDisplayableState(isDisplayable)

    suspend fun add(device: Device) {
        dao.add(device)
    }

    suspend fun update(device: Device) {
        dao.update(device)
    }
}