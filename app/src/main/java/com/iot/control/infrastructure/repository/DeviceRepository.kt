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

    suspend fun updateByEvent(id: UUID, event: Event, value: String?) {
        val device = getDeviceById(id)

        if(device != null) {
            val value = when(event.type) {
                EventType.On -> Device.ON
                EventType.Off -> Device.OFF
                EventType.Inverse -> if(device.value == Device.ON) Device.OFF else Device.ON
                EventType.Set -> value ?:  event.payload
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
                CommandAction.On -> Device.ON
                CommandAction.Off -> Device.OFF
                CommandAction.Set -> value ?: ""
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