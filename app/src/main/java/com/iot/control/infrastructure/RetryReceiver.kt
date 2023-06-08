package com.iot.control.infrastructure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class RetryReceiver @Inject constructor(private val manager: CommandManager) : BroadcastReceiver() {
    override fun onReceive(ctx: Context?, intent: Intent?) {
        Log.d("Retry", "Intent $intent")
        if(intent == null) return

        val id = UUID.fromString(intent.getStringExtra("id")) ?: return
        val mqtt = intent.getBooleanExtra("mqtt", true)
        val value = intent.getStringExtra("value")

        Log.d("Retry", "$id | $mqtt | $value")

        MainScope().launch {
            manager.retry(id, mqtt, value)
        }

    }
}