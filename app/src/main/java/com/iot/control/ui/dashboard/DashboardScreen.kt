package com.iot.control.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.model.Device
import com.iot.control.model.WidgetAndDevice
import com.iot.control.model.enums.CommandAction
import com.iot.control.ui.connections.NewConnectionFab
import com.iot.control.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel
) {
    val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by dashboardViewModel.dialogState.collectAsStateWithLifecycle()
    val openDialog = remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = { NewConnectionFab(dashboardViewModel::newWidget, openDialog) },
        floatingActionButtonPosition = FabPosition.End
    ) {

        Column(modifier = Modifier.padding(it)) {
            TabRow(selectedTabIndex = selectedIndex) {
                state.dashboards.forEachIndexed { index, dashboard ->
                    if(state.selectedDashboard == null) {
                        dashboardViewModel.selectDashboard(dashboard)
                        selectedIndex = index
                    }

                    Tab(selected = (selectedIndex == index), onClick = {
                        selectedIndex = index
                        dashboardViewModel.selectDashboard(dashboard)
                    }) {
                        Text(dashboard.name)
                    }
                }
            }

            WidgetGrid(
                dashboardViewModel.filter(state.widgets),
                dashboardViewModel::formatValue,
                dashboardViewModel::parseColor,
                dashboardViewModel::send
            )
        }
    }

    if(openDialog.value)
        Dialog(
            onDismissRequest = { openDialog.value = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            WidgetDialog(openDialog, dashboardViewModel, dialogState, state)
        }
}

@Composable
fun WidgetGrid(
    widgets: List<WidgetAndDevice>,
    format: (String?, String) -> String,
    parseColor: (String) -> ULong,
    send: (Device, CommandAction, String?) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 165.dp)
    ) {
        widgets.forEach { item ->
            item(span = { if(item.widget.expanded) GridItemSpan(2) else GridItemSpan(1) }) {
                DeviceWidget(item.widget, item.device, format, parseColor, send)
            }
        }
    }
}

