package com.iot.control.viewmodel


import android.app.Activity
import android.content.Context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iot.control.R
import com.iot.control.infrastructure.mqtt.broker.MqttBroker
import com.iot.control.infrastructure.mqtt.MqttConnections
import com.iot.control.infrastructure.sms.SmsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val connectionsRunning: Boolean,
    val brokerRunning: Boolean,
    val smsClientRunning: Boolean,
    val locale: Int,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mqttConnections: MqttConnections,
    private val mqttBroker: MqttBroker,
    private val smsClient: SmsClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(
        SettingsUiState(
            connectionsRunning = mqttConnections.running,
            brokerRunning = mqttBroker.running,
            smsClientRunning = smsClient.running,
            locale = R.string.en
        )
    )

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val languageList: List<Int>
        get() = listOf(R.string.ru, R.string.en)



    fun updateLocale(context: Context) {
        val locale = Locale.getDefault()//config.locales[0]


        _uiState.update {
            it.copy(locale = if(locale.language == "ru") R.string.ru else R.string.en)
        }
    }

    fun changeLanguage(context: Context, language: Int) {
        val config = context.resources.configuration
        val locale = Locale(if(language == R.string.ru) "ru" else "en")

        if(config.locales[0] != locale) {
            context.createConfigurationContext(config)

            Locale.setDefault(locale)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            _uiState.update { it.copy(locale = language) }
        }
    }

    fun toggleMqttConnections() {
        viewModelScope.launch {
            if(mqttConnections.running)
                mqttConnections.stop()
            else
                mqttConnections.start()

            _uiState.update { it.copy(connectionsRunning = mqttConnections.running) }
        }
    }

    fun toggleMqttBroker() {
        viewModelScope.launch {
            if(mqttBroker.running)
                mqttBroker.stop()
            else
                mqttBroker.start()

            _uiState.update { it.copy(brokerRunning = mqttBroker.running) }
        }
    }

    fun toggleSmsClient(context: Context) {
        viewModelScope.launch {
            if(smsClient.running)
                smsClient.stop(context)
            else
                smsClient.start(context)

            _uiState.update { it.copy(smsClientRunning = smsClient.running) }
        }
    }

}