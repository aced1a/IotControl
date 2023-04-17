package com.iot.control.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ScriptGuard
import java.util.UUID

@Entity
data class Script(@PrimaryKey val id: UUID = UUID.randomUUID(),
                  val guard: ScriptGuard = ScriptGuard.No,
                  @ColumnInfo(name="guard_val") val guardValue: String? = null,
                  @ColumnInfo(name="action") val commandAction : CommandAction = CommandAction.ON,
                  @ColumnInfo(name="action_val") val actionValue: String? = null,
                  @ColumnInfo(name="event_id") val eventId: UUID = UUID.randomUUID(),
                  @ColumnInfo(name="device_id") val deviceId: UUID = UUID.randomUUID())

