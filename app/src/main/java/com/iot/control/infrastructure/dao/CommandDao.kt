package com.iot.control.infrastructure.dao

import androidx.room.*
import com.iot.control.model.Command
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ConnectionType
import java.util.UUID

@Dao
interface CommandDao {
    @Query("SELECT * FROM command WHERE id = (:id)")
    suspend fun getById(id: UUID): Command?

    @Query("SELECT * FROM command WHERE device_id = (:id) AND type = (:type)")
    suspend fun getByIdAndType(id: UUID, type: ConnectionType): List<Command>

    @Query("SELECT * FROM command WHERE device_id = (:id) AND [action] = (:action) AND type = (:type)")
    suspend fun getByDeviceEvent(id: UUID, action: CommandAction, type: ConnectionType): Command?

    @Query("SELECT * FROM command WHERE connection_id = (:connectionId) AND topic = (:topic) and `action`=(:action)")
    suspend fun getByConnectionAndTopic(connectionId: UUID, topic: String, action: CommandAction): List<Command>

    @Query("SELECT * FROM command WHERE connection_id = (:id)")
    suspend fun getByConnectionId(id: UUID): List<Command>

    @Insert
    suspend fun add(command: Command)

    @Update
    suspend fun update(command: Command)

    @Delete
    suspend fun delete(command: Command)
}