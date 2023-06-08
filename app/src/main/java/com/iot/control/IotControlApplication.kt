package com.iot.control

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.iot.control.infrastructure.ApplicationDatabase
import com.iot.control.infrastructure.DbContext
import com.iot.control.infrastructure.repository.CommandRepository
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DashboardRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.EventRepository
import com.iot.control.infrastructure.repository.LogRepository
import com.iot.control.infrastructure.repository.ScriptRepository
import com.iot.control.infrastructure.repository.TimerRepository
import com.iot.control.infrastructure.repository.WidgetRepository
import com.iot.control.infrastructure.sms.SmsSender
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class IotControlApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("IotControlApplication", "onCreate")
        //DbContext.initialize(this)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton fun provideSmsSender(@ApplicationContext context: Context): SmsSender = SmsSender.create(context) { context }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton fun provideDatabase(@ApplicationContext context: Context): ApplicationDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ApplicationDatabase::class.java,
            DbContext.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton fun provideConnectionRepository(database: ApplicationDatabase): ConnectionRepository {
        return ConnectionRepository(database.connectionDao())
    }

    @Provides
    @Singleton fun provideDeviceRepository(database: ApplicationDatabase): DeviceRepository {
        return DeviceRepository(database.deviceDao())
    }
    @Provides
    @Singleton fun provideCommandRepository(database: ApplicationDatabase): CommandRepository {
        return CommandRepository(database.commandDao())
    }

    @Provides
    @Singleton fun provideEventRepository(database: ApplicationDatabase): EventRepository {
        return EventRepository(database.eventDao())
    }

    @Provides
    @Singleton fun provideScriptRepository(database: ApplicationDatabase): ScriptRepository {
        return ScriptRepository((database.scriptDao()))
    }

    @Provides
    @Singleton fun provideTimerRepository(database: ApplicationDatabase): TimerRepository {
        return TimerRepository(database.timerDao())
    }

    @Provides
    @Singleton fun provideWidgetRepository(database: ApplicationDatabase): WidgetRepository {
        return WidgetRepository(database.widgetDao())
    }

    @Provides
    @Singleton fun provideLogRepository(database: ApplicationDatabase): LogRepository {
        return LogRepository((database.logDao()))
    }

    @Provides
    @Singleton fun provideDashboardRepository(database: ApplicationDatabase): DashboardRepository {
        return DashboardRepository(database.dashboardDao())
    }
}