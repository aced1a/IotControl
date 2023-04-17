package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iot.control.model.enums.EventType
import java.util.UUID

@Entity
data class Event(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var topic: String = "",
                 var payload: String = "",
                 var type: EventType = EventType.ON,
                 @ColumnInfo(name = "is_json") var isJson: Boolean = false,
                 @ColumnInfo(name = "data_field") var dataField: String = "",
                 @ColumnInfo(name = "connection_id") val connectionId: UUID = UUID.randomUUID(),
                 @ColumnInfo(name = "device_id") val deviceId: UUID = UUID.randomUUID())