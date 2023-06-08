package com.iot.control.infrastructure

import android.util.Log
import com.iot.control.infrastructure.mqtt.broker.MqttBroker
import com.iot.control.infrastructure.mqtt.MqttConnections
import com.iot.control.infrastructure.repository.CommandRepository
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.LogRepository
import com.iot.control.infrastructure.sms.SmsClient
import com.iot.control.infrastructure.utils.setValue
import com.iot.control.infrastructure.utils.tryParse
import com.iot.control.model.Command
import com.iot.control.model.Device
import com.iot.control.model.LogMessage
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
    private val connectionRepository: ConnectionRepository,
    private val notificationManager: NotificationManager,
    private val logRepository: LogRepository
) {
    companion object {
        const val TAG = "CommandManager"
    }


    suspend fun executeByAction(deviceId: UUID, action: CommandAction, value: String?=null) = withContext(Dispatchers.IO) {
        val device = deviceRepository.getDeviceById(deviceId)
        if(device != null)
            executeByDeviceAction(device, action, value)
    }

    suspend fun executeByDeviceAction(device: Device, action: CommandAction, value: String?) = withContext(Dispatchers.IO) {
        val mqttCommand = commandRepository.getByDeviceAction(device.id, action, ConnectionType.MQTT)

        if(mqttCommand != null) {
            executeMqttCommand(mqttCommand, value)
        } else {
            val smsCommand = commandRepository.getByDeviceAction(device.id, action, ConnectionType.SMS)
            if(smsCommand != null)
                executeSmsCommand(smsCommand, value)
        }
    }

    private suspend fun executeMqttCommand(command: Command, value: String?) {
        val connection = connectionRepository.getById(command.connectionId) ?: return
        val callback = { update(command, value) }
        val onFail = { notificationManager.retryMqtt(command, value) }

        val payload = if(command.isJson) setJsonPayload(command.payload, command.dataField, value) else getPayload(command.payload, value)
        logRepository.add(LogMessage(address=connection.address, name = command.action.name, topic=command.topic, resolved=true, event=false, message = payload))

        if(connection.type == ConnectionType.LOCAL_MQTT) {
            if(mqttBroker.has(connection.username))
                mqttBroker.publish(command.topic, payload, callback)
            else
                onFail()
        }
        else {
            val client = mqttConnections.get(connection.address)
            if(client != null)
                client.publish(command.topic, payload, callback, onFail)
            else
                onFail()
        }
    }

    private suspend fun executeSmsCommand(command: Command, value: String?) {
        val connection = connectionRepository.getById(command.connectionId) ?: return
        val updatedPayloadCommand = command.copy(payload = getPayload(command.payload, value))

        logRepository.add(LogMessage(address=connection.address, name = command.action.name, topic=command.topic, resolved=true, event=false, message = updatedPayloadCommand.payload))
        if(smsClient.running)
            smsClient.send(connection.address, updatedPayloadCommand, value, connection.expiredTime)
        else
            notificationManager.retrySms(command, value)
    }

    private fun setJsonPayload(payload: String, field: String?, value: String?): String {
        if(value == null || field == null) return payload

        return setValue(payload, field, value)
    }

    private fun getPayload(payload: String, value: String?): String {
        return if(value != null)
            payload.replace("{value}", value)
        else
            payload
    }

    fun update(command: Command, value: String? = null) {
        Log.d(TAG, "Updating device value by $command")
        kotlinx.coroutines.MainScope().launch {
            deviceRepository.updateByCommand(command.deviceId, command, value)
        }
    }

    suspend fun retry(commandId: UUID, mqtt: Boolean, value: String?) {
        val command = commandRepository.getById(commandId)
        Log.d(TAG, "Cmd: $command")
        if(command == null) return

        if(command.type == ConnectionType.MQTT && mqtt) {
            executeMqttCommand(command, value)
        } else if(command.type == ConnectionType.SMS) {
            executeSmsCommand(command, value)
        } else {
            val smsCommand = commandRepository.getByDeviceAction(command.deviceId, command.action, ConnectionType.SMS) ?: return
            executeSmsCommand(smsCommand, value)
        }
    }
}