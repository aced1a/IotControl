package com.iot.control

import android.app.Application
import com.iot.control.infrastructure.DbContext

class IotControlApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DbContext.initialize(this)
    }
}