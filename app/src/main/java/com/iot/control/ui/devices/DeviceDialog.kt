package com.iot.control.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.DeviceDetailUiState
import com.iot.control.viewmodel.DeviceDetailViewModel


@Composable
fun DeviceDialog(
    deviceDetailViewModel: DeviceDetailViewModel,
    modifier: Modifier = Modifier,
    back: () -> Unit
) {
    TopBarDialog(
        title = stringResource(R.string.new_device_label),
        close = back,
        save = {
            deviceDetailViewModel.save()
            back()
    }) {
        val uiState by deviceDetailViewModel.uiState.collectAsStateWithLifecycle()
        val dialogState by deviceDetailViewModel.dialogState.collectAsStateWithLifecycle()

        val commandDialog = remember { mutableStateOf(false) }
        val eventDialog = remember { mutableStateOf(false) }


        DeviceDialogBody(
            uiState,
            deviceDetailViewModel::updateUiState,
            modifier,
            eventList = {
                EventList(
                    events = uiState.events,
                    editEvent = {
                        deviceDetailViewModel.editEvent(it)
                        eventDialog.value = true

                    }, addEvent = {
                        deviceDetailViewModel.newEvent()
                        eventDialog.value = true
                    },
                    deleteEvent = deviceDetailViewModel::deleteEvent
                )
            },
            commandList = {
                CommandList(
                    commands = uiState.commands,
                    editCommand = {
                        deviceDetailViewModel.editCommand(it)
                        commandDialog.value = true
                    },
                    addCommand = {
                        deviceDetailViewModel.newCommand(it)
                        commandDialog.value = true
                    },
                    deleteCommand = deviceDetailViewModel::deleteCommand
                )
            }
        )

        if(commandDialog.value) CallDialog(commandDialog) {
            CommandDialog(
                dialogState,
                stringResource(R.string.add_new_command, ""),
                uiState.isMqtt,
                deviceDetailViewModel::updateDialogState,
                save = {
                    deviceDetailViewModel.saveCommand()
                    commandDialog.value = false
                },
                cancel = { commandDialog.value = false }
            )
        }
        if(eventDialog.value) CallDialog(eventDialog) {
            EventDialog(
                dialogState,
                stringResource(R.string.add_new_event, ""),
                uiState.isMqtt,
                deviceDetailViewModel::updateDialogState,
                save = {
                    deviceDetailViewModel.saveEvent()
                    eventDialog.value = false
                },
                cancel = { eventDialog.value = false }
            )
        }
    }
}

@Composable
fun DeviceDialogBody(
    uiState: DeviceDetailUiState,
    update: (DeviceDetailUiState) -> Unit,
    modifier: Modifier = Modifier,
    eventList: @Composable () -> Unit,
    commandList: @Composable () -> Unit
) {

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(all = 5.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { update(uiState.copy(name = it)) },
                label = { Text(stringResource(R.string.name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
            )

            commandList()
            eventList()
        }
    }
}


@Composable
fun CallDialog(
    openDialog: MutableState<Boolean>,
    dialog: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = { openDialog.value = false},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        dialog()
    }
}
