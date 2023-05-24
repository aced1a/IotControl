package com.iot.control.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Command
import com.iot.control.model.Device
import com.iot.control.model.enums.CommandAction
import com.iot.control.ui.utils.GeneralField
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.DialogUiState
import com.iot.control.viewmodel.Marked

@Composable
fun EventDialog(
    dialogUiState: DialogUiState,
    title: String,
    update: (DialogUiState) -> Unit,
    save: () -> Unit,
    cancel: () -> Unit
) {
    CommonDialog(dialogUiState, title , update , save, cancel) {
        GeneralField(title = stringResource(R.string.notify_options), modifier = Modifier.fillMaxWidth().padding(top=5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
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
    update: (DialogUiState) -> Unit,
    save: () -> Unit,
    cancel: () -> Unit
) {
    CommonDialog(dialogUiState, title , update , save, cancel) {}
}

@Composable
fun CommonDialog(
    dialogUiState: DialogUiState,
    title: String,
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

            GeneralField(title = stringResource(R.string.json_options), modifier = Modifier.fillMaxWidth().padding(top=5.dp)) {

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
