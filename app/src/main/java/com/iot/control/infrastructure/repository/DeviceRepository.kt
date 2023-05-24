package com.iot.control.infrastructure.repository

import androidx.annotation.WorkerThread
import com.iot.control.infrastructure.dao.DeviceDao
import com.iot.control.model.Command
import com.iot.control.model.Device
import com.iot.control.model.Event
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.EventType
import java.util.UUID

class DeviceRepository(private val dao: DeviceDao) {

    suspend fun getDeviceById(id: UUID): Device? = dao.getDeviceById(id)

    suspend fun getAll() = dao.getAll()

    fun getByConnectionId(id: UUID) = dao.getByConnectionId(id)

    fun getByDisplayableState(isDisplayable: Boolean) = dao.getByDisplayableState(isDisplayable)

    suspend fun updateByEvent(id: UUID, event: Event) {
        val device = getDeviceById(id)

        if(device != null) {
            val value = when(event.type) {
                EventType.ON -> "ON"
                EventType.OFF -> "OFF"
                EventType.INVERSE -> if(device.value == "ON") "OFF" else "ON"
                EventType.SET -> event.payload
                else -> return
            }
            update(device.copy(value = value))
        }
    }

    suspend fun deleteWidows() {
        dao.deleteWidows()
//        for(device in dao.getWidows()) delete(device)
    }

    suspend fun updateByCommand(id: UUID, command: Command, value: String? = null) {
        val device = getDeviceById(id)
        if(device != null) {
            val newValue = when(command.action) {
                CommandAction.ON -> "ON"
                CommandAction.OFF -> "OFF"
                CommandAction.SET -> value ?: ""
                else -> return
            }

            update(device.copy(value = newValue))
        }
    }

    suspend fun add(device: Device) {
        dao.add(device)
    }
    suspend fun update(device: Device) {
        dao.update(device)
    }

    suspend fun delete(device: Device) {
        dao.delete(device)
    }
}