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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.Device
import com.iot.control.ui.connections.ContextMenuItem
import com.iot.control.ui.connections.SimpleChip
import com.iot.control.ui.utils.DashedDivider
import com.iot.control.ui.utils.LabeledValue
import com.iot.control.ui.utils.SimpleTextDialog
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
                leftSelect = {
                    toDeviceDialog(null, deviceViewModel.connectionId)
                },
                topSelect = {
                    deviceViewModel.loadExistingDevices()
                    selectDeviceDialog.value = true
                },
                expanded)
       },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        val state by deviceViewModel.uiState.collectAsStateWithLifecycle()
        val selectState by deviceViewModel.selectState.collectAsStateWithLifecycle()

        DeviceList(
            devices = state.devices,
            { toDeviceDialog(it, deviceViewModel.connectionId) },
            deviceViewModel::delete,
            modifier = Modifier.padding(padding)
        )

        if(selectDeviceDialog.value) {
            SelectExistingDeviceDialog(selectState, selectDeviceDialog, deviceViewModel::selectDevice)
        }
    }
}

@Composable
fun NewDeviceExtendedFab(
    leftSelect: () -> Unit,
    topSelect: () -> Unit,
    expanded: MutableState<Boolean>
) {
    Box {
        AnimatedVisibility(visible=expanded.value) {
            FloatingActionButton(
                onClick = leftSelect,
                modifier = Modifier
                    .offset(x = (-60).dp, y = (-5).dp)
                    .size(40.dp)
            ) { Icon(painterResource(R.drawable.baseline_add_12), null) }

            FloatingActionButton(
                onClick = topSelect,
                modifier = Modifier
                    .offset(x = (-5).dp, y = (-60).dp)
                    .size(40.dp)
            ) { Icon(painterResource(R.drawable.baseline_search_12), null) }
        }

        FloatingActionButton(
            onClick = {
                expanded.value = expanded.value.not()
            },
            modifier = Modifier
                .offset(x = (-10).dp, y = (-10).dp)
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
    delete: (Device) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(state= rememberLazyListState(), modifier = modifier) {
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
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(device.name, style = MaterialTheme.typography.headlineSmall)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {

                if(device.mqttConnectionId != null) SimpleChip(R.string.mqtt_label)
                if(device.smsConnectionId != null) SimpleChip(R.string.sms_label)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectExistingDeviceDialog(
    state: SelectDeviceUiState,
    visibility: MutableState<Boolean>,
    select: (Device) -> Unit
) {
    val acceptVisibility = remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = { visibility.value = false }) {
        Card(
            modifier = Modifier.padding(all=15.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(modifier = Modifier.padding(all=10.dp)) {
                item {
                    Text(stringResource(R.string.devices_without, state.type.name))
                    Divider()
                }
                item {
                    if(state.withoutConnection.isEmpty()) {
                        Text(
                            stringResource(R.string.no_devices_found),
                            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
                items(state.withoutConnection) { device ->
                    SimpleDeviceItem(device, modifier = Modifier.fillMaxWidth()) {
                        select(device)
                        visibility.value = false
                    }
                    DashedDivider()
                }
                item {
                    Text(stringResource(R.string.devices_other))
                    Divider()
                }
                item {
                    if(state.others.isEmpty()) {
                        Text(
                            stringResource(R.string.no_devices_found),
                            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
                items(state.others) { device ->
                    SimpleDeviceItem(device, modifier = Modifier.fillMaxWidth()) {
                        acceptVisibility.value = true
                    }
                    DashedDivider()

                    if(acceptVisibility.value) SimpleTextDialog(
                        stringResource(R.string.change_connection),
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
fun SimpleDeviceItem(
    device: Device,
    modifier: Modifier = Modifier,
    select: () -> Unit
) {
    LabeledValue(device.name, "", modifier = modifier
        .padding(horizontal = 10.dp)
        .clickable { select() }, space = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if(device.mqttConnectionId != null) SimpleChip(R.string.mqtt_label)
            if(device.smsConnectionId != null) SimpleChip(R.string.sms_label)
        }
    }

}