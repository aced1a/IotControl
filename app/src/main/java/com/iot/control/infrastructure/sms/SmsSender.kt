package com.iot.control.infrastructure.sms

import android.content.Context
import android.os.Build
import android.telephony.SmsManager

class SmsSender(private val manager: SmsManager) {
    fun send(number: String, payload: String) {
        manager.sendTextMessage(number, null, payload, null, null)
    }

    companion object {
        fun create(context: Context): SmsSender {
            val manager = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                context.getSystemService(SmsManager::class.java)
            else
                @Suppress("DEPRECATION") SmsManager.getDefault()

            return SmsSender(manager)
        }
    }
}