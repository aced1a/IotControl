package com.iot.control.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.iot.control.model.Device
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.DbContext
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.model.Connection
import com.iot.control.model.enums.ConnectionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.util.UUID
import javax.inject.Inject


data class DevicesUiState(
    val devices: List<Device> = emptyList(),
    val selectedDeviceId: UUID? = null
)

data class SelectDeviceUiState(
    val withoutConnection: List<Device> = emptyList(),
    val others: List<Device> = emptyList(),
    val type: ConnectionType = ConnectionType.MQTT
)

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val connectionRepository: ConnectionRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private lateinit var connection: Connection
    val connectionId: UUID = UUID.fromString(savedStateHandle.get<String>("connectionId").orEmpty())

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    private val _selectState = MutableStateFlow(SelectDeviceUiState())
    val selectState: StateFlow<SelectDeviceUiState> = _selectState.asStateFlow()

    init {
        viewModelScope.launch {
            connection = connectionRepository.getById(connectionId) ?: throw IllegalArgumentException("Connection doesn't exist")

            deviceRepository.getByConnectionId(connectionId).collect { devices ->
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

            deviceRepository.update(updatedDevice)
        }
    }

    fun loadExistingDevices() {
        //TODO("Refactor")
        viewModelScope.launch {
            val devices = deviceRepository.getAll()
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

    fun delete(device: Device) {
        viewModelScope.launch {
            deviceRepository.delete(device)
        }
    }

    companion object {
        const val TAG = "DevicesViewModel"
//        fun provideFactory(connectionId: UUID): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            @Suppress("UNCHECKED_CAST")
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                return DevicesViewModel(connectionId) as T
//            }
//        }
    }
}