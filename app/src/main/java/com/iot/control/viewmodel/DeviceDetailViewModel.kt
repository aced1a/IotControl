package com.iot.control.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.Notification
import com.iot.control.infrastructure.mqtt.MqttConnections
import com.iot.control.infrastructure.repository.CommandRepository
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.EventRepository
import com.iot.control.model.Command
import com.iot.control.model.Connection
import com.iot.control.model.Device
import com.iot.control.model.Event
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ConnectionType
import com.iot.control.model.enums.DeviceType
import com.iot.control.model.enums.EventType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class Marked<T>(val item: T, val isNew: Boolean)

data class DeviceDetailUiState(
    val name: String = "",
    val type: DeviceType = DeviceType.Light,

    val commands: List<Marked<Command>> = emptyList(),
    val events: List<Marked<Event>> = emptyList())

data class DialogUiState(
    val id: UUID? = null,
    val action: CommandAction = CommandAction.ON,
    val type: EventType = EventType.ON,
    val topic: String = "",
    val payload: String = "",
    val isJson: Boolean = false,
    val notify: Boolean = true,
    val notification: String = "",
    val dataField: String = "")


@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val deviceRepository: DeviceRepository,
    private val commandRepository: CommandRepository,
    private val eventRepository: EventRepository,
    private val mqttConnections: MqttConnections,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private var deviceId: UUID? = null
    private val connectionId: UUID = UUID.fromString(savedStateHandle.get<String>("connectionId"))

    private lateinit var connection: Connection
    private lateinit var device: Device

    private val _uiState = MutableStateFlow(DeviceDetailUiState())
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(DialogUiState())
    val dialogState: StateFlow<DialogUiState> = _dialogState.asStateFlow()

    init {
        val id = savedStateHandle.get<String>("deviceId")
        if(id != null) deviceId = UUID.fromString(id)

        loadDeviceDetail()
    }

    private fun loadDeviceDetail()
    {
        viewModelScope.launch {
            connection = connectionRepository.getById(connectionId) ?: throw java.lang.IllegalArgumentException("Wrong connection id")

            if(deviceId == null) {
                device = Device(
                    name = "",
                    value = "OFF",
                    type = DeviceType.Light,
                    idDisplayable = true,
                    mqttConnectionId = if(connection.type.value() == ConnectionType.MQTT) connection.id else null,
                    smsConnectionId =  if(connection.type == ConnectionType.SMS) connection.id else null
                )
                return@launch
            }

            val deviceDef = async { deviceRepository.getDeviceById(deviceId!!) }
            val commandsDef = async { commandRepository.getByDeviceIdAndType(deviceId!!, connection.type.value()).map { Marked(it, false) } }
            val eventsDef = async { eventRepository.getByConnectionIdAndDeviceID(connectionId, deviceId!!).map { Marked(it, false) } }

            device = deviceDef.await() ?: throw java.lang.IllegalArgumentException("Wrong device id")
            val commands = commandsDef.await()
            val events = eventsDef.await()

            _uiState.update {
                it.copy(
                    name = device.name,
                    type = device.type,
                    commands = commands,
                    events = events
                )
            }
        }
    }

    fun updateUiState(state: DeviceDetailUiState) {
        _uiState.update { state }
    }

    fun updateDialogState(state: DialogUiState) {
        _dialogState.update { state }
    }

    fun save()
    {
        device = device.copy(
            name = uiState.value.name,
            type = uiState.value.type,
            value = uiState.value.type.default
        )
        val client = if(connection.type == ConnectionType.MQTT) mqttConnections.get(connection.address) else null

        viewModelScope.launch {
            if (deviceId == null) deviceRepository.add(device)
            else deviceRepository.update(device)

            for(command in uiState.value.commands)
                if(command.isNew) commandRepository.add(command.item)
                else commandRepository.update(command.item)

            for(event in uiState.value.events) {
                if(event.isNew) eventRepository.add(event.item)
                else eventRepository.update(event.item)
                client?.subscribe(event.item.topic)
            }
        }
    }


    fun newCommand(action: CommandAction) {
        _dialogState.update {
            DialogUiState(action = action)
        }
    }

    fun editCommand(command: Command) {
        _dialogState.update {
            DialogUiState(
                id = command.id,
                action = command.action,
                topic = command.topic,
                payload = command.payload,
                isJson = command.isJson,
                dataField = command.dataField ?: ""
            )
        }
    }

    fun saveCommand() {
        val command = Command(
            id = dialogState.value.id ?: UUID.randomUUID(),
            action = dialogState.value.action,
            type = connection.type.value(),
            topic = dialogState.value.topic,
            payload = dialogState.value.payload,
            isJson = dialogState.value.isJson,
            dataField = dialogState.value.dataField.ifEmpty { null },
            isSync = false,
            deviceId = device.id,
            connectionId = connectionId)

        _uiState.update { ui ->
            ui.copy(
                commands = if(dialogState.value.id == null) ui.commands.plus(Marked(command, true))
                            else ui.commands.map { if(it.item.id == command.id ) Marked(command, it.isNew) else it }
            )
        }
    }


    fun newEvent(type: EventType) {
        _dialogState.update {
            DialogUiState(type = type)
        }
    }
    fun editEvent(event: Event) {
        _dialogState.update {
            DialogUiState(
                id = event.id,
                type = event.type,
                topic = event.topic,
                payload = event.payload,
                isJson = event.isJson,
                dataField = event.dataField ?: "",
                notify = event.notify,
                notification = event.notification ?: ""
            )
        }
    }

    fun saveEvent() {
        val event = Event(
            id = dialogState.value.id ?: UUID.randomUUID(),
            type = dialogState.value.type,
            topic = dialogState.value.topic,
            payload = dialogState.value.payload,
            isJson = dialogState.value.isJson,
            dataField = dialogState.value.dataField.ifEmpty { null },
            connectionId = connectionId,
            deviceId = device.id,
            isSync = false,
            notify = dialogState.value.notify,
            notification = dialogState.value.notification.ifEmpty { null }
        )
        _uiState.update { ui ->
            ui.copy(
                events = if(dialogState.value.id == null) ui.events.plus(Marked(event, true))
                else ui.events.map { if(it.item.id == event.id ) Marked(event, it.isNew) else it }
            )
        }
    }

    fun deleteCommand() {}
    fun deleteEvent() {}

    companion object {
        const val TAG = "DeviceDetailViewModel"
    }
}