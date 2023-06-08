package com.iot.control.ui.devices

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.enums.CommandMode
import com.iot.control.ui.scripts.EventDropdownMenu
import com.iot.control.ui.utils.DropdownMenuField
import com.iot.control.ui.utils.GeneralField
import com.iot.control.ui.utils.LabeledValue
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.DialogUiState

@Composable
fun EventDialog(
    dialogUiState: DialogUiState,
    title: String,
    isMqtt: Boolean,
    update: (DialogUiState) -> Unit,
    save: () -> Unit,
    cancel: () -> Unit
) {
    CommonDialog(dialogUiState, title, isMqtt, false, update , save, cancel) {

        if(!isMqtt)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.only_value))
                Switch(
                    checked = dialogUiState.onlyValue,
                    onCheckedChange = { update(dialogUiState.copy(onlyValue = it)) }
                )
            }

        EventDropdownMenu(value = dialogUiState.type.name, select = { update(dialogUiState.copy(type = it)) })

        GeneralField(
            title = stringResource(R.string.notify_options),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.enable_notification))
                Switch(
                    checked = dialogUiState.notify,
                    onCheckedChange = { update(dialogUiState.copy(notify = it)) }
                )
            }

            if (dialogUiState.notify) {
                OutlinedTextField(
                    label = { Text(stringResource(R.string.notification_label)) },
                    value = dialogUiState.notification,
                    onValueChange = { update(dialogUiState.copy(notification = it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
    }
}

@Composable
fun CommandDialog(
    dialogUiState: DialogUiState,
    title: String,
    isMqtt: Boolean,
    update: (DialogUiState) -> Unit,
    save: () -> Unit,
    cancel: () -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    CommonDialog(dialogUiState, title, isMqtt, true, update , save, cancel) {

        if(!isMqtt) Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.sync))
            Switch(
                checked = dialogUiState.isSync,
                onCheckedChange = { update(dialogUiState.copy(isSync = it)) }
            )
        }
        if (dialogUiState.isSync)
            DropdownMenuField(
                stringResource(R.string.action_label),
                value = dialogUiState.syncMode.name,
                expanded = expanded,
                modifier = Modifier.fillMaxWidth()
            ) {
                CommandMode.syncCommands().forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = { update(dialogUiState.copy(syncMode = item)) })
                }
            }
    }
}

@Composable
fun CommonDialog(
    dialogUiState: DialogUiState,
    title: String,
    isMqtt: Boolean,
    isCommand: Boolean,
    update: (DialogUiState) -> Unit,
    save: () -> Unit,
    cancel: () -> Unit,
    content: @Composable () -> Unit
) {
    TopBarDialog(title = title, close = cancel, save = save) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(isMqtt || (!dialogUiState.onlyValue && !isCommand))
                OutlinedTextField(
                    value = dialogUiState.topic,
                    label = { Text(stringResource(R.string.topic_label)) },
                    onValueChange = { update(dialogUiState.copy(topic = it)) },
                    modifier = Modifier.fillMaxWidth()
                )

            OutlinedTextField(
                value = dialogUiState.payload,
                label = { Text(stringResource(R.string.payload_label)) },
                onValueChange = { update(dialogUiState.copy(payload = it)) },
                modifier = Modifier.fillMaxWidth()
            )
//            LabeledValue(stringResource(R.string.use_auto), "") {
//                IconButton(onClick = { /*TODO*/ }) {
//                    Icon(imageVector = Icons.Filled.Search, contentDescription = "")
//                }
//            }

            if(isMqtt)
                GeneralField(title = stringResource(R.string.json_options), modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.json_payload_text))
                    Switch(
                        checked = dialogUiState.isJson,
                        onCheckedChange = { update(dialogUiState.copy(isJson = it)) }
                    )
                }

                if (dialogUiState.isJson)
                    OutlinedTextField(
                        label = { Text(stringResource(R.string.data_field_label)) },
                        value = dialogUiState.dataField,
                        onValueChange = { update(dialogUiState.copy(dataField = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
            }
            content()
        }
    }
}
