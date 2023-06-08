package com.iot.control.infrastructure.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.UUID

class DeliveryIntentReceiver(val resolve: (String, Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        Log.d("DeliveryReceiver", "Receive new intent")

        val id = intent.getStringExtra("id") ?: return

        if(resultCode == Activity.RESULT_OK) {
            resolve(id, true)
        } else {
            resolve(id, false)
        }
    }

    private fun getUUID(id: String?) = try {
        UUID.fromString(id)
    } catch(_: Throwable) {
        null
    }
}