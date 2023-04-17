package com.iot.control.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.DbContext
import com.iot.control.model.Device
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val devices: List<Device> = emptyList()
)

class DashboardViewModel : ViewModel() {
    private val deviceRepository = DbContext.get().deviceRepository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.getByDisplayableState(true).collect { devices ->
                _uiState.update { it.copy(devices = devices) }
            }
        }
    }

    companion object {
        const val TAG = "DashboardViewModel"
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel() as T
            }
        }
    }
}