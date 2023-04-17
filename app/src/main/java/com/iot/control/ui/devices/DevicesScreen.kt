package com.iot.control.ui.devices

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.Device
import com.iot.control.model.enums.DeviceType
import com.iot.control.ui.connections.ContextMenuItem
import com.iot.control.ui.connections.SimpleChip
import com.iot.control.ui.theme.IotControlTheme
import com.iot.control.ui.utils.SimpleTextDialog
import com.iot.control.ui.dashboard.WidgetIcon
import com.iot.control.viewmodel.DevicesViewModel
import com.iot.control.viewmodel.SelectDeviceUiState
import java.util.*

@Composable
fun DevicesScreen(
    deviceViewModel: DevicesViewModel,
    toDeviceDialog: (UUID?, UUID) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val selectDeviceDialog = remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            NewDeviceExtendedFab(
                newDevice = {
                    toDeviceDialog(null, deviceViewModel.connectionId)
                },
                selectDevice = {
                    deviceViewModel.loadExistingDevices()
                    selectDeviceDialog.value = true
                },
                expanded)
       },
        floatingActionButtonPosition = FabPosition.End
    ) {
        val state by deviceViewModel.uiState.collectAsStateWithLifecycle()
        val selectState by deviceViewModel.selectState.collectAsStateWithLifecycle()

        DeviceList(
            devices = state.devices,
            { toDeviceDialog(it, deviceViewModel.connectionId) },
            { /*TODO("DELETE") */ }
        )

        if(selectDeviceDialog.value) {
            SelectExistingDeviceDialog(selectState, selectDeviceDialog, deviceViewModel::selectDevice)
        }
    }
}

@Composable
fun NewDeviceExtendedFab(
    newDevice: () -> Unit,
    selectDevice: () -> Unit,
    expanded: MutableState<Boolean>
)
{
    Box {
        AnimatedVisibility(visible=expanded.value) {
            FloatingActionButton(
                onClick = newDevice,
                modifier = Modifier
                    .offset(x = (-60).dp, y = (-70).dp)
                    .size(40.dp)
            ) { Icon(painterResource(R.drawable.baseline_add_12), null) }

            FloatingActionButton(
                onClick = selectDevice,
                modifier = Modifier
                    .offset(x = (0).dp, y = (-130).dp)
                    .size(40.dp)
            ) { Icon(painterResource(R.drawable.baseline_search_12), null) }
        }

        FloatingActionButton(
            onClick = {
                expanded.value = expanded.value.not()
            },
            modifier = Modifier
                .offset(x = (-10).dp, y = (-80).dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Filled.Add, null)
        }
    }
}

@Composable
fun DeviceList(
    devices: List<Device>,
    select: (UUID) -> Unit,
    delete: (Device) -> Unit
) {
    LazyColumn(state= rememberLazyListState()) {
        items(devices) { device ->
            DeviceItem(device, select, delete)
        }
        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}


@Composable
fun DeviceItem(device: Device,
               select: (UUID) -> Unit,
               delete: (Device) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .fillMaxWidth()
            .clickable { menuOpen = menuOpen.not() }
    ) {
        WidgetIcon(device.type)

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(device.name, style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(device.type.nameId), style=MaterialTheme.typography.bodyMedium, color=MaterialTheme.colorScheme.secondary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if(device.idDisplayable) SimpleChip(R.string.displayable_label)
                if(device.mqttConnectionId != null) SimpleChip(R.string.mqtt_label)
                if(device.smsConnectionId != null) SimpleChip(R.string.mqtt_label)
            }
        }
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 15.dp)) {
        if(menuOpen) {
            ContextMenuItem(R.string.edit_label) { select(device.id) }
            ContextMenuItem(R.string.delete_label) { delete(device) }
        }
    }
}

@Composable
fun SelectExistingDeviceDialog(
    state: SelectDeviceUiState,
    visibility: MutableState<Boolean>,
    select: (Device) -> Unit
) {
    val acceptVisibility = remember { mutableStateOf(false) }
    Dialog(onDismissRequest = { visibility.value = false }) {
        Card(
            modifier = Modifier.padding(all=15.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(modifier = Modifier.padding(all=10.dp)) {
                item {
                    Text("Devices without ${state.type.name}")
                    Divider()
                }
                items(state.withoutConnection) { device ->
                    SimpleDeviceItem(device) {
                        select(device)
                        visibility.value = false
                    }
                }
                item {
                    Text("Other devices")
                    Divider()
                }
                items(state.others) { device ->
                    SimpleDeviceItem(device) {
                        acceptVisibility.value = true
                    }
                    if(acceptVisibility.value) SimpleTextDialog(
                        "Replace connection for this device",
                        accept = {
                            select(device)
                            visibility.value = false },
                        acceptVisibility)
                }
            }
        }
    }
}

@Composable
fun SimpleDeviceItem(device: Device,
               select: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .fillMaxWidth()
            .clickable { select() }
    ) {
        WidgetIcon(device.type)

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(device.name, style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(device.type.nameId), style=MaterialTheme.typography.bodyMedium, color=MaterialTheme.colorScheme.secondary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if(device.idDisplayable) SimpleChip(R.string.displayable_label)
                if(device.mqttConnectionId != null) SimpleChip(R.string.mqtt_label)
                if(device.smsConnectionId != null) SimpleChip(R.string.mqtt_label)
            }
        }
    }
}


@Preview
@Composable
fun PreviewDeviceItem()
{
    IotControlTheme {
        DeviceItem(Device(name="Kitchen light", type=DeviceType.Light, mqttConnectionId = UUID.randomUUID(), smsConnectionId = UUID.randomUUID()), {}, {})
    }
}