package com.iot.control.infrastructure.repository

import com.iot.control.infrastructure.dao.EventDao
import com.iot.control.model.Event
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.EventType
import java.util.UUID

class EventRepository(private val dao: EventDao) {

    suspend fun getById(id: UUID) = dao.getById(id)

    suspend fun getByDeviceId(id: UUID) = dao.getByDeviceId(id)

    suspend fun getSetByTopic(connectionId: UUID, topic: String) = dao.getSetByTopic(connectionId, topic)

    suspend fun getByPayload(connectionId: UUID, topic: String, payload: String)
        = dao.getByAddressAndPayload(connectionId, topic, payload)

    suspend fun getByMqttEventJson(connectionId: UUID, topic: String)
        = dao.getByMqttEventJson(connectionId, topic)

    suspend fun getByPayload(connectionId: UUID, payload: String) = dao.getByPayload(connectionId, payload)

    suspend fun getEventForDevice(deviceId: UUID, payload: String) = dao.getEventForDevice(deviceId, payload)

    suspend fun getByConnectionId(connectionId: UUID) = dao.getByConnectionId(connectionId)

    suspend fun getByConnectionIdAndDeviceID(connectionId: UUID, deviceId: UUID)
        = dao.getByConnectionIdAndDeviceId(connectionId, deviceId)

    suspend fun getByConnectionAndTopic(connectionId: UUID, topic: String, type: EventType) = dao.getByConnectionAndTopic(connectionId, topic, type)

    fun getEventDto() = dao.getEventDto()

    suspend fun add(event: Event) = dao.add(event)
    suspend fun update(event: Event) = dao.update(event)
    suspend fun delete(event: Event) = dao.delete(event)
}