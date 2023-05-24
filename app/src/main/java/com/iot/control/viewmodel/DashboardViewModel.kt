package com.iot.control.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.CommandManager
import com.iot.control.infrastructure.DbContext
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.model.Command
import com.iot.control.model.Device
import com.iot.control.model.enums.CommandAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val devices: List<Device> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val commandManager: CommandManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.getByDisplayableState(true).collect { devices ->
                _uiState.update { it.copy(devices = devices) }
            }
        }
    }

    fun send(device: Device, action: CommandAction) {
        viewModelScope.launch {
            commandManager.executeByDeviceAction(device, action)
        }
    }

    companion object {
        const val TAG = "DashboardViewModel"
    }
}