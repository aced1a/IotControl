package com.iot.control.ui.dashboard

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.model.Device
import com.iot.control.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel
) {
    val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    
    WidgetGrid(devices = state.devices)
}

@Composable
fun WidgetGrid(
    devices: List<Device>
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp)
    ) {
        items(devices) { device ->
            Widget(device)
        }
    }
}