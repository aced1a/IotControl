package com.iot.control.infrastructure.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.util.Log

class SmsReceiver(val resolve: (String, String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        for(message in messages) {
            Log.d("SmsReceiver", "New message: (${message.displayOriginatingAddress}, ${message.displayMessageBody})")
            resolve(message.displayOriginatingAddress, message.displayMessageBody)
        }
    }
}