package com.iot.control.infrastructure

import android.util.Log
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.EventRepository
import com.iot.control.infrastructure.repository.LogRepository
import com.iot.control.infrastructure.sms.CommandRecord
import com.iot.control.infrastructure.utils.compare

import com.iot.control.infrastructure.utils.tryParse
import com.iot.control.model.Event
import com.iot.control.model.LogMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventManager @Inject constructor(
    private val notificationManager: NotificationManager,
    private val connectionRepository: ConnectionRepository,
    private val deviceRepository: DeviceRepository,
    private val eventRepository: EventRepository,
    private val logRepository: LogRepository
) {
    companion object {
        const val TAG= "EventManager"
    }

    private val _events = MutableStateFlow<Event?>(null)
    val events: StateFlow<Event?> = _events.asStateFlow()

    suspend fun resolveMqttEvent(address: String, topic: String, payload: String) {
        val connection = connectionRepository.getByAddress(address)
        Log.d(TAG, "Trying resolve mqtt event ($address, $topic, $payload): $connection")

        if(connection != null) {
            val json = tryParse(payload)

            logRepository.add(LogMessage(address=address, topic = topic, message = payload, success = true))

            val events = if(json != null)
                             getJsonEvents(json, connection.id, topic)
                         else
                            eventRepository.getByMqttEventPayload(connection.id, topic, payload)

            processEvents(events)
        } else {
            logRepository.add(LogMessage(address=address, topic = topic, message = payload, success = false))
        }

    }

    private suspend fun getJsonEvents(actual: JSONObject, connectionId: UUID, topic: String): List<Event> {
        Log.d(TAG, "Try find json events")
        val result = mutableListOf<Event>()
        val events = eventRepository.getByMqttEventJson(connectionId, topic)

        for(event in events) {
            val excepted = tryParse(event.payload)

            if(excepted != null && compare(excepted, actual, event.dataField)) {
                result.add(
                    if(event.dataField != null && actual.has(event.dataField))
                        event.copy(payload = actual.getString(event.dataField))
                    else
                        event
                )
            }
        }

        return result
    }

    suspend fun resolveSmsEvent(number: String, message: String, onParseFailed: () -> CommandRecord?) {
        val connection = connectionRepository.getByAddress(number) ?: return



    }

    private suspend fun processEvents(events: List<Event>) {
        for (event in events) {
            notificationManager.notify(event, deviceRepository)
            update(event)
            _events.update { event }
        }
    }

    private suspend fun update(event: Event) {
        logRepository.add(LogMessage(resolved = true, address = event.type.name, topic = event.topic , message = event.payload))
        kotlinx.coroutines.MainScope().launch {
            deviceRepository.updateByEvent(event.deviceId, event)
        }
    }

}