package com.iot.control.infrastructure

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.iot.control.BuildConfig
import com.iot.control.DatabaseModule
import com.iot.control.infrastructure.Notification as MyNotification

import com.iot.control.R
import com.iot.control.infrastructure.mqtt.broker.MqttBroker
import com.iot.control.infrastructure.mqtt.MqttConnections
import com.iot.control.infrastructure.mqtt.broker.BrokerAuthenticator
import com.iot.control.infrastructure.repository.ConnectionRepository
import com.iot.control.infrastructure.sms.SmsClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IotService : LifecycleService() {
    @Inject lateinit var database: ApplicationDatabase

    @Inject lateinit var mqttConnections: MqttConnections
    @Inject lateinit var mqttBroker: MqttBroker
    @Inject lateinit var smsClient: SmsClient
    @Inject lateinit var timerManager: TimerManager

    @Inject lateinit var notifications: com.iot.control.infrastructure.NotificationManager

    private var started = false
    private var isForeground = false

    private val localBinder = LocalBinder()
    private var bindCount = 0

    private val isBound get() = bindCount > 0
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if(intent?.action == ACTION_STOP) {
            lifecycleScope.launch {
                mqttConnections.stop()
                mqttBroker.stop()
                smsClient.stop(this@IotService)
                timerManager.stop()
            }
        }

        if(started.not()) {
            Log.d(TAG, "Start service")
            started = true

            setWakeLock()
            createNotificationChannel()

            lifecycleScope.launch {
                BrokerAuthenticator.init(ConnectionRepository(database.connectionDao()))
                mqttConnections.start()
                mqttBroker.setup(this@IotService)
                smsClient.start(this@IotService)
                timerManager.init()
            }

            lifecycleScope.launch {
                notifications.notifications.collect(::showNotification)
            }
        }

        manageLifetime()

        return START_STICKY
    }

    private fun setWakeLock() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED)
            wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IotService::lock").apply { acquire() }
            }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        handleBind()
        return localBinder
    }

    override fun onRebind(intent: Intent?) {
        handleBind()
    }

    private fun handleBind() {
        bindCount++
        startService(Intent(this, this::class.java))
    }

    override fun onUnbind(intent: Intent?): Boolean {
        bindCount--
        lifecycleScope.launch {
            delay(UNBIND_DELAY_MILLIS)
            manageLifetime()
        }
        return true
    }

    private fun manageLifetime() {
        when {
            isBound -> exitForeground()
            mqttConnections.running || mqttBroker.running || smsClient.running -> enterForeground()
            else -> stopService()
        }
    }

    private fun enterForeground() {
        if(isForeground.not()) {
            isForeground = true

            showForegroundNotification()
        }
    }

    private fun exitForeground() {
        if(isForeground) {
            isForeground = false
            stopForeground(STOP_FOREGROUND_DETACH)
        }
    }

    private fun stopService() {
        wakeLock?.let {
            if(it.isHeld) it.release()
        }
        stopSelf()
    }

    private fun showForegroundNotification() {
        if(isForeground.not()) return

//        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "IotService",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(this.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, this::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Tap to open application")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.baseline_lightbulb_48)
            .addAction(R.drawable.baseline_switch_left_12, getString(R.string.cancel_label), stopIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        //TODO("replace string to res id")
    }

    private fun showNotification(notification: MyNotification){
        if(
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            && notification.id != 0
        ) {

            val title = getNotificationTitle(notification)
            val message = getNotificationContent(notification)

            val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_lightbulb_48)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(notification.id, builder.build())
            }
        }
    }

    private fun getNotificationTitle(notification: MyNotification): String {
        Log.d(TAG, "Try get string resource ${notification.titleRes}")

        return notification.title ?: getString(notification.titleRes)
    }

    private fun getNotificationContent(notification: MyNotification): String {
        return notification.content ?: if(notification.arg != null) getString(notification.contentRes, notification.arg) else getString(notification.contentRes)
    }


    private companion object {
        const val TAG = "IotService"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "IotService"
        const val UNBIND_DELAY_MILLIS = 1500L
        const val ACTION_STOP = BuildConfig.APPLICATION_ID + ".ACTION_STOP"
    }

    internal inner class LocalBinder : Binder() {
        fun getService(): IotService = this@IotService
    }
}

class IotServiceConnection @Inject constructor(): ServiceConnection {
    var service: IotService? = null
        private set

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        service = (binder as IotService.LocalBinder).getService()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }
}