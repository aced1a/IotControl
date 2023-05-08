package com.iot.control.infrastructure.dao

import androidx.room.*
import com.iot.control.model.Event
import com.iot.control.model.EventDto
import com.iot.control.model.Script
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event WHERE id = (:id)")
    suspend fun getById(id: UUID): Event?

    @Query("SELECT * FROM event WHERE device_id = (:id)")
    suspend fun getByDeviceId(id: UUID): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND topic = (:topic) AND payload = (:payload)")
    suspend fun getByMqttEventPayload(connectionId: UUID, topic: String, payload: String): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND topic = (:topic) AND is_json = 1")
    suspend fun getByMqttEventJson(connectionId: UUID, topic: String): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND topic = (:address) AND payload = (:payload)")
    suspend fun getBySmsEvent(connectionId: UUID, address: String, payload: String): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId)")
    suspend fun getByConnectionId(connectionId: UUID): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND device_id = (:deviceId)")
    suspend fun getByConnectionIdAndDeviceId(connectionId: UUID, deviceId: UUID): List<Event>

    @Query("""
        SELECT * FROM (
	        SELECT event.id AS id, device.name AS name, event.type AS type FROM event
	        INNER JOIN device ON event.device_id = device.id) dto, script
        WHERE dto.id = script.event_id
    """)
    fun getEventDto(): Flow<Map<EventDto, List<Script>>>

    @Insert
    suspend fun add(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)
}