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
import com.iot.control.model.enums.CommandAction
import com.iot.control.viewmodel.DialogUiState
import com.iot.control.viewmodel.Marked

@Composable
fun DetailDataDialog(
    dialogUiState: DialogUiState,
    update: (DialogUiState) -> Unit,
    save: () -> Unit,
    cancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = dialogUiState.topic,
            label = { Text("Topic") },
            onValueChange = { update(dialogUiState.copy(topic = it)) }
        )

        OutlinedTextField(
            value = dialogUiState.payload,
            label = { Text("Payload") },
            onValueChange = { update(dialogUiState.copy(payload = it)) }
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.json_payload_text))
            Checkbox(
                checked = dialogUiState.isJson,
                onCheckedChange = { update(dialogUiState.copy(isJson = it)) }
            )
        }
        if(dialogUiState.isJson) Row {
            Text("Data field")
            OutlinedTextField(
                value = dialogUiState.dataField,
                onValueChange = { update(dialogUiState.copy(dataField = it)) }
            )
        }

        Row {
            TextButton(
                onClick = cancel
            ) {
                Text(stringResource(R.string.cancel_label))
            }

            TextButton(
                onClick = save
            ) {
                Text(stringResource(R.string.save_label))
            }
        }
    }
}


@Composable
fun NewItemList(name: String, action: () -> Unit)
{
    Text(
        name,
        modifier = Modifier.fillMaxWidth().clickable { action() },
        style=MaterialTheme.typography.bodyMedium,
        color= MaterialTheme.colorScheme.secondary
    )
}