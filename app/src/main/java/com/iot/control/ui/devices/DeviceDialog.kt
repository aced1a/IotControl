package com.iot.control.ui.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.enums.DeviceType
import com.iot.control.ui.dashboard.WidgetIcon
import com.iot.control.viewmodel.DeviceDetailViewModel
import com.iot.control.viewmodel.DialogUiState


@Composable
fun DeviceDialog(
    deviceDetailViewModel: DeviceDetailViewModel,
    modifier: Modifier = Modifier,
    back: () -> Unit
) {
    val uiState by deviceDetailViewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by deviceDetailViewModel.dialogState.collectAsStateWithLifecycle()

    val commandListVisible = remember { mutableStateOf(false) }
    val eventListVisible = remember { mutableStateOf(false) }

    val selectTypeDialogVisible = remember { mutableStateOf(false) }
    val dialogVisible = remember { mutableStateOf(false) }

    val isCommandDialog = remember { mutableStateOf(true) }

    val update = deviceDetailViewModel::updateUiState

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
                label = { Text("Name") }
            )

            DeviceTypeItem(
                type = uiState.type,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 40.dp)
                    .width(200.dp),
                callback = { selectTypeDialogVisible.value = true }
            )

            CommandList(
                uiState.commands,
                commandListVisible,
                {
                    deviceDetailViewModel.editCommand(it)
                    dialogVisible.value = true
                    isCommandDialog.value = true
                },
                {
                    deviceDetailViewModel.newCommand(it)
                    dialogVisible.value = true
                    isCommandDialog.value = true
                })

            EventList(
                events = uiState.events,
                visible = eventListVisible,
                editEvent = {
                    deviceDetailViewModel.editEvent(it)
                    dialogVisible.value = true
                    isCommandDialog.value = false },
                addEvent = {
                    deviceDetailViewModel.newEvent(it)
                    dialogVisible.value = true
                    isCommandDialog.value = false }
            )

            Row {
                TextButton(
                    onClick = { back() }
                ) {
                    Text(stringResource(R.string.cancel_label))
                }

                TextButton(
                    onClick = {
                        deviceDetailViewModel.save()
                        back()
                    }
                ) {
                    Text(stringResource(R.string.save_label))
                }
            }

            if(selectTypeDialogVisible.value)
                CallSelectDeviceTypeDialog(selectTypeDialogVisible) {
                    update(uiState.copy(type = it))
                }

            if(dialogVisible.value) CallDialog(
                dialogState,
                dialogVisible,
                deviceDetailViewModel::updateDialogState) {
                    if(isCommandDialog.value)
                        deviceDetailViewModel.saveCommand()
                    else
                        deviceDetailViewModel.saveEvent()
                    dialogVisible.value = dialogVisible.value.not()
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallDialog(
    dialogState: DialogUiState,
    openDialog: MutableState<Boolean>,
    update: (DialogUiState) -> Unit,
    save: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { openDialog.value = false}
    ) {
        DetailDataDialog(
            dialogState,
            update = update,
            save = save,
            cancel = { openDialog.value = false }
        )
    }
}

@Composable
fun CallSelectDeviceTypeDialog(
    visible: MutableState<Boolean>,
    callback: (DeviceType) -> Unit
) {
    Dialog(
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