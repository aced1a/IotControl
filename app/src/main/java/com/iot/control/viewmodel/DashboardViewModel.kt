package com.iot.control.viewmodel

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iot.control.infrastructure.CommandManager
import com.iot.control.infrastructure.DbContext
import com.iot.control.infrastructure.repository.DashboardRepository
import com.iot.control.infrastructure.repository.DeviceRepository
import com.iot.control.infrastructure.repository.WidgetRepository
import com.iot.control.model.Command
import com.iot.control.model.Dashboard
import com.iot.control.model.Device
import com.iot.control.model.Widget
import com.iot.control.model.WidgetAndDevice
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.Icons
import com.iot.control.model.enums.WidgetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

data class DashboardUiState(
    val dashboards: List<Dashboard> = emptyList(),
    val devices: List<Device> = emptyList(),
    val widgets: List<WidgetAndDevice> = emptyList(),
    val selectedWidget: UUID? = null,
    val selectedDashboard: UUID? = null,
    val dashboard: Dashboard? = null
)

data class DashboardDialogState(
    val dashboardName: String = "",
    val newDashboard: Boolean = true,
    val name: String = "",
    val device: Device? = null,
    val type: WidgetType = WidgetType.State,
    val useIcon: Boolean = false,
    val expanded: Boolean = false,
    val icon: Icons = Icons.Light,
    val onColor: ULong = 18408873036368314368UL,
    val offColor: ULong = 18408873036368314368UL,
    val formatter: String = "",
    val subtext: String = ""
) {
    fun hexOn() = "#${offColor.toString(16).substring(2, 8)}"
    fun hexOff() = "#${offColor.toString(16).substring(2, 8)}"
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val commandManager: CommandManager,
    private val widgetRepository: WidgetRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(DashboardDialogState())
    val dialogState: StateFlow<DashboardDialogState> = _dialogState.asStateFlow()

    init {
        viewModelScope.launch {
            val devices = deviceRepository.getAll()
            _uiState.update { it.copy(devices = devices) }

            dashboardRepository.getAll().collect { dashboards ->
                _uiState.update { it.copy(dashboards = dashboards) }
            }
        }

        viewModelScope.launch {
            widgetRepository.getAll().collect { widgets ->
                _uiState.update { it.copy(widgets = widgets) }
            }
        }

    }

    fun send(device: Device, action: CommandAction, value: String?=null) {
        viewModelScope.launch {
            commandManager.executeByDeviceAction(device, action, value)
        }
    }

    fun parseColor(value: String): ULong {
        val color = value.replace("#", "")

        return try {
            color.toULong(16)
        } catch (_: Exception) {
            18408873036368314368UL
        }
    }

    private val parserRegex = """\{[^{}]+\}""".toRegex()
    fun formatValue(formatter: String?, value: String): String {
        if (formatter == null) return value

        val formatted = formatter.replace("{value}", value)
        if (formatted.contains("\$value")) {
            val parser = parserRegex.find(formatter)?.value ?: return formatted
            val updated = parser.replace("\$value", "(.+)").toRegex().find(value)?.value

            return formatted.replace(parserRegex, updated ?: "")
        }

        return formatted
    }

    fun newWidget() {
        val state = dialogState.value.newDashboard
        val name = dialogState.value.dashboardName

        _dialogState.update {
            DashboardDialogState(newDashboard = state, dashboardName = name)
        }
    }

    fun selectDashboard(dashboard: Dashboard) {
        _uiState.update { it.copy(selectedDashboard = dashboard.id, dashboard = dashboard) }
        _dialogState.update { it.copy(newDashboard = false, dashboardName = dashboard.name) }
    }

    fun filter(widgets: List<WidgetAndDevice>): List<WidgetAndDevice> {
        return widgets.filter { it.widget.dashboardId == uiState.value.selectedDashboard }
    }

    fun editWidget(widget: Widget) {

        val device = uiState.value.devices.find { it.id == widget.deviceId } ?: return

        _uiState.update { it.copy(selectedWidget = widget.id) }
        _dialogState.update {
            DashboardDialogState(
                name = widget.name,
                type = widget.type,
                device = device,
                icon = widget.icon,
                onColor = widget.onColor.toULong(),
                offColor = widget.offColor.toULong(),
                formatter = widget.formatter ?: "",
                subtext = widget.subtext ?: "",
                expanded = widget.expanded,
                useIcon = widget.useIcon
            )
        }
    }

    fun update(state: DashboardDialogState) {
        _dialogState.update {
            state
        }
    }

    fun save() {
        if(dialogState.value.device == null || uiState.value.selectedDashboard == null) return

        val widget = Widget(
            id = uiState.value.selectedWidget ?: UUID.randomUUID(),
            name = dialogState.value.name,
            type = dialogState.value.type,
            useIcon = dialogState.value.useIcon,
            formatter = dialogState.value.formatter.ifEmpty { null },
            subtext = dialogState.value.subtext.ifEmpty { null },
            icon = dialogState.value.icon,
            onColor = dialogState.value.onColor.toLong(),
            offColor = dialogState.value.offColor.toLong(),

            deviceId = dialogState.value.device!!.id,
            dashboardId = uiState.value.selectedDashboard!!,
            expanded = dialogState.value.expanded
        )

        viewModelScope.launch {
            if(uiState.value.selectedWidget != null)
                widgetRepository.update(widget)
            else
                widgetRepository.add(widget)
        }
    }

    fun delete(widget: Widget) {
        viewModelScope.launch {
            widgetRepository.delete(widget)
        }
    }

    fun deleteDashboard() {
        val dashboard = uiState.value.dashboard ?: return

        viewModelScope.launch {
            dashboardRepository.delete(dashboard)
        }
        _uiState.update {
            it.copy(
                selectedDashboard = null,
                dashboard = null
            )
        }
        _dialogState.update { it.copy(newDashboard = true) }
    }

    fun saveDashboard() {
        val id = uiState.value.selectedDashboard

        val dashboard = Dashboard(
            id = if(dialogState.value.newDashboard || id == null) UUID.randomUUID() else id,
            name = dialogState.value.dashboardName,
            order = 0
        )

        viewModelScope.launch {
            if(id == null || dialogState.value.newDashboard)
                dashboardRepository.add(dashboard)
            else
                dashboardRepository.update(dashboard)
        }
    }

    companion object {
        const val TAG = "DashboardViewModel"
    }
}