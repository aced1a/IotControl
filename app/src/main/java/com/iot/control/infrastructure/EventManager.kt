package com.iot.control.infrastructure

import android.util.Log
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.EventRepository
import com.iot.control.infrastructure.repository.ScriptRepository
import com.iot.control.infrastructure.utils.compare

import com.iot.control.infrastructure.utils.tryParse
import com.iot.control.model.Event
import com.iot.control.model.Timer
import kotlinx.coroutines.MainScope
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
    private val scriptRepository: ScriptRepository
) {
    companion object {
        const val TAG= "EventManager"
    }

    suspend fun resolveMqttEvent(address: String, topic: String, payload: String) {
        Log.d(TAG, "Trying resolve mqtt event ($address, $topic, $payload)")
        val connection = connectionRepository.getByAddress(address)

        if(connection != null) {
            val json = tryParse(payload)

            val events = if(json != null)
                             getJsonEvents(json, connection.id, topic)
                         else
                            eventRepository.getByMqttEventPayload(connection.id, topic, payload)

            processEvents(events)
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
                    if(event.dataField != null && excepted.has(event.dataField))
                        event.copy(payload = excepted.getString(event.dataField))
                    else
                        event
                )
            }
        }

        return result
    }

    suspend fun resolveSmsEvent(number: String, message: String, onParseFailed: () -> Unit) {

    }

    private suspend fun processEvents(events: List<Event>) {
        for (event in events) {
            notificationManager.notify(event, deviceRepository)
            update(event)
        }
    }

    private suspend fun update(event: Event) {
        kotlinx.coroutines.MainScope().launch {
            deviceRepository.updateByEvent(event.deviceId, event)
        }
    }

}