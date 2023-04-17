package com.iot.control.infrastructure.dao

import androidx.room.*
import com.iot.control.model.Connection
import com.iot.control.model.enums.ConnectionType
import kotlinx.coroutines.flow.Flow
import java.util.UUID


@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection")
    fun getAll(): Flow<List<Connection>>

    @Query("SELECT * FROM connection WHERE id = (:id)")
    suspend fun getById(id: UUID): Connection?

    @Query("SELECT * FROM connection WHERE address = (:address)")
    suspend fun getByAddress(address: String): Connection?

    @Query("SELECT * FROM connection WHERE type == (:type)")
    suspend fun getByConnectionType(type: ConnectionType): List<Connection>

    @Insert
    suspend fun add(connection: Connection)

    @Update
    suspend fun update(connection: Connection)

    @Delete
    suspend fun delete(connection: Connection)
}