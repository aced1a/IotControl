package com.iot.control.viewmodel

import com.iot.control.model.Device
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.DbContext
import com.iot.control.model.Connection
import com.iot.control.model.enums.ConnectionType
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID


data class DevicesUiState(
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: UUID? = null
)

data class SelectDeviceUiState(
    val withoutConnection: List<Device> = emptyList(),
    val others: List<Device> = emptyList(),
    val type: ConnectionType = ConnectionType.MQTT
)

class DevicesViewModel(val connectionId: UUID) : ViewModel() {
    private val devicesRep = DbContext.get().deviceRepository
    private val connectionRep = DbContext.get().connectionRepository

    private lateinit var connection: Connection

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    private val _selectState = MutableStateFlow(SelectDeviceUiState())
    val selectState: StateFlow<SelectDeviceUiState> = _selectState.asStateFlow()

    init {
        viewModelScope.launch {
            connection = connectionRep.getById(connectionId) ?: Connection()

            devicesRep.getByConnectionId(connectionId).collect { devices ->
                _uiState.update { it.copy(devices = devices) }
            }
        }
    }

    fun selectDevice(device: Device) {
        viewModelScope.launch {
            val updatedDevice = if(connection.type.value() == ConnectionType.MQTT)
                                    device.copy(mqttConnectionId = connectionId)
                                else
                                    device.copy(smsConnectionId = connectionId)

            devicesRep.update(updatedDevice)
        }
    }

    fun loadExistingDevices() {
        //TODO("Refactor")
        viewModelScope.launch {
            val devices = devicesRep.getAll()
            val filter: (Device) -> Boolean =
                if(connection.type.value() == ConnectionType.MQTT)
                    { device -> device.mqttConnectionId == null }
                else
                    { device -> device.smsConnectionId == null }

            val withoutDef = async { devices.filter(filter) }
            val othersDef = async { devices.filter { !filter(it) } }

            val without = withoutDef.await()
            val others = othersDef.await()

            _selectState.update {
                it.copy(
                    type = connection.type.value(),
                    withoutConnection = without,
                    others = others
                )
            }
        }
    }

    companion object {
        const val TAG = "DevicesViewModel"
        fun provideFactory(connectionId: UUID): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DevicesViewModel(connectionId) as T
            }
        }
    }
}