package com.iot.control.infrastructure

import com.iot.control.R
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.model.Command
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
    val arg: String? = null,

    val cmdId: String? = null,
    val mqtt: Boolean = true,
    val cmdValue: String? = null,
)

@Singleton
class NotificationManager @Inject constructor() {
    private val random = Random(System.currentTimeMillis())

    private val _notifications = MutableStateFlow(Notification(title = "Application started", content = ""))
    val notifications: StateFlow<Notification> = _notifications.asStateFlow()

    fun retryMqtt(command: Command, value: String?) {
        val notification = Notification(
            id = random.nextInt(),
            titleRes = R.string.failed_command,
            contentRes = R.string.failed_mqtt_command_text,
            arg = command.action.name,
            cmdId = command.id.toString(),
            mqtt = true,
            cmdValue = value
        )

        _notifications.update { notification }
    }

    fun retrySms(command: Command, value: String?) {
        val notification = Notification(
            id = random.nextInt(),
            titleRes = R.string.failed_command,
            contentRes = R.string.failed_sms_command_text,
            arg = command.action.name,
            cmdId = command.id.toString(),
            mqtt = false,
            cmdValue = value
        )

        _notifications.update { notification }
    }

    fun notify(title: Int, message: Int, arg: String?) {
        val notification = Notification(
            id = random.nextInt(),
            titleRes = title,
            contentRes = message,
            arg = arg
        )

        _notifications.update { notification }
    }

    fun notify(title: String, message: String) {
        val notification = Notification(
            id = random.nextInt(),
            title = title,
            content = message
        )

        _notifications.update { notification }
    }

    fun notify(event: Event, rep: DeviceRepository) {
        if(!event.notify) return
        if(event.notification != null) {
            _notifications.update {
                Notification(id = random.nextInt(), titleRes = R.string.new_event_text, content = event.notification)
            }
            return
        }

        kotlinx.coroutines.MainScope().launch {
            val device = rep.getDeviceById(event.deviceId) ?: return@launch

            notifyAboutEvent(device, event)
        }
    }

    private fun notifyAboutEvent(device: Device, event: Event) {
        val notification = Notification(
            id = Random().nextInt(),
            titleRes = R.string.new_event_text,
            content = "${device.name}: ${event.type.name}"
        )

        _notifications.update { notification }
    }

}