package com.iot.control.infrastructure.sms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import kotlin.random.Random


class SmsSender(private val manager: SmsManager, private val getContext: () -> Context) {
    private val random = Random(System.currentTimeMillis())

    fun send(id: String, number: String, payload: String): Boolean {

        val intent = createIntent(id)

        return try {
            manager.sendTextMessage(number, null, payload, null, intent)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun createIntent(id: String): PendingIntent {
        //TODO try?
        val intent = Intent(SmsClient.DELIVER_ACTION).apply { putExtra("id", id) }

        return PendingIntent.getBroadcast(
            getContext(),
            random.nextInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    }

    companion object {
        fun create(context: Context, getContext: () -> Context): SmsSender {
            val manager = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                context.getSystemService(SmsManager::class.java)
            else
                @Suppress("DEPRECATION") SmsManager.getDefault()

            return SmsSender(manager, getContext)
        }
    }
}