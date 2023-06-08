package com.iot.control.ui.scripts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Device
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.EventType
import com.iot.control.model.enums.ScriptGuard
import com.iot.control.ui.devices.SimpleDeviceItem
import com.iot.control.ui.utils.DashedDivider
import com.iot.control.ui.utils.DeviceSelectField
import com.iot.control.ui.utils.DropdownMenuField
import com.iot.control.ui.utils.SelectItemDialog
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.ScriptDialogUiState
import com.iot.control.viewmodel.ScriptUiState

@Composable
fun ScriptDialog(
    state: ScriptDialogUiState,
    screenState: ScriptUiState,
    update: (ScriptDialogUiState) -> Unit,
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
        ScriptDialogBody(state, screenState, update)
    }
}


@Composable
fun ScriptDialogBody(
    state: ScriptDialogUiState,
    screenState: ScriptUiState,
    update: (ScriptDialogUiState) -> Unit
) {
    val selectDeviceVisibility = remember { mutableStateOf(false) }

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
                }
            )

            EventDropdownMenu(
                state.event.name,
                select = { update(state.copy(event = it)) }
            )
            Spacer(modifier = Modifier.height(10.dp))

            TargetCommandFields(state, screenState, update)

            if(selectDeviceVisibility.value)
                CallSelectDeviceDialog(
                    devices = screenState.devices,
                    select = {
                        update(state.copy(source = it))
                    },
                    visibility = selectDeviceVisibility
                )
        }
    }
}


@Composable
fun TargetCommandFields(
    state: ScriptDialogUiState,
    screenState: ScriptUiState,
    update: (ScriptDialogUiState) -> Unit
)
{
    val selectDeviceVisibility = remember { mutableStateOf(false) }

    DeviceSelectField(
        title = stringResource(R.string.target_device_label),
        device = state.target,
        select = {
            selectDeviceVisibility.value = true
        }
    )

    ActionsDropdownMenu(
        value = state.command.name,
        select = { update(state.copy(command = it)) }
    )

    OutlinedTextField(
        value = state.actionValue,
        label = { Text(stringResource(R.string.action_val))},
        onValueChange = { update(state.copy(actionValue = it))},
        modifier = Modifier.fillMaxWidth()
    )

    if(screenState.selectedTimer == null) {
        GuardsDropdownMenu(
            value = state.guard.name,
            select = { update(state.copy(guard = it))}
        )

        if(state.guard != ScriptGuard.No)
            OutlinedTextField(
                value = state.guardValue,
                label = { Text(stringResource(R.string.guard_val)) },
                onValueChange = { update(state.copy(guardValue = it))},
                modifier = Modifier.fillMaxWidth()
            )
    }

    if(selectDeviceVisibility.value)
        CallSelectDeviceDialog(
            devices = screenState.devices,
            select = {
                update(state.copy(target = it))
            },
            visibility = selectDeviceVisibility
        )
}

@Composable
fun EventDropdownMenu(
    value: String,
    select: (EventType) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }

    DropdownMenuField(
        label = stringResource(R.string.event_label),
        value = value,
        expanded = expanded,
        modifier = Modifier.fillMaxWidth()
    ) {
        EventType.values().forEach {
            DropdownMenuItem(
                text = { Text(it.name) },
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
        expanded = expanded,
        modifier = Modifier.fillMaxWidth()
    ) {
        CommandAction.values().forEach {
            DropdownMenuItem(text = { Text(it.name)}, onClick = {
                select(it)
                expanded.value = false
            })
        }
    }
}

@Composable
fun GuardsDropdownMenu(
    value: String,
    select: (ScriptGuard) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }

    DropdownMenuField(
        label = stringResource(R.string.guard_label),
        value = value,
        expanded = expanded,
        modifier = Modifier.fillMaxWidth()
    ) {
        ScriptGuard.values().forEach {
            DropdownMenuItem(text = { Text(it.name)}, onClick = {
                select(it)
                expanded.value = false
            })
        }
    }
}

@Composable
fun CallSelectDeviceDialog(
    devices: List<Device>,
    select: (Device) -> Unit,
    visibility: MutableState<Boolean>
) {
    SelectItemDialog(visibility) {
        LazyColumn(modifier = Modifier.padding(all = 10.dp)) {
            items(devices) { device ->
                SimpleDeviceItem(device, modifier = Modifier.fillMaxWidth()) {
                    select(device)
                    visibility.value = false
                }
                DashedDivider()
            }
        }
    }
}