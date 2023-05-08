package com.iot.control.ui.scripts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Device
import com.iot.control.model.Event
import com.iot.control.model.enums.CommandAction
import com.iot.control.ui.devices.SimpleDeviceItem
import com.iot.control.ui.utils.DeviceSelectField
import com.iot.control.ui.utils.DropdownMenuField
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.ScriptDialogUiState
import com.iot.control.viewmodel.ScriptUiState
import java.util.UUID

@Composable
fun ScriptDialog(
    state: ScriptDialogUiState,
    screenState: ScriptUiState,
    update: (ScriptDialogUiState) -> Unit,
    loadEvents: (UUID) -> Unit,
    openDialog: MutableState<Boolean>,
    save: () -> Unit
) {
    TopBarDialog(
        title = stringResource(R.string.new_script_label),
        close = { openDialog.value = false },
        save = {
            save()
            openDialog.value = false
        }
    ) {
        ScriptDialogBody(state, screenState , update , loadEvents)
    }
}


@Composable
fun ScriptDialogBody(
    state: ScriptDialogUiState,
    screenState: ScriptUiState,
    update: (ScriptDialogUiState) -> Unit,
    loadEvents: (UUID) -> Unit
) {
    val selectDeviceVisibility = remember { mutableStateOf(false) }
    var selectTargetDevice by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.padding(horizontal = 30.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            DeviceSelectField(
                title= stringResource(R.string.source_device_label),
                device = state.source,
                select = {
                    selectDeviceVisibility.value = true
                    selectTargetDevice = false
                }
            )

            EventDropdownMenu(
                state.event?.type?.name ?: stringResource(R.string.not_selected),
                screenState.events,
                select = { update(state.copy(event = it)) }
            )
            Spacer(modifier = Modifier.height(10.dp))

            DeviceSelectField(
                title = stringResource(R.string.target_device_label),
                device = state.target,
                select = {
                    selectDeviceVisibility.value = true
                    selectTargetDevice = true
                }
            )

            ActionsDropdownMenu(
                value = state.command.name,
                select = { update(state.copy(command = it)) }
            )

            if(selectDeviceVisibility.value)
                CallSelectDeviceDialog(
                    devices = screenState.devices,
                    select = {
                        if(selectTargetDevice)
                            update(state.copy(target = it))
                        else {
                            update(state.copy(source = it))
                            loadEvents(it.id)
                        }
                    },
                    visibility = selectDeviceVisibility
                )
        }
    }
}

@Composable
fun EventDropdownMenu(
    value: String,
    events: List<Event>,
    select: (Event) -> Unit
) {
    var expanded = remember { mutableStateOf(false) }

    DropdownMenuField(
        label = stringResource(R.string.event_label),
        value = value,
        expanded = expanded
    ) {
        events.forEach {
            DropdownMenuItem(
                text = { Text(it.type.name) },
                onClick = {
                    select(it)
                    expanded.value = false
                }
            )
        }
    }
}

@Composable
fun ActionsDropdownMenu(
    value: String,
    select: (CommandAction) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }

    DropdownMenuField(
        label = stringResource(R.string.action_label),
        value = value,
        expanded = expanded
    ) {
        CommandAction.values().forEach {
            DropdownMenuItem(text = { Text(it.name)}, onClick = {
                select(it)
                expanded.value = false
            })
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallSelectDeviceDialog(
    devices: List<Device>,
    select: (Device) -> Unit,
    visibility: MutableState<Boolean>
) {
    ModalBottomSheet(onDismissRequest = { visibility.value = false }) {
        Card(
            modifier = Modifier.padding(all = 15.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(modifier = Modifier.padding(all = 10.dp)) {
                items(devices) { device ->
                    SimpleDeviceItem(device) {
                        select(device)
                        visibility.value = false
                    }
                }
            }
        }
    }
}