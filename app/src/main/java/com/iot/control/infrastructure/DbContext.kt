package com.iot.control.infrastructure

import android.content.Context
import androidx.room.Room
import com.iot.control.infrastructure.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

//@Singleton
class DbContext(val context: Context) {//@Inject constructor(@ApplicationContext context: Context) {

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

        fun provide(context: Context): DbContext {
            return DbContext(context)
        }

        fun initialize(context: Context) {
            INSTANCE = INSTANCE ?: DbContext(context)
        }

        fun get(): DbContext {
            return INSTANCE ?: throw java.lang.IllegalStateException("DbContext must be initialized")
        }
    }
}