package com.iot.control.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.DbContext
import com.iot.control.model.EventDto
import com.iot.control.model.Script
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class ScriptUiState(
    val scripts: Map<EventDto, List<Script>> = emptyMap()
)

class ScriptViewModel : ViewModel() {
    private val eventRepository = DbContext.get().eventRepository

    private val _uiState = MutableStateFlow(ScriptUiState())
    val uiState: StateFlow<ScriptUiState> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            eventRepository.getEventDto().collect { scripts ->
                _uiState.update { it.copy(scripts = scripts) }
            }
        }
    }

    companion object {
        const val TAG = "ScriptViewModel"
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScriptViewModel() as T
            }
        }
    }
}