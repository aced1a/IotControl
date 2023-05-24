package com.iot.control.infrastructure

import com.iot.control.R
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

data class Notification(
    val id: Int = 0,
    val title: String? = null,
    val content: String? = null,
    val titleRes: Int = 0,
    val contentRes: Int = 0,
    val arg: String? = null
)

@Singleton
class NotificationManager @Inject constructor() {

    private val _notifications = MutableStateFlow(Notification(title = "Application started", content = ""))
    val notifications: StateFlow<Notification> = _notifications.asStateFlow()

    fun notify(title: Int, message: Int, arg: String?) {
        val notification = Notification(
            id = Random().nextInt(),
            titleRes = title,
            contentRes = message,
            arg = arg
        )

        _notifications.update { notification }
    }

    fun notify(title: String, message: String) {
        val notification = Notification(
            id = Random().nextInt(),
            title = title,
            content = message
        )

        _notifications.update { notification }
    }

    private fun notify(device: Device, event: Event) {
        //TODO string to resource id
        val notification = Notification(
            id = Random().nextInt(),
            titleRes = R.string.new_event_text,
            content = "${device.name}: ${event.type.name}"
        )

        _notifications.update { notification }
    }

    fun notify(event: Event, rep: DeviceRepository) {
        if(!event.notify) return
        if(event.notification != null) {
            notify("New event", event.notification)
            return
        }

        kotlinx.coroutines.MainScope().launch {
            val device = rep.getDeviceById(event.deviceId) ?: return@launch

            notify(device, event)
        }
    }

}