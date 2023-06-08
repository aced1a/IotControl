package com.iot.control.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.TimerManager
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.EventRepository
import com.iot.control.infrastructure.repository.ScriptRepository
import com.iot.control.infrastructure.repository.TimerRepository
import com.iot.control.model.Device
import com.iot.control.model.EventDto
import com.iot.control.model.Script
import com.iot.control.model.Timer
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.EventType
import com.iot.control.model.enums.ScriptGuard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject


data class ScriptUiState(
    val selectedScript: UUID? = null,
    val timer: Timer? = null,
    val dto: EventDto? = null,
    val scripts: Map<EventDto, List<Script>> = emptyMap(),
    val devices: List<Device> = emptyList(),
    val timers: Map<Timer, List<Script>> = emptyMap(),
    val selectedTimer: UUID? = null
)

data class ScriptDialogUiState(
    val newTimer: Boolean = true,
    val source: Device? = null,
    val event: EventType = EventType.On,
    val target: Device? = null,
    val command: CommandAction = CommandAction.On,
    val guard: ScriptGuard = ScriptGuard.No,
    val guardValue: String = "",
    val actionValue: String = "",
    val useDate: Boolean = false,
    val repeat: Boolean = true,
    val onBoot: Boolean = false,

    val hours: Int = 12,
    val minutes: Int = 30,
    val day: Int = 1,
    val month: Int = 1,
    val year: Int = 2023,

    val hourInterval: Int = 0,
    val minuteInterval: Int = 1,
    val secondInterval: Int = 0,

    val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault()),
) {
    fun getInterval(): Long {
        return (((hourInterval * 60) + minuteInterval) * 60 + secondInterval) * 1000L
    }

    fun updateDate(calendar: Calendar): ScriptDialogUiState {
        return this.copy(
            calendar = calendar,
            hours = calendar.get(Calendar.HOUR_OF_DAY),
            minutes = calendar.get(Calendar.MINUTE),
            day = calendar.get(Calendar.DAY_OF_MONTH),
            month = calendar.get(Calendar.MONTH),
            year = calendar.get(Calendar.YEAR)
        )
    }
}

@HiltViewModel
class ScriptViewModel @Inject constructor(
    private val scriptRepository: ScriptRepository,
    private val eventRepository: EventRepository,
    private val deviceRepository: DeviceRepository,
    private val timerRepository: TimerRepository,
    private val timerManager: TimerManager
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

        viewModelScope.launch {
            timerRepository.getTimersMap().collect { timers ->
                _uiState.update { it.copy(timers = timers) }
            }
        }

        _dialogState.update { it.updateDate(it.calendar) }
    }


    fun timerIsActive(timer: Timer) = timerManager.has(timer.id)

    fun toggleTimer(timer: Timer): Boolean {
        return timerManager.toggleTimer(timer)
    }

    fun getDeviceName(deviceId: UUID): String {
        return uiState.value.devices.find { it.id == deviceId }?.name ?: "NF"
    }

    fun updateDialogState(state: ScriptDialogUiState) {
        _dialogState.update { state }
    }

    fun selectTimer(timer: Timer) {
        _uiState.update {
            it.copy(selectedTimer = timer.id, timer = timer)
        }
    }

    fun selectDto(dto: EventDto) {
        _uiState.update {
            it.copy(dto = dto)
        }
    }

    fun updateDate(time: Long?) {
        if(time == null) return

        _dialogState.value.calendar.timeInMillis = time
        updateTime(_dialogState.value.hours, _dialogState.value.minutes)
    }

    fun updateTime(hour: Int, minute: Int) {
        _dialogState.value.calendar.set(Calendar.HOUR_OF_DAY, hour)
        _dialogState.value.calendar.set(Calendar.MINUTE, minute)

        _dialogState.update {
            it.updateDate(_dialogState.value.calendar)
        }
    }

    fun newScript(timerId: UUID? = null, timer: Timer? = null) {
        val calendar = Calendar.getInstance()

        _uiState.update { it.copy(selectedScript = null, selectedTimer = timerId, timer = timer) }
        _dialogState.update {
            ScriptDialogUiState(
                newTimer = timerId == null
            ).updateDate(calendar)
        }
    }

    fun editEventTriggeredScript(deviceId: UUID, script: Script) {
        viewModelScope.launch {

            val device = uiState.value.devices.find { it.id == deviceId }
            val targetDef = async { deviceRepository.getDeviceById(script.deviceId) ?: throw IllegalArgumentException("Device doesn't exist") }

            val target = targetDef.await()

            _dialogState.update {
                it.copy(
                    newTimer = false,
                    source = device,
                    event = script.eventType,
                    target = target,
                    command = script.commandAction
                )
            }
            _uiState.update { it.copy(selectedScript = script.id, selectedTimer = null, timer = null) }
        }
    }

    fun editTimerTriggeredScript(timerId: UUID, script: Script) {
        viewModelScope.launch {

            val timer = uiState.value.timers.keys.find { it.id == timerId } ?: return@launch
            val targetDef = async { deviceRepository.getDeviceById(script.deviceId) ?: throw IllegalArgumentException("Device doesn't exist") }

            val target = targetDef.await()

            _dialogState.update {
                ScriptDialogUiState(
                    newTimer = false,
                    source = null,
                    target = target,
                    command = script.commandAction
                )
            }
            _uiState.update { it.copy(selectedScript = script.id, selectedTimer = timerId, timer = timer) }
        }
    }

    fun editTimer(timerId: UUID) {
        viewModelScope.launch {
            val timer = uiState.value.timers.keys.find { it.id == timerId } ?: return@launch

            val calendar = Calendar.getInstance()
            calendar.time = timer.date ?: Date()

            _dialogState.update {
                it.copy(
                    source = null,
                    onBoot = timer.initOnBoot,
                    useDate = timer.date != null,
                    repeat = timer.repeat,
                    hourInterval = (timer.interval / 3600000).toInt(),
                    minuteInterval = (timer.interval / 60000 % 60).toInt(),
                    secondInterval = (timer.interval % 60).toInt(),
                ).updateDate(calendar)
            }
            _uiState.update { it.copy(selectedTimer = timerId, timer = timer) }
        }
    }

    fun saveTimer() {
        val timer = Timer(
            uiState.value.selectedTimer ?: UUID.randomUUID(),
            date = if(dialogState.value.useDate) dialogState.value.calendar.time else null,
            repeat = dialogState.value.repeat,
            interval = dialogState.value.getInterval(),
            initOnBoot = dialogState.value.onBoot
        )

        viewModelScope.launch {
            if(uiState.value.selectedTimer == null)
                timerRepository.add(timer)
            else
                timerRepository.update(timer)
        }
    }

    fun saveTimerTriggeredScript() {
        if(dialogState.value.target == null) return
        val state = dialogState.value

        val timer = Timer(
            if(!state.newTimer) (uiState.value.selectedTimer ?: UUID.randomUUID()) else UUID.randomUUID(),
            date = if(state.useDate) state.calendar.time else null,
            repeat = state.repeat,
            interval = state.getInterval(),
            initOnBoot = state.onBoot
        )

        val script = Script(
            id = uiState.value.selectedScript ?: UUID.randomUUID(),
            commandAction = state.command,
            eventType = EventType.On,
            deviceId = state.target!!.id,
            sourceId = null,
            timerId = timer.id,
            actionValue = state.actionValue.ifEmpty { null }
        )

        viewModelScope.launch {
            if(uiState.value.selectedTimer == null || dialogState.value.newTimer)
                 timerRepository.add(timer)

            if(uiState.value.selectedScript != null)
                scriptRepository.update(script)
            else
                scriptRepository.add(script)
        }
    }

    fun saveScript() {
        val state = dialogState.value
       if(state.source == null || state.target == null) return

        val script = Script(
            id = uiState.value.selectedScript ?: UUID.randomUUID(),
            commandAction = state.command,
            actionValue = state.actionValue.ifEmpty { null },
            guard = state.guard,
            guardValue = state.guardValue.ifEmpty { null },
            eventType = state.event,
            deviceId = state.target.id,
            sourceId = state.source.id,
            timerId = null
        )
        viewModelScope.launch {

            Log.d(TAG, "Save script $script")
            if(_uiState.value.selectedScript != null)
                scriptRepository.update(script)
            else
                scriptRepository.add(script)
        }
    }

    fun deleteScript(script: Script) {
        viewModelScope.launch {
            scriptRepository.delete(script)
        }
    }

    fun deleteTimer(timer: Timer) {
        viewModelScope.launch {
            timerRepository.delete(timer)
        }
    }

    fun getScriptsForEvent(): List<Script> {
        val dto = uiState.value.dto
        return if(dto != null)
            uiState.value.scripts[dto] ?: emptyList()
        else
            emptyList()

    }

    fun getScriptsForTimer(): List<Script> {
        return if(uiState.value.timer != null)
            uiState.value.timers[uiState.value.timer!!] ?: emptyList()
        else
            emptyList()
    }

    companion object {
        const val TAG = "ScriptViewModel"
    }
}