package com.iot.control.infrastructure.mqtt.broker

import android.util.Log
import com.iot.control.infrastructure.NotificationManager
import com.iot.control.infrastructure.repository.ConnectionRepository
import io.moquette.broker.security.IAuthenticator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class BrokerAuthenticator() : IAuthenticator {

//    @Inject lateinit var connectionRepository: ConnectionRepository
//    @Inject lateinit var notificationManager: NotificationManager

    companion object {
        var repository: ConnectionRepository? = null

        fun init(connectionRepository: ConnectionRepository) {
            repository = connectionRepository
        }
    }


    override fun checkValid(clientId: String?, username: String?, password: ByteArray?): Boolean {
        Log.d("Auth", "Try authenticate ($username, $password)")
        if(username == null || repository == null) return false


        return runBlocking {
            val connection = repository?.getByUsername(username)
            Log.d("Auth", "${connection?.password?.toByteArray(Charsets.UTF_8)} | ${password}")

            (connection?.password != null && connection.password.toByteArray(Charsets.UTF_8).contentEquals(password))
        }
    }
}