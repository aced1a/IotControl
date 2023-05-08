package com.iot.control.infrastructure.repository

import android.util.Log
import com.iot.control.infrastructure.dao.ConnectionDao
import com.iot.control.infrastructure.dao.DeviceDao
import com.iot.control.model.Connection
import com.iot.control.model.enums.ConnectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ConnectionRepository(private val dao: ConnectionDao) {
    companion object {
        const val TAG = "ConnectionRepository"
    }

    suspend fun add(connection: Connection) {
        Log.d(TAG, "Create new connection: $connection in ${Thread.currentThread()}")
        dao.add(connection)
    }

    suspend fun update(connection: Connection) {
        Log.d(TAG, "Update connection: $connection in ${Thread.currentThread()}")
        dao.update(connection)
    }

    fun getAll(): Flow<List<Connection>> {
        Log.d(TAG, "Get all connections in ${Thread.currentThread()}")
        return dao.getAll()
    }

    suspend fun getById(id: UUID): Connection?  {
        Log.d(TAG, "Get connection by id: $id in ${Thread.currentThread()}")
        return dao.getById(id)
    }

    suspend fun getByType(type: ConnectionType): List<Connection> {
        return dao.getByConnectionType(type)
    }

    suspend fun getByAddress(address: String): Connection? {
        Log.d(TAG, "GetByAddress: $address in ${Thread.currentThread()}")
        return dao.getByAddress(address)
    }

    suspend fun delete(connection: Connection) {
        dao.delete(connection)
    }
}