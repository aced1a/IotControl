package com.iot.control.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.DbContext
import com.iot.control.infrastructure.mqtt.MqttBroker
import com.iot.control.infrastructure.mqtt.MqttConnections
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.sms.SmsClient
import com.iot.control.model.Connection
import com.iot.control.model.enums.ConnectionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


data class ConnectionDialogState(
    val name: String = "",
    val address: String = "",
    val port: String = "8883",
    val username: String = "",
    val password: String = "",
    val parser: String = "",
    val isSsl: Boolean = false,
    val type: ConnectionType = ConnectionType.MQTT)

data class ConnectionsUiState(
    val connections: List<Connection> = emptyList(),
    val selectedConnectionID: UUID? = null,
    val isLoading: Boolean = false)


@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val deviceRepository: DeviceRepository,
    private val mqttConnections: MqttConnections,
    private val smsClient: SmsClient,
    private val mqttBroker: MqttBroker
) : ViewModel() {

    private val _dialogState = MutableStateFlow(ConnectionDialogState())
    val dialogState: StateFlow<ConnectionDialogState> = _dialogState.asStateFlow()

    private val _uiState = MutableStateFlow(ConnectionsUiState(isLoading = true))
    val uiState: StateFlow<ConnectionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            connectionRepository.getAll().collect { connections ->
                _uiState.update { it.copy(connections = connections) }
            }
        }
    }

    fun active(connection: Connection): Boolean {
        return when(connection.type) {
            ConnectionType.SMS -> smsClient.running
            ConnectionType.LOCAL_MQTT -> mqttBroker.running && mqttBroker.has(connection.username)
            ConnectionType.MQTT -> mqttConnections.running && mqttConnections.has(connection.address)
            else -> false
        }
    }

    fun newConnection() {
        _uiState.update { it.copy(selectedConnectionID = null) }
        _dialogState.update { ConnectionDialogState() }
    }

    fun loadSelectedConnection(connection: Connection)
    {
        _uiState.update { it.copy(selectedConnectionID = connection.id) }
        _dialogState.update {
            it.copy(
                name = connection.name,
                address = connection.address,
                port = connection.port.toString(),
                username = connection.username ?: "",
                password = connection.password ?: "",
                parser = connection.parser ?: "",
                isSsl = connection.isSsl,
                type = connection.type
            )
        }
    }

    fun updateDialogModel(connectionDialogNewState: ConnectionDialogState)
    {
        _dialogState.update { connectionDialogNewState }
    }

    fun saveConnection() {
        //TODO("address for local broker connection")
        val type = dialogState.value.type
        val connection = Connection(
            id = uiState.value.selectedConnectionID ?: UUID.randomUUID(),
            name = dialogState.value.name,
            address = if(type == ConnectionType.LOCAL_MQTT) "local:${dialogState.value.username}" else dialogState.value.address,
            port = dialogState.value.port.toIntOrNull() ?: 8883,
            username = dialogState.value.username.ifEmpty { null },
            password = dialogState.value.password.ifEmpty { null },
            parser = dialogState.value.parser.ifEmpty { null },
            isSsl = dialogState.value.isSsl,
            type = type
        )

        Log.d(TAG, "Try save connection $connection")

        viewModelScope.launch {
            if(uiState.value.selectedConnectionID == null)
                connectionRepository.add(connection)
            else
                connectionRepository.update(connection)
        }
    }

    fun deleteConnection(connection: Connection) {
        viewModelScope.launch {
            connectionRepository.delete(connection)
            deviceRepository.deleteWidows()
        }
    }

    companion object {
        const val TAG = "ConnectionViewModel"
    }
}