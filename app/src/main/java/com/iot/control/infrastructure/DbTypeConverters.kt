package com.iot.control.infrastructure

import androidx.room.TypeConverter
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ConnectionMode
import com.iot.control.model.enums.ConnectionType
import com.iot.control.model.enums.DeviceType
import com.iot.control.model.enums.EventType
import java.util.*

class DbTypeConverters {
    @TypeConverter
    fun toUUID(uuid: String?): UUID? = if(uuid != null) UUID.fromString(uuid) else null

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toDeviceType(type: Int): DeviceType = DeviceType.fromInt(type)

    @TypeConverter
    fun fromDeviceType(type: DeviceType): Int = type.ordinal

    @TypeConverter
    fun toCommandAction(action: Int): CommandAction = CommandAction.fromInt(action)

    @TypeConverter
    fun fromCommandAction(action: CommandAction): Int = action.ordinal

    @TypeConverter
    fun toEventType(event: Int): EventType = EventType.fromInt(event)

    @TypeConverter
    fun fromEventType(event: EventType): Int = event.ordinal

    @TypeConverter
    fun toConnectionType(type: Int): ConnectionType = ConnectionType.fromInt(type)

    @TypeConverter
    fun fromConnectionType(type: ConnectionType): Int = type.ordinal

    @TypeConverter
    fun fromDate(date: Date): Long = date.time

    @TypeConverter
    fun toDate(time: Long): Date = Date(time)

    @TypeConverter
    fun fromConnectionMode(mode: ConnectionMode): Int = mode.ordinal

    @TypeConverter
    fun toConnectionMode(mode: Int): ConnectionMode = ConnectionMode.fromInt(mode)
}