package com.iot.control.infrastructure.sms

import android.content.Context
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.Telephony
import android.util.Log
import com.iot.control.infrastructure.EventManager
import com.iot.control.infrastructure.NotificationManager
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.model.Command
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class CommandRecord(
    val command: Command,
    var message: String?,
    var time: Long = System.currentTimeMillis() + SmsClient.ExpireTime,
    val number: String
)

@Singleton
class SmsClient @Inject constructor(
    private val eventManager: EventManager,
    private val notificationManager: NotificationManager,
    private val deviceRepository: DeviceRepository,
    private val sender: SmsSender
) {
    var running: Boolean = false
        private set

    private val receiver = SmsReceiver(::receive)
    private val deliveryReceiver = DeliveryIntentReceiver(::delivered)

    private val sent: MutableMap<String, CommandRecord> = mutableMapOf()
    private val queue: MutableMap<String, Queue<CommandRecord>> = mutableMapOf()
    private val schedule: Timer = Timer()

    private fun initTimer() {
        schedule.schedule(timerTask, 1000, ExpireTime)
    }

    private val timerTask = object : TimerTask() {
        override fun run() {
            val time = System.currentTimeMillis()

            for(item in sent.entries)
                if(item.value.time >= time) sent.remove(item.key)

            for(item in queue.values) {
                val head = item.peek()
                if(head != null && head.time >= time) {
                    sendNext()
                }
            }
        }
    }

    fun send(number: String, command: Command) {
        if(command.isSync) {
            if(queue.containsKey(number).not()) queue[number] = LinkedList()

            if(queue[number]?.isEmpty() == true) {
               sendAndSave(number, command)
            }

            queue[number]?.add(CommandRecord(command = command, message = null, number = number))
        } else {
            sendAndSave(number, command)
        }
    }

    private fun sendAndSave(number: String, command: Command) {
        val id = command.id.toString()
        if(sender.send(id, number, command.payload)) {
            sent[id] = CommandRecord(command = command, message = null, number = number)
        }
    }

    private fun receive(number: String, payload: String) {
        val onFail = {
            val command = queue[number]?.poll()

            if(command != null && command.time > (System.currentTimeMillis() + ExpireTime))
                command
            else
                null
        }

        MainScope().launch {
            eventManager.resolveSmsEvent(number, payload, onFail)
        }
    }

    private fun sendNext() {

    }

    private fun delivered(id: String, success: Boolean) {
        val command = sent[id] ?: return

        sent.remove(id)
        if(success) {
            updateOnSuccess(command.number, command.command)
        } else {

        }
    }

    private fun updateOnSuccess(number: String, command: Command) {
        if(command.isSync) {
            val message =  queue[number]?.peek() ?: return
            if(message.command.id == command.id) message.time = System.currentTimeMillis() + ExpireTime

        } else MainScope().launch {
            deviceRepository.updateByCommand(command.deviceId, command, null)
        }
    }



    fun start(context: Context) {
        Log.d(TAG, "Try start sms client")
        if(running) return

        if(hasPermission(context, Manifest.permission.RECEIVE_SMS) && hasPermission(context, Manifest.permission.SEND_SMS)) {
            Log.d(TAG, "Permission granted")
            running = true

            val smsIntent = IntentFilter().apply { addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION) }
            val deliveryIntent = IntentFilter().apply { addAction(DELIVER_ACTION) }

            context.registerReceiver(receiver, smsIntent)
            context.registerReceiver(deliveryReceiver, deliveryIntent)

            Log.d(TAG, "Receiver has been registered")
        }
    }

    fun stop(context: Context) {
        if(running) {
            Log.d(TAG, "Stopping sms client")
            running = false

            context.unregisterReceiver(receiver)
            context.unregisterReceiver(deliveryReceiver)
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ExpireTime = 60 * 1000L
        const val DELIVER_ACTION = "DELIVER_ACTION"
        const val TAG = "SmsClient"
    }
}