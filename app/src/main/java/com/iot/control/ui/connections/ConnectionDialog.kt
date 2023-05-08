package com.iot.control.ui.connections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.enums.ConnectionType
import com.iot.control.ui.theme.IotControlTheme
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.ConnectionDialogState


@Composable
fun ConnectionDialog(
    dialogState: ConnectionDialogState,
    onUpdate: (ConnectionDialogState) -> Unit,
    saveConnection: () -> Unit,
    openDialog: MutableState<Boolean>,
    modifier: Modifier
) {
    TopBarDialog(title = stringResource(R.string.new_connection_label), close = { openDialog.value = false }, save = saveConnection) {
        ConnectionDialogBody(
            dialogState = dialogState,
            onUpdate = onUpdate,
            modifier = modifier
        )
    }
}


@Composable
fun ConnectionDialogBody(
    dialogState: ConnectionDialogState,
    onUpdate: (ConnectionDialogState) -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp) ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            OutlinedTextField(
                value = dialogState.name,
                onValueChange = { onUpdate(dialogState.copy(name = it)) },
                label = { Text(stringResource(R.string.name_label)) }
            )

            ConnectionTypeToggleButton(dialogState, onUpdate)

            if(dialogState.type == ConnectionType.MQTT) MqttFields(dialogState, onUpdate)
            if(dialogState.type >= ConnectionType.MQTT) GenericMqttFields(dialogState, onUpdate)
            if(dialogState.type == ConnectionType.SMS) SmsFields(dialogState, onUpdate)

        }}
}

@Composable
fun ConnectionTypeToggleButton(
    dialogState: ConnectionDialogState,
    onUpdate: (ConnectionDialogState) -> Unit,
)
{
    Row(horizontalArrangement = Arrangement.Center) {

        ToggleButton(R.string.mqtt_label, 0, 0,
            dialogState.type == ConnectionType.MQTT ) {
            onUpdate(dialogState.copy(type = ConnectionType.MQTT))
        }

        ToggleButton(R.string.local_label, 1, 1,
            dialogState.type == ConnectionType.LOCAL_MQTT ) {
            onUpdate(dialogState.copy(type = ConnectionType.LOCAL_MQTT))
        }

        ToggleButton(R.string.sms_label, 2, 2,
            dialogState.type == ConnectionType.SMS )  {
            onUpdate(dialogState.copy(type = ConnectionType.SMS))
        }
    }
}

@Composable
fun ToggleButton(
    id: Int,
    number: Int,
    position: Int,
    checked: Boolean,
    callback: () -> Unit)
{
    OutlinedButton(
        onClick = callback,
        shape = when(position) {
            0 -> RoundedCornerShape(16.dp, 0.dp, 0.dp, 16.dp)
            2 -> RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp)
            else -> RoundedCornerShape(0.dp, 0.dp, 0.dp, 0.dp)
        },
        modifier = Modifier.offset((number * -1).dp, 0.dp),
        border = BorderStroke(1.dp,
            if(checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
        colors = if(checked)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else
                    ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
    ) { Text(stringResource(id)) }
}

@Composable
fun MqttFields(
    dialogState: ConnectionDialogState,
    onUpdate: (ConnectionDialogState) -> Unit
)
{
    OutlinedTextField(
        value = dialogState.address,
        label = { Text(stringResource(R.string.ip_label)) },
        onValueChange = { onUpdate(dialogState.copy(address = it)) })

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(stringResource(R.string.ssl_switcher_label))
        Switch(
            checked = dialogState.isSsl,
            onCheckedChange = { onUpdate(dialogState.copy(isSsl = it)) }
        )

        Spacer(modifier = Modifier.width(10.dp))

        OutlinedTextField(
            value = dialogState.port,
            label = { Text(stringResource(R.string.port_label)) },
            onValueChange = { onUpdate(dialogState.copy(port = it)) },
            modifier = Modifier.width(100.dp),
            keyboardOptions = KeyboardOptions(keyboardType=KeyboardType.Number)
        )
    }
}

@Composable
fun GenericMqttFields(
    dialogState: ConnectionDialogState,
    onUpdate: (ConnectionDialogState) -> Unit
)
{
    OutlinedTextField(
        value = dialogState.username,
        label = { Text(stringResource(R.string.username_label)) },
        onValueChange = { onUpdate(dialogState.copy(username = it)) })

    OutlinedTextField(
        value = dialogState.password,
        label = { Text(stringResource(R.string.password_label)) },
        onValueChange = { onUpdate(dialogState.copy(password = it)) },
        keyboardOptions = KeyboardOptions(keyboardType=KeyboardType.Password)
    )
}

@Composable
fun SmsFields(
    dialogState: ConnectionDialogState,
    onUpdate: (ConnectionDialogState) -> Unit
) {
    OutlinedTextField(
        value = dialogState.address,
        label = { Text(stringResource(R.string.number_label)) },
        onValueChange = { onUpdate(dialogState.copy(address = it)) },
        keyboardOptions = KeyboardOptions(keyboardType=KeyboardType.Number),
        maxLines = 1
    )

    OutlinedTextField(
        value = dialogState.parser,
        label = { Text(stringResource(R.string.parser_label)) },
        onValueChange = { onUpdate(dialogState.copy(parser = it)) },
        maxLines = 3
    )
}

@Preview
@Composable
fun PreviewConnectionDialog()
{
    IotControlTheme {
        ConnectionDialog(dialogState = ConnectionDialogState(), onUpdate = {}, {}, remember { mutableStateOf(true) }, Modifier)
    }
}