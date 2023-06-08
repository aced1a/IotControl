package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iot.control.model.enums.ConnectionMode
import com.iot.control.model.enums.ConnectionType
import java.util.UUID

@Entity
data class Connection(@PrimaryKey val id: UUID = UUID.randomUUID(),
                      val name: String,
                      val address: String,
                      val port: Int,
                      @ColumnInfo(name="is_ssl") val isSsl: Boolean,
                      @ColumnInfo(name="certificate_path") val certificatePath: String?,
                      val expiredTime: Long,
                      val mode: ConnectionMode,
                      val username: String?,
                      val password: String?,
                      val parser: String?,
                      val type: ConnectionType) {

    val isCredentials: Boolean
        get() = password != null
}
