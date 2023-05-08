package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.iot.control.model.enums.EventType
import java.util.UUID

@Entity(foreignKeys = [
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Connection::class, parentColumns = ["id"], childColumns = ["connection_id"]),
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Device::class, parentColumns = ["id"], childColumns = ["device_id"]),
])
data class Event(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 val topic: String,
                 val payload: String,
                 val type: EventType,
                 @ColumnInfo(name = "is_sync") val isSync: Boolean,
                 @ColumnInfo(name = "is_json") var isJson: Boolean,
                 @ColumnInfo(name = "data_field") var dataField: String?,
                 @ColumnInfo(name = "connection_id", index=true) val connectionId: UUID,
                 @ColumnInfo(name = "device_id", index=true) val deviceId: UUID) {

    companion object {
        fun getDefaultEvent(): Event {
            return Event(
                topic = "",
                payload = "",
                type = EventType.ON,
                isSync = false,
                isJson = false,
                dataField = null,
                connectionId = UUID.randomUUID(),
                deviceId = UUID.randomUUID()
            )
        }
    }
}
