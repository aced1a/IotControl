package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ConnectionType
import java.util.UUID

@Entity(foreignKeys = [
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Connection::class, parentColumns = ["id"], childColumns = ["connection_id"]),
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Device::class, parentColumns = ["id"], childColumns = ["device_id"]),
])
data class Command(@PrimaryKey val id: UUID = UUID.randomUUID(),
                   val topic: String,
                   val payload: String,
                   val action: CommandAction = CommandAction.ON,
                   val type: ConnectionType = ConnectionType.MQTT,
                   @ColumnInfo(name="is_sync") val isSync: Boolean,
                   @ColumnInfo(name="connection_id", index = true) val connectionId: UUID,
                   @ColumnInfo(name="device_id", index=true) val deviceId: UUID,
                   @ColumnInfo(name="is_json") var isJson: Boolean,
                   @ColumnInfo(name="data_field") val dataField: String?)
