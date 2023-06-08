package com.iot.control.infrastructure.mqtt.broker

import android.content.Context
import android.os.Environment
import android.util.Log
import com.iot.control.infrastructure.EventManager
import com.iot.control.infrastructure.NotificationManager
import com.iot.control.model.Connection
import io.moquette.BrokerConstants
import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig
import io.moquette.interception.AbstractInterceptHandler
import io.moquette.interception.messages.InterceptAcknowledgedMessage
import io.moquette.interception.messages.InterceptConnectMessage
import io.moquette.interception.messages.InterceptConnectionLostMessage
import io.moquette.interception.messages.InterceptDisconnectMessage
import io.moquette.interception.messages.InterceptPublishMessage
import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt.MqttMessageBuilders
import io.netty.handler.codec.mqtt.MqttPublishMessage
import io.netty.handler.codec.mqtt.MqttQoS
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.Properties
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttBroker @Inject constructor(
    private val eventManager: EventManager,
) {
    companion object {
        const val ADDRESS = "localhost"
        const val PREFERENCES = "broker-preferences"
        const val TAG = "MqttBroker"
    }

    private val id: String = "IotControlBroker-${UUID.randomUUID()}"
    private val clients: MutableSet<String> = mutableSetOf()
    private val server: Server = Server()
    private val properties = Properties().apply {
        set(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, false.toString())
        set(BrokerConstants.HOST_PROPERTY_NAME, "0.0.0.0")
        set(BrokerConstants.PORT_PROPERTY_NAME, "1883")
        set(BrokerConstants.AUTHENTICATOR_CLASS_NAME, BrokerAuthenticator::class.java.name)
//        set(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, Environment.getExternalStorageDirectory().absolutePath+File.separator+BrokerConstants.DEFAULT_MOQUETTE_STORE_H2_DB_FILENAME)
    }

    fun has(username: String?): Boolean = username != null && clients.contains(username)

    var running: Boolean = false
        private set

    fun start(context: Context) {
        if(running) return

        try {
            running = true
            tryLoadPreferences(context)

            Log.d(TAG, "${properties[BrokerConstants.AUTHENTICATOR_CLASS_NAME]}")

            val config = MemoryConfig(properties)
            server.startServer(config, listOf(InterceptHandler()))

        } catch (e: Exception) {
            running = false
            Log.d(TAG, "Failure on broker start: ${e.message}")
        }
    }

    private fun tryLoadPreferences(context: Context) {
        try {
            properties.load(context.openFileInput(PREFERENCES))
        } catch (_: IOException) {
            trySaveDefaultsPreferences(context)
        }
    }

    private fun trySaveDefaultsPreferences(context: Context) {
        try {
            properties.store(context.openFileOutput(PREFERENCES, Context.MODE_PRIVATE), "#")
        }
        catch (_: Exception) {}
    }


    fun stop() {
        if(running) {
            server.stopServer()
            running = false
        }
    }

    fun publish(topic: String, payload: String, callback: () -> Unit) {
        val message = MqttMessageBuilders.publish()
            .topicName(topic)
            .qos(MqttQoS.EXACTLY_ONCE)
            .payload(Unpooled.copiedBuffer(payload.toByteArray()))
            .build()

        if(internalPublish(message)) callback()
    }

    private fun internalPublish(message: MqttPublishMessage): Boolean {
        return try {
            server.internalPublish(message, id)
            Log.d(TAG, "Successfully internal publish")
            true
        } catch (e: Exception) {
            Log.d(TAG, "Failure on internal publish")
            false
        }
    }

    inner class InterceptHandler() : AbstractInterceptHandler() {
        override fun getID(): String {
            return "IotControlPublishListener"
        }

        override fun onMessageAcknowledged(msg: InterceptAcknowledgedMessage?) {
            super.onMessageAcknowledged(msg)
            Log.d(TAG, "message ${msg?.username} ${msg?.topic}")
        }

        override fun onConnect(msg: InterceptConnectMessage?) {
            super.onConnect(msg)
            if(msg != null) {
                clients.add(msg.username)
                Log.d(TAG, "New connection: (${msg.clientID}, ${msg.username})")
            }
        }

        override fun onDisconnect(msg: InterceptDisconnectMessage?) {
            super.onDisconnect(msg)
            if(msg != null) {
                clients.remove(msg.username)
                Log.d(TAG, "Client disconnected: (${msg.clientID}, ${msg.username})")
            }
        }

        override fun onConnectionLost(msg: InterceptConnectionLostMessage?) {
            super.onConnectionLost(msg)
            if(msg != null) {
                clients.remove(msg.username)
                Log.d(TAG, "Lost connection with: (${msg.clientID}, ${msg.username})")
            }
        }

        override fun onPublish(msg: InterceptPublishMessage?) {
            Log.d(TAG, "New publish")
            super.onPublish(msg)
            if(msg != null) {
                val buf = msg.payload
                val bytes = ByteArray(buf?.readableBytes() ?: 0)
                buf.getBytes(buf.readerIndex(), bytes)

                MainScope().launch {
                    eventManager.resolveMqttEvent("$ADDRESS:${msg.username}", msg.topicName, String(bytes, Charsets.UTF_8))
                }
            }
        }

    }

}