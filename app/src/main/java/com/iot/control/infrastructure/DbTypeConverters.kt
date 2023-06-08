package com.iot.control.infrastructure

import androidx.room.TypeConverter
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.CommandMode
import com.iot.control.model.enums.ConnectionMode
import com.iot.control.model.enums.ConnectionType
import com.iot.control.model.enums.WidgetType
import com.iot.control.model.enums.EventType
import java.util.*

class DbTypeConverters {
    @TypeConverter
    fun toUUID(uuid: String?): UUID? = if(uuid != null) UUID.fromString(uuid) else null

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toDeviceType(type: Int): WidgetType = WidgetType.fromInt(type)

    @TypeConverter
    fun fromDeviceType(type: WidgetType): Int = type.ordinal

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
    fun fromDate(date: Date?): Long = date?.time ?: 0L

    @TypeConverter
    fun toDate(time: Long): Date? = if(time == 0L) null else Date(time)

    @TypeConverter
    fun fromConnectionMode(mode: ConnectionMode): Int = mode.ordinal

    @TypeConverter
    fun toConnectionMode(mode: Int): ConnectionMode = ConnectionMode.fromInt(mode)

    @TypeConverter
    fun fromCommandMode(mode: CommandMode): Int = mode.ordinal

    @TypeConverter
    fun toCommandMode(mode: Int): CommandMode = CommandMode.fromInt(mode)
}