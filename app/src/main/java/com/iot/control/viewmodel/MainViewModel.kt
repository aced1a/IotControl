package com.iot.control.viewmodel

import android.content.ServiceConnection
import androidx.lifecycle.ViewModel
import com.iot.control.infrastructure.IotServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val serviceConnection: IotServiceConnection
) : ViewModel(), ServiceConnection by serviceConnection {

    fun toggleMqttServices(){}
    fun toggleSmsServices(){}
    fun toggleBroker(){}

}