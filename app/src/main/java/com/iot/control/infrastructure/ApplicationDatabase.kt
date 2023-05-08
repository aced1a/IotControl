package com.iot.control.infrastructure

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iot.control.infrastructure.dao.*
import com.iot.control.model.*


@Database(entities = [Device::class, Connection::class, Command::class, Event::class, Script::class, Timer::class], version = 1)
@androidx.room.TypeConverters(DbTypeConverters::class)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun commandDao(): CommandDao
    abstract fun eventDao(): EventDao
    abstract fun scriptDao(): ScriptDao

    abstract fun timerDao(): TimerDao
}