package com.iot.control.infrastructure

import android.util.Log
import com.iot.control.infrastructure.mqtt.MqttBroker
import com.iot.control.infrastructure.mqtt.MqttConnections
import com.iot.control.infrastructure.repository.CommandRepository
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.sms.SmsClient
import com.iot.control.model.Command
import com.iot.control.model.Device
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.ConnectionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandManager @Inject constructor(
    private val mqttBroker: MqttBroker,
    private val mqttConnections: MqttConnections,
    private val smsClient: SmsClient,
    private val deviceRepository: DeviceRepository,
    private val commandRepository: CommandRepository,
    private val connectionRepository: ConnectionRepository
) {
    companion object {
        const val TAG = "CommandManager"
    }

    suspend fun executeByAction(deviceId: UUID, action: CommandAction) = withContext(Dispatchers.IO) {
        val device = deviceRepository.getDeviceById(deviceId)
        if(device != null)
            executeByDeviceAction(device, action)
    }

    suspend fun executeByDeviceAction(device: Device, action: CommandAction) = withContext(Dispatchers.IO) {
        val mqttCommand = commandRepository.getByDeviceAction(device.id, action, ConnectionType.MQTT)

        if(mqttCommand != null) {
            //TODO report when failure
            executeMqttCommand(mqttCommand, device)
        } else {
            val smsCommand = commandRepository.getByDeviceAction(device.id, action, ConnectionType.SMS)
            if(smsCommand != null)
                executeSmsCommand(smsCommand, device)
        }
    }

    private suspend fun executeMqttCommand(command: Command, device: Device) {
        if(device.mqttConnectionId != null) {
            val connection = connectionRepository.getById(device.mqttConnectionId) ?: return
            val callback = { update(command) }

            if(connection.type == ConnectionType.LOCAL_MQTT) {
                mqttBroker.publish(command.topic, command.payload, callback)
            }
            else {
                val client = mqttConnections.get(connection.address)
                client?.publish(command.topic, command.payload, callback)
            }
        }

    }

    private suspend fun executeSmsCommand(command: Command, device: Device) {
        if(device.smsConnectionId != null) {
            val connection = connectionRepository.getById(device.smsConnectionId) ?: return

            //SmsClient.send
        }
    }

    fun update(command: Command, value: String? = null) {
        Log.d(TAG, "Updating device value by $command")
        kotlinx.coroutines.MainScope().launch {
            deviceRepository.updateByCommand(command.id, command, value)
        }
    }
}