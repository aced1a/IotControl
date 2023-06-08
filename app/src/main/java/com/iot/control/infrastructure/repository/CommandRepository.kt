package com.iot.control.infrastructure.repository

import com.iot.control.infrastructure.dao.CommandDao
import com.iot.control.model.Command
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ConnectionType
import java.util.UUID

class CommandRepository(private val dao: CommandDao) {
    suspend fun getByDeviceIdAndType(id: UUID, type: ConnectionType) = dao.getByIdAndType(id, type)

    suspend fun getByDeviceAction(id: UUID, action: CommandAction, type: ConnectionType) = dao.getByDeviceEvent(id, action, type)

    suspend fun getByConnectionId(id: UUID) = dao.getByConnectionId(id)

    suspend fun getByConnectionAndTopic(connectionId: UUID, topic: String, action: CommandAction) = dao.getByConnectionAndTopic(connectionId, topic, action)

    suspend fun getById(id: UUID) = dao.getById(id)

    suspend fun add(command: Command) = dao.add(command)

    suspend fun update(command: Command) = dao.update(command)

    suspend fun delete(command: Command) = dao.delete(command)

}