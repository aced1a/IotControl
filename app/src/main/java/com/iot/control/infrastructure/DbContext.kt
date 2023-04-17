package com.iot.control.infrastructure

import android.content.Context
import androidx.room.Room
import com.iot.control.infrastructure.repository.*

class DbContext private constructor(context: Context) {

    private val database: ApplicationDatabase = Room.databaseBuilder(
        context.applicationContext,
        ApplicationDatabase::class.java,
        DATABASE_NAME
    ).build()

    val deviceRepository: DeviceRepository
        get() = DeviceRepository(database.deviceDao())

    val connectionRepository: ConnectionRepository
        get() = ConnectionRepository(database.connectionDao())

    val commandRepository: CommandRepository
        get() = CommandRepository(database.commandDao())

    val eventRepository: EventRepository
        get() = EventRepository(database.eventDao())

    val scriptRepository: ScriptRepository
        get() = ScriptRepository(database.scriptDao())


    companion object {
        const val TAG = "DbContext"
        const val DATABASE_NAME = "IotDb"

        private var INSTANCE: DbContext? = null

        fun initialize(context: Context) {
            INSTANCE = INSTANCE ?: DbContext(context)
        }

        fun get(): DbContext {
            return INSTANCE ?: throw java.lang.IllegalStateException("DbContext must be initialized")
        }
    }
}