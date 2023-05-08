package com.iot.control.infrastructure

import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.model.Device
import com.iot.control.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

data class Notification(val id: Int = 0, val title: String, val content: String)

@Singleton
class NotificationManager @Inject constructor() {

    private val _notifications = MutableStateFlow(Notification(title = "Application started", content = ""))
    val notifications: StateFlow<Notification> = _notifications.asStateFlow()

    fun notify(device: Device, event: Event) {
        //TODO string to resource id
        val notification = Notification(
            id = Random().nextInt(),
            title = "New event",
            content = "${device.name}: ${event.type.name}"
        )

        _notifications.update { notification }
    }

    fun notify(event: Event, rep: DeviceRepository) {
        kotlinx.coroutines.MainScope().launch {
            val device = rep.getDeviceById(event.deviceId) ?: return@launch

            notify(device, event)
        }
    }

}