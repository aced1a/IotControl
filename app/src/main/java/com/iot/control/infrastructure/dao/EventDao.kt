package com.iot.control.infrastructure.dao

import androidx.room.*
import com.iot.control.model.Device
import com.iot.control.model.Event
import com.iot.control.model.EventDto
import com.iot.control.model.Script
import com.iot.control.model.enums.EventType
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event WHERE id = (:id)")
    suspend fun getById(id: UUID): Event?

    @Query("SELECT * FROM event WHERE device_id = (:id)")
    suspend fun getByDeviceId(id: UUID): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND topic = (:topic) AND type = 3")
    suspend fun getSetByTopic(connectionId: UUID, topic: String): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND topic = (:topic) AND payload = (:payload)")
    suspend fun getByAddressAndPayload(connectionId: UUID, topic: String, payload: String): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND payload = (:payload) AND only_value = 0")
    suspend fun getByPayload(connectionId: UUID, payload: String): List<Event>

    @Query("SELECT * FROM event WHERE device_id = (:deviceId) AND payload = (:payload) AND only_value = 1")
    suspend fun getEventForDevice(deviceId: UUID, payload: String): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND topic = (:topic) AND is_json = 1")
    suspend fun getByMqttEventJson(connectionId: UUID, topic: String): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId)")
    suspend fun getByConnectionId(connectionId: UUID): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND device_id = (:deviceId)")
    suspend fun getByConnectionIdAndDeviceId(connectionId: UUID, deviceId: UUID): List<Event>

    @Query("SELECT * FROM event WHERE connection_id = (:connectionId) AND topic = (:topic) AND type = (:type)")
    suspend fun getByConnectionAndTopic(connectionId: UUID, topic: String, type: EventType): List<Event>

    @Query("""SELECT * FROM
        (SELECT DISTINCT device.id AS deviceId, device.name AS name, script.event_type AS type
        FROM device INNER JOIN script ON script.source_id = device.id) dto JOIN script
         ON dto.deviceId = script.source_id AND dto.type = script.event_type
    """)
    fun getEventDto(): Flow<Map<EventDto, List<Script>>>

    @Insert
    suspend fun add(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)
}