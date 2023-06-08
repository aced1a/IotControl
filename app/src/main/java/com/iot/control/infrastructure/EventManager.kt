package com.iot.control.infrastructure

import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.EventRepository
import com.iot.control.infrastructure.repository.LogRepository
import com.iot.control.infrastructure.sms.CommandRecord
import com.iot.control.infrastructure.sms.SmsParser
import com.iot.control.infrastructure.utils.compare
import com.iot.control.infrastructure.utils.getPayload

import com.iot.control.infrastructure.utils.tryParse
import com.iot.control.model.Command
import com.iot.control.model.Event
import com.iot.control.model.LogMessage
import com.iot.control.model.enums.CommandMode
import com.iot.control.model.enums.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class EventWithValue(
    val event: Event,
    val value: String?
)

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

    private val _events = MutableStateFlow<EventWithValue?>(null)
    val events: StateFlow<EventWithValue?> = _events.asStateFlow()
    private val smsParser: SmsParser = SmsParser()

    suspend fun resolveMqttEvent(address: String, topic: String, payload: String) {
        val connection = connectionRepository.getByAddress(address)

        logRepository.add(LogMessage(address=address, name = "MQTT", topic = topic, message = payload))

        if(connection != null) {
            val json = tryParse(payload)

            val events = if(json != null)
                             getJsonEvents(json, connection.id, topic)
                         else {
                             eventRepository.getByPayload(connection.id, topic, payload).plus(
                                 eventRepository.getSetByTopic(connection.id, topic)
                             )
                         }

            processEvents(events, if(json != null) null else payload)
        }
    }

    private suspend fun getJsonEvents(actual: JSONObject, connectionId: UUID, topic: String): List<Event> {

        val result = mutableListOf<Event>()
        val events = eventRepository.getByMqttEventJson(connectionId, topic)

        for(event in events) {

            if(compare(event.payload, actual, event.dataField)) {
                val value = getPayload(actual, event.dataField)
                result.add(
                    if(value != null)
                        event.copy(payload = actual.getString(event.dataField))
                    else
                        event
                )
            }
        }

        return result
    }

    suspend fun resolveSmsEvent(number: String, message: String, onParseFailed: () -> CommandRecord?) {
        logRepository.add(LogMessage(address=number, name = "SMS", message = message))

        val connection = connectionRepository.getByAddress(number) ?: return

        val success = if(connection.parser == null)
            findByMessage(connection.id, message)
        else
            findWithParser(connection.id, connection.parser, message)

        if(!success) {
            val command = onParseFailed()?.command ?: return
            logRepository.add(LogMessage(address=number, name = "SMS Answer", message = message))

            processAsAnswer(command, message)
        }
    }

    private suspend fun findWithParser(connectionId: UUID, parser: String, message: String): Boolean {
        val data = smsParser.parse(parser, message)
        if(data.containsKey("address") && (data.containsKey("payload") || data.containsKey("value"))) {

            val value = data["value"]
            val payload = data["payload"]

            val events = if(payload != null)
                eventRepository.getByPayload(connectionId, data["address"]!!, payload)
            else
                eventRepository.getSetByTopic(connectionId, data["address"]!!)

            processEvents(events, value)
            return true
        }
        return false
//        return findByMessage(connectionId, message)
    }

    private suspend fun findByMessage(connectionId: UUID, message: String): Boolean {
        val events = eventRepository.getByPayload(connectionId, message)

        processEvents(events, message)

        return events.isNotEmpty()
    }

    private suspend fun processAsAnswer(command: Command, message: String) {
        when(command.mode) {
            CommandMode.Confirm -> deviceRepository.updateByCommand(command.id, command, message)
            CommandMode.Match -> processEvents(eventRepository.getEventForDevice(command.deviceId, message), message)
            CommandMode.Set -> update(Event.getDefaultEvent().copy(type = EventType.Set, payload = message), message)
            else -> return
        }
    }

    private suspend fun processEvents(events: List<Event>, value: String?) {
        for (event in events) {
            notificationManager.notify(event, deviceRepository)
            update(event, value)
            _events.update { EventWithValue(event, value) }
        }
    }

    private suspend fun update(event: Event, value: String?) {

        logRepository.add(LogMessage(resolved = true, name = event.type.name, topic = event.topic , message = event.payload))

        deviceRepository.updateByEvent(event.deviceId, event, value)
    }

}