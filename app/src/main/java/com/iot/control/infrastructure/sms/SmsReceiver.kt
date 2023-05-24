package com.iot.control.infrastructure.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony

class SmsReceiver(val resolve: (String, String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        for(message in messages) {
            resolve(message.displayOriginatingAddress, message.displayMessageBody)
        }
    }
}