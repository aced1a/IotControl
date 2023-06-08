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
import com.iot.control.model.enums.CommandMode
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
    val number: String,
    val value: String? = null,
    val expired: Long = SmsClient.ExpireTime
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
    private var schedule: Timer = Timer()
    private var timerRunning = false

    private fun initTimer() {
        if(!timerRunning) {

            val timerTask = object : TimerTask() {
                override fun run() {
                    val time = System.currentTimeMillis()
                    var empty = true

                    for(item in sent.entries)
                        if(item.value.time <= time) {
                            notificationManager.retrySms(item.value.command, item.value.value)
                            sent.remove(item.key)
                        }

                    for(item in queue.values) {
                        val head = item.peek()
                        if(head != null && head.time <= time) {
                            sendNext(head.number)
                        }
                        empty = empty and item.isEmpty()
                    }
                    if(sent.isEmpty() && empty) stopTimer()
                }
            }

            schedule = Timer()
            schedule.schedule(timerTask, 1000, ExpireTime)
            timerRunning = true
        }
    }

    private fun stopTimer() {
        if(timerRunning) {

            schedule.cancel()
            schedule.purge()

            timerRunning = false
        }
    }

    fun send(number: String, command: Command, value: String?, expired: Long) {
        if(command.mode == CommandMode.Async) {
            sendAndSave(number, command, value, expired)
        } else {
            if(queue.containsKey(number).not()) queue[number] = LinkedList()

            if(queue[number]?.isEmpty() == true) {
                sendAndSave(number, command, value, expired)
            }

            queue[number]?.add(CommandRecord(
                command = command,
                message = null,
                number = number,
                value = value,
                expired = expired,
                time = System.currentTimeMillis() + expired
            ))
        }
        initTimer()
    }

    private fun sendAndSave(number: String, command: Command, value: String?, expired: Long) {
        val id = command.id.toString()
        if(sender.send(id, number, command.payload)) {
            sent[id] = CommandRecord(command = command, message = null, number = number, value = value, expired = expired, time = System.currentTimeMillis() + expired)
        }
    }

    private fun receive(number: String, payload: String) {
        val onFail = {
            val command = sendNext(number)

            if(command != null && command.time > System.currentTimeMillis())
                command
            else
                null
        }

        MainScope().launch {
            eventManager.resolveSmsEvent(number, payload, onFail)
        }
    }

    private fun sendNext(number: String): CommandRecord? {
        val returned = queue[number]?.poll()

        var head = queue[number]?.peek()

        if(head != null) {
            head.time = System.currentTimeMillis() + head.expired
            sendAndSave(head.number, head.command, head.value, head.expired)
        }
        return returned
    }

    private fun delivered(id: String, success: Boolean) {
        val record = sent[id] ?: return

        sent.remove(id)
        if(success) {
            updateOnSuccess(record.number, record.command)
        } else {
            val command = record.command
            Log.d(TAG, "Failure on deliver sms command: ${command.type.name}")
            if(command.mode != CommandMode.Async) sendNext(record.number)
            notificationManager.retrySms(command, record.value)
        }
    }

    private fun updateOnSuccess(number: String, command: Command) {
        if(command.mode == CommandMode.Async) {
            MainScope().launch {
                deviceRepository.updateByCommand(command.deviceId, command, null)
            }
        } else {
            val message =  queue[number]?.peek() ?: return
            if(message.command.id == command.id) message.time = System.currentTimeMillis() + message.expired
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

//            initTimer()
            Log.d(TAG, "Receiver has been registered")
        }
    }

    fun stop(context: Context) {
        if(running) {
            Log.d(TAG, "Stopping sms client")
            running = false

            try {
                context.unregisterReceiver(receiver)
                context.unregisterReceiver(deliveryReceiver)
            } catch (_ : Exception) {}
            stopTimer()

            queue.clear()
            sent.clear()
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ExpireTime = 8 * 1000L
        const val DELIVER_ACTION = "DELIVER_ACTION"
        const val TAG = "SmsClient"
    }
}