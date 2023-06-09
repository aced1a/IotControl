package com.iot.control.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.LogMessage
import com.iot.control.ui.utils.DashedDivider
import com.iot.control.ui.utils.DropdownMenuField
import com.iot.control.ui.utils.GeneralField
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.SettingsUiState
import com.iot.control.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
  settingsViewModel: SettingsViewModel
) {
    val state by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val brokerDialog = remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        ConnectionsSettings(settingsViewModel, state, brokerDialog)

        SettingsFieldsGroup(
            stringResource(R.string.general),
            listOf(
                { LanguageSettings(settingsViewModel, state) },
                { LogSettings(state, settingsViewModel::delete) }
            )
        )
    }

    if(brokerDialog.value) BrokerSettingsDialog(settingsViewModel, state, brokerDialog)
}

@Composable
fun ConnectionsSettings(
    settingsViewModel: SettingsViewModel,
    state: SettingsUiState,
    brokerDialog: MutableState<Boolean>
) {
    val context = LocalContext.current.applicationContext
    settingsViewModel.updateLocale()

    SettingsFieldsGroup(
        stringResource(R.string.connections),
        listOf(
            { SettingsSwitchField(
                stringResource(R.string.enable_mqtt_text),
                value = state.connectionsRunning,
                onCheckedChange = { settingsViewModel.toggleMqttConnections() })},
            { SettingsSwitchField(
                stringResource(R.string.enable_broker_text),
                value = state.brokerRunning,
                modifier = Modifier.clickable { brokerDialog.value = true },
                onCheckedChange = { settingsViewModel.toggleMqttBroker(context) })},
            { SettingsSwitchField(
                stringResource(R.string.enable_sms_text),
                value = state.smsClientRunning,
                onCheckedChange = { settingsViewModel.toggleSmsClient(context) })}
        )
    )
}

@Composable
fun LanguageSettings(
    settingsViewModel: SettingsViewModel,
    state: SettingsUiState
) {
    val expanded = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.language_label), modifier = Modifier.padding(start = 10.dp))
        Spacer(modifier = Modifier.width(50.dp))
        DropdownMenuField("", stringResource(state.locale), expanded) {
            settingsViewModel.languageList.forEach {
                DropdownMenuItem({ Text(stringResource(it)) }, onClick = { settingsViewModel.changeLanguage(context, it) })
            }
        }
    }
}

@Composable
fun BrokerSettingsDialog(
    settingsViewModel: SettingsViewModel,
    state: SettingsUiState,
    visible: MutableState<Boolean>
) {
    Dialog(
        onDismissRequest = { visible.value = false },
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        TopBarDialog(title = "Broker settings", close = { visible.value = false}, save = { visible.value = false }) {
            BrokerSettings(settingsViewModel, state)
        }
    }
}

@Composable
fun BrokerSettings(
    settingsViewModel: SettingsViewModel,
    state: SettingsUiState
) {
    val expanded = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    )
    {
        OutlinedTextField(
            "",
            label= { Text(stringResource(R.string.ip_label)) },
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )

        Row {
            DropdownMenuField("Type", "MQTT", expanded, modifier = Modifier.width(200.dp)) {
                listOf("mqtt", "ws", "mqtts", "wss").forEach { item ->
                    DropdownMenuItem({ Text(item) }, onClick = { expanded.value = false })
                }
            }
            Spacer(modifier = Modifier.width(50.dp))

            OutlinedTextField(value = "1883", label = { Text(stringResource(R.string.port_label))}, onValueChange = {})
        }

        GeneralField("Ssl") {}

        GeneralField("Connected users") {}
    }
}


@Composable
fun SettingsFieldsGroup(
    title: String,
    items: List<@Composable () -> Unit>,
) {
    Column {
        Text(
            title,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Surface(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 5.dp),
            shape = MaterialTheme.shapes.large,
            shadowElevation = 3.dp
        ){

            Column {
                Spacer(modifier = Modifier.height(10.dp))
                items.forEachIndexed { index, item ->
                    item()
                    if(index == items.size - 1)
                        Spacer(modifier = Modifier.height(10.dp))
                    else
                        Divider(thickness = 1.dp)
                }
            }
        }
    }
}



@Composable
fun SettingsSwitchField(
    label: String,
    value: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
){
    Row(
        modifier = modifier
            .padding(horizontal = 5.dp, vertical = 2.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(label, modifier = Modifier.padding(start = 5.dp))
        Switch(
            checked = value,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(end = 5.dp)
        )
    }
}

@Composable
fun LogSettings(
    state: SettingsUiState,
    delete: () -> Unit
) {
    val dialog = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
            .clickable { dialog.value = true },
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(stringResource(R.string.history_label), modifier = Modifier.padding(start = 15.dp, top = 5.dp, bottom = 10.dp))
    }

    if(dialog.value) LogDialog(dialog, state, delete)
}

@Composable
fun LogDialog(
    open: MutableState<Boolean>,
    state: SettingsUiState,
    delete: () -> Unit
) {
    Dialog(
        onDismissRequest = { open.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        TopBarDialog(title = stringResource(R.string.history_label), close = { open.value = false }, save = { open.value = false }) {
            LazyColumn {
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { delete() }) {
                            Text(stringResource(R.string.delete_label), color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    DashedDivider(modifier = Modifier.padding(bottom = 5.dp))
                }
                items(state.logs) { log ->
                    LogRow(log)
                    DashedDivider()
                }
            }
        }
    }
}

@Composable
fun LogRow(message: LogMessage) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Text(message.date.toString(), color = MaterialTheme.colorScheme.secondary)

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                getString(message),
                color = MaterialTheme.colorScheme.secondary
            )
            if(message.resolved) Text(message.name.toString())
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(stringResource(R.string.address_label), color = MaterialTheme.colorScheme.secondary)
            Text(message.address ?: "-")
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(stringResource(R.string.topic_label), color = MaterialTheme.colorScheme.secondary)
            Text(message.topic ?: "-")
        }

    if(expanded)
        Text(message.message, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun getString(message: LogMessage): String {
    return if(message.resolved)
        if(message.event) stringResource(R.string.event_label) else stringResource(R.string.command_label)
    else
        stringResource(R.string.msg_label)
}
