package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ConnectionType
import java.util.UUID

@Entity
data class Command(@PrimaryKey val id: UUID = UUID.randomUUID(),
                   var topic: String="",
                   var payload: String="",
                   var action: CommandAction = CommandAction.ON,
                   var type: ConnectionType = ConnectionType.MQTT,
                   @ColumnInfo(name="connection_id") var connectionId: UUID = UUID.randomUUID(),
                   @ColumnInfo(name="device_id") val deviceId: UUID=UUID.randomUUID(),
                   @ColumnInfo(name="is_json") var isJson: Boolean = false,
                   @ColumnInfo(name="data_field") val dataField: String? = null)
