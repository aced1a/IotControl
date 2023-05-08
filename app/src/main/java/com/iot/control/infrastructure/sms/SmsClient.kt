package com.iot.control.infrastructure.sms

import android.content.Context
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import com.iot.control.infrastructure.EventManager
import com.iot.control.model.Command
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsClient @Inject constructor(
    private val eventManager: EventManager,
    private val sender: SmsSender
) {
    var running: Boolean = false
        private set

    private val receiver = SmsReceiver()
    private val queue: Map<String, Queue<Pair<String, Command>>> = mutableMapOf()

    fun send(number: String, command: Command) {
        if(command.isSync) {

        } else {
            sender.send(number, command.payload)
        }
    }

    private fun receive(number: String, payload: String) {
        kotlinx.coroutines.MainScope().launch {
            eventManager.resolveSmsEvent(number, payload) {
                if(queue[number]?.isNotEmpty() == true) {

                }
            }
        }
    }

    fun start(context: Context) {
        Log.d(TAG, "Try start sms client")
        if(running) return

        if(hasPermission(context, Manifest.permission.RECEIVE_SMS) && hasPermission(context, Manifest.permission.SEND_SMS)) {
            Log.d(TAG, "Permission granted")
            running = true

            val smsIntent = IntentFilter().apply { addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION) }
            context.registerReceiver(receiver, smsIntent)
            Log.d(TAG, "Receiver has been registered")
        }
    }

    fun stop(context: Context) {
        if(running) {
            Log.d(TAG, "Stopping sms client")
            running = false

            context.unregisterReceiver(receiver)
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val TAG = "SmsClient"
    }
}