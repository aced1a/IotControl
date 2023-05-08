package com.iot.control.infrastructure.repository

import com.iot.control.infrastructure.dao.EventDao
import com.iot.control.model.Event
import java.util.UUID

class EventRepository(private val dao: EventDao) {

    suspend fun getById(id: UUID) = dao.getById(id)

    suspend fun getByDeviceId(id: UUID) = dao.getByDeviceId(id)

    suspend fun getByMqttEventPayload(connectionId: UUID, topic: String, payload: String)
        = dao.getByMqttEventPayload(connectionId, topic, payload)

    suspend fun getByMqttEventJson(connectionId: UUID, topic: String)
        = dao.getByMqttEventJson(connectionId, topic)

    suspend fun getBySmsEvent(connectionId: UUID, address: String, payload: String)
        = dao.getBySmsEvent(connectionId, address, payload)

    suspend fun getByConnectionId(connectionId: UUID) = dao.getByConnectionId(connectionId)

    suspend fun getByConnectionIdAndDeviceID(connectionId: UUID, deviceId: UUID)
        = dao.getByConnectionIdAndDeviceId(connectionId, deviceId)

    fun getEventDto() = dao.getEventDto()

    suspend fun add(event: Event) = dao.add(event)
    suspend fun update(event: Event) = dao.update(event)
    suspend fun delete(event: Event) = dao.delete(event)
}