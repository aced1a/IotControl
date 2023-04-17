package com.iot.control.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.DbContext
import com.iot.control.model.Command
import com.iot.control.model.Connection
import com.iot.control.model.Device
import com.iot.control.model.Event
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ConnectionType
import com.iot.control.model.enums.DeviceType
import com.iot.control.model.enums.EventType
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

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
    val dataField: String = "")


class DeviceDetailViewModel(private val deviceId: UUID?,
                            private val connectionId: UUID) : ViewModel() {

    private val connectionRep = DbContext.get().connectionRepository
    private val deviceRep = DbContext.get().deviceRepository
    private val commandRep = DbContext.get().commandRepository
    private val eventRep = DbContext.get().eventRepository

    private lateinit var connection: Connection
    private lateinit var device: Device


    private val _uiState = MutableStateFlow(DeviceDetailUiState())
    val uiState: StateFlow<DeviceDetailUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(DialogUiState())
    val dialogState: StateFlow<DialogUiState> = _dialogState.asStateFlow()


    init {
        loadDeviceDetail()
    }

    private fun loadDeviceDetail()
    {
        viewModelScope.launch {
            connection = connectionRep.getById(connectionId) ?: throw java.lang.IllegalArgumentException("Wrong connection id")

            if(deviceId == null) {
                device = Device(
                    mqttConnectionId = if(connection.type.value() == ConnectionType.MQTT) connection.id else null,
                    smsConnectionId =  if(connection.type == ConnectionType.SMS) connection.id else null
                )
                return@launch
            }

            val deviceDef = async { deviceRep.getDeviceById(deviceId) }
            val commandsDef = async { commandRep.getByDeviceIdAndType(deviceId, connection.type.value()).map { Marked(it, false) } }
            val eventsDef = async { eventRep.getByConnectionIdAndDeviceID(connectionId, deviceId).map { Marked(it, false) } }

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
        viewModelScope.launch {
            if (deviceId == null) deviceRep.add(device)
            else deviceRep.update(device)

            for(command in uiState.value.commands)
                if(command.isNew) commandRep.add(command.item)
                else commandRep.update(command.item)

            for(event in uiState.value.events)
                if(event.isNew) eventRep.add(event.item)
                else eventRep.update(event.item)
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
            type = connection.type,
            topic = dialogState.value.topic,
            payload = dialogState.value.payload,
            isJson = dialogState.value.isJson,
            dataField = dialogState.value.dataField,

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
                dataField = event.dataField
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
            dataField = dialogState.value.dataField,
        )
        _uiState.update { ui ->
            ui.copy(
                events = if(dialogState.value.id == null) ui.events.plus(Marked(event, true))
                else ui.events.map { if(it.item.id == event.id ) Marked(event, it.isNew) else it }
            )
        }
    }

    companion object {
        const val TAG = "DeviceDetailViewModel"
        fun provideFactory(deviceId: UUID?, connectionId: UUID): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DeviceDetailViewModel(deviceId, connectionId) as T
            }
        }
    }
}