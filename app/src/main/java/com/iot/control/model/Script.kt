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
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Event::class,  parentColumns = ["id"], childColumns = ["event_id"]),
    ForeignKey(onDelete = ForeignKey.CASCADE, entity = Device::class, parentColumns = ["id"], childColumns = ["device_id"]),
])
data class Script(@PrimaryKey val id: UUID = UUID.randomUUID(),
                  val guard: ScriptGuard = ScriptGuard.No,
                  @ColumnInfo(name="guard_val") val guardValue: String? = null,
                  @ColumnInfo(name="action") val commandAction : CommandAction,
                  @ColumnInfo(name="action_val") val actionValue: String? = null,

                  @ColumnInfo(name="timer_id", index=true) val timerId: UUID?,
                  @ColumnInfo(name="event_id", index=true) val eventId: UUID?,
                  @ColumnInfo(name="device_id", index=true) val deviceId: UUID) {
    companion object {
        fun getDefaultScript(): Script {
            return Script(
                guard = ScriptGuard.No,
                guardValue = null,
                commandAction = CommandAction.ON,
                actionValue = null,
                timerId = null,
                eventId = null,
                deviceId = UUID.randomUUID()
            )
        }
    }
}

