package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.SET_NULL
import androidx.room.PrimaryKey
import com.iot.control.model.enums.WidgetType
import java.util.UUID

@Entity(foreignKeys = [
    ForeignKey(onDelete = SET_NULL, entity = Connection::class, parentColumns = ["id"], childColumns = ["mqtt_id"]),
    ForeignKey(onDelete = SET_NULL, entity = Connection::class, parentColumns = ["id"], childColumns = ["sms_id"])
])
data class Device(@PrimaryKey val id: UUID = UUID.randomUUID(),
                  val name: String,
                  val value: String,
                  @ColumnInfo(name="is_displayable") val idDisplayable: Boolean,
                  @ColumnInfo(name="mqtt_id", index = true) val mqttConnectionId: UUID?,
                  @ColumnInfo(name="sms_id", index = true) val smsConnectionId: UUID?) {
    companion object {
        const val ON = "ON"
        const val OFF = "OFF"
    }
}
