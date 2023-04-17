package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.iot.control.model.enums.ConnectionType
import java.util.UUID

@Entity
data class Connection(@PrimaryKey val id: UUID = UUID.randomUUID(),
                      @ColumnInfo(name="is_ssl") var isSsl: Boolean = false,
                      var name: String="",
                      var address: String="",
                      var port: Int = 8883,
                      var username: String?=null,
                      var password: String?=null,
                      var parser: String? = null,
                      var type: ConnectionType = ConnectionType.MQTT) {

    val isCredentials: Boolean
        get() = password != null
}
