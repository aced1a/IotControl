package com.iot.control.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.enums.DeviceType
import com.iot.control.ui.dashboard.WidgetIcon
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
                        deviceDetailViewModel.newEvent(it)
                        eventDialog.value = true
                    }
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
                    }
                )
            }
        )

        if(commandDialog.value) CallDialog(commandDialog) {
            CommandDialog(
                dialogState,
                stringResource(R.string.add_new_command, ""),
                deviceDetailViewModel::updateDialogState ,
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
                deviceDetailViewModel::updateDialogState ,
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
    val selectTypeDialogVisible = remember { mutableStateOf(false) }

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

            DeviceTypeItem(
                type = uiState.type,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 40.dp)
                    .width(200.dp),
                callback = { selectTypeDialogVisible.value = true }
            )

            commandList()
            eventList()

            if(selectTypeDialogVisible.value)
                CallSelectDeviceTypeDialog(selectTypeDialogVisible) {
                    update(uiState.copy(type = it))
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallSelectDeviceTypeDialog(
    visible: MutableState<Boolean>,
    callback: (DeviceType) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { visible.value = false },
        //properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        SelectDeviceTypeDialog(visible, callback)
    }
}

@Composable
fun SelectDeviceTypeDialog(
    visible: MutableState<Boolean>,
    callback: (DeviceType) -> Unit
)
{
   Card(
       colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) ,
       shape = MaterialTheme.shapes.medium,
       modifier = Modifier
           .padding(all = 15.dp)
    ) {
        LazyColumn {
            items(DeviceType.values()) { type ->
                DeviceTypeItem(type, Modifier.fillMaxWidth()) {
                    callback(it)
                    visible.value = false
                }
            }
            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
fun DeviceTypeItem(
    type: DeviceType,
    modifier: Modifier = Modifier,
    callback: (DeviceType) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(all = 5.dp)
            .clickable { callback(type) }
    ) {
        WidgetIcon(type)

        Text(stringResource(type.nameId), style = MaterialTheme.typography.headlineSmall)
    }
}