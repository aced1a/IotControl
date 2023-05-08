package com.iot.control.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.DbContext
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.EventRepository
import com.iot.control.infrastructure.repository.ScriptRepository
import com.iot.control.model.Device
import com.iot.control.model.Event
import com.iot.control.model.EventDto
import com.iot.control.model.Script
import com.iot.control.model.enums.CommandAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.UUID
import javax.inject.Inject


data class ScriptUiState(
    val selectedScript: UUID? = null,
    val scripts: Map<EventDto, List<Script>> = emptyMap(),
    val devices: List<Device> = emptyList(),
    val events: List<Event> = emptyList()
)

data class ScriptDialogUiState(
    val source: Device? = null,//Device.getDefaultDevice(),
    val event: Event? = null,//Event.getDefaultEvent(),
    val target: Device? = null,//Device.getDefaultDevice(),
    val command: CommandAction = CommandAction.ON
)

@HiltViewModel
class ScriptViewModel @Inject constructor(
    private val scriptRepository: ScriptRepository,
    private val eventRepository: EventRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScriptUiState())
    val uiState: StateFlow<ScriptUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(ScriptDialogUiState())
    val dialogState: StateFlow<ScriptDialogUiState> = _dialogState.asStateFlow()


    init {
        viewModelScope.launch {
            val devices = deviceRepository.getAll()
            _uiState.update { it.copy(devices = devices) }

            eventRepository.getEventDto().collect { scripts ->
                _uiState.update { it.copy(scripts = scripts) }
            }
        }
    }

    fun getDeviceName(deviceId: UUID): String {
        return uiState.value.devices.find { it.id == deviceId }?.name ?: "NF"
    }

    fun updateDialogState(state: ScriptDialogUiState) {
        _dialogState.update { state }
    }

    fun newScript() {
        _dialogState.update {
            _uiState.update { it.copy(selectedScript = null) }
            ScriptDialogUiState()
        }
    }
    fun editEventTriggeredScript(deviceId: UUID, script: Script) {
        viewModelScope.launch {
            val eventId = script.eventId ?: return@launch
            //TODO get from array or db?

            val device = uiState.value.devices.find { it.id == deviceId }
            val eventDef = async { eventRepository.getById(eventId) ?: throw IllegalArgumentException("Event doesn't exist") }
            val targetDef = async { deviceRepository.getDeviceById(script.deviceId) ?: throw IllegalArgumentException("Device doesn't exist") }

            val event = eventDef.await()
            val target = targetDef.await()

            _dialogState.update {
                it.copy(
                    source = device,
                    event = event,
                    target = target,
                    command = script.commandAction
                )
            }
            _uiState.update { it.copy(selectedScript = script.id) }
        }
    }

    fun saveScript() {
        if(
            dialogState.value.event == null ||
            dialogState.value.source == null ||
            dialogState.value.target == null
        )
            return

        val script = Script(
            id = uiState.value.selectedScript ?: UUID.randomUUID(),
            commandAction = dialogState.value.command,
            eventId = dialogState.value.event!!.id,
            deviceId = dialogState.value.target!!.id,
            timerId = null
        )
        viewModelScope.launch {
            if(_uiState.value.selectedScript != null)
                scriptRepository.update(script)
            else
                scriptRepository.add(script)
        }
    }

    fun loadEventsForDevice(id: UUID) {
        viewModelScope.launch {
            val events = eventRepository.getByDeviceId(id)
            _uiState.update {
                it.copy(events = events)
            }
        }
    }

    fun deleteScript(script: Script) {
        viewModelScope.launch {
            scriptRepository.delete(script)
        }
    }

    companion object {
        const val TAG = "ScriptViewModel"
    }
}