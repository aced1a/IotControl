package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.EventType
import com.iot.control.model.enums.ScriptGuard
import java.util.UUID

@Entity(foreignKeys = [
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Timer::class,  parentColumns = ["id"], childColumns = ["timer_id"]),
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Device::class, parentColumns = ["id"], childColumns = ["device_id"]),
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Device::class, parentColumns = ["id"], childColumns = ["source_id"])
])
data class Script(@PrimaryKey val id: UUID = UUID.randomUUID(),
                  val guard: ScriptGuard = ScriptGuard.No,
                  @ColumnInfo(name="guard_val") val guardValue: String? = null,
                  @ColumnInfo(name="action") val commandAction : CommandAction,
                  @ColumnInfo(name="action_val") val actionValue: String? = null,
                  @ColumnInfo(name="event_type") val eventType: EventType = EventType.On,

                  @ColumnInfo(name="timer_id", index=true) val timerId: UUID?,
                  @ColumnInfo(name="source_id", index=true) val sourceId: UUID?,
                  @ColumnInfo(name="device_id", index=true) val deviceId: UUID) {
}

