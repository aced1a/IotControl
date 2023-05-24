package com.iot.control.ui.scripts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.EventDto
import com.iot.control.model.Script
import com.iot.control.ui.connections.NewConnectionFab
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.ScriptDialogUiState
import com.iot.control.viewmodel.ScriptUiState
import com.iot.control.viewmodel.ScriptViewModel
import java.util.UUID

@Composable
fun ScriptScreen(
    scriptViewModel: ScriptViewModel
) {
    val openDialog = remember { mutableStateOf(false) }
    val state by scriptViewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by scriptViewModel.dialogState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = { NewScriptFab(newScript = { scriptViewModel.newScript() }, openDialog = openDialog)},
        floatingActionButtonPosition = FabPosition.End
    ) {
        Column(modifier = Modifier.padding(it)) {

            TriggerList(
                triggers = state.scripts,
                getName = scriptViewModel::getDeviceName,
                select = { id, script ->
                    scriptViewModel.editEventTriggeredScript(id, script)
                    openDialog.value = true
                }
            )
        }

        if(openDialog.value) {
            CallScriptDialog(
                dialogState,
                state,
                openDialog,
                scriptViewModel::updateDialogState,
                scriptViewModel::loadEventsForDevice,
                scriptViewModel::saveScript
            )
        }
    }
}


@Composable
fun CallScriptDialog(
    state: ScriptDialogUiState,
    screenState: ScriptUiState,
    openDialog: MutableState<Boolean>,
    update: (ScriptDialogUiState) -> Unit,
    loadEvents: (UUID) -> Unit,
    save: () -> Unit
) {
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { openDialog.value = false}
    ) {
        ScriptDialog(state, screenState , update, loadEvents, openDialog, save)
    }
}

@Composable
fun NewScriptFab(
  newScript: () -> Unit,
  openDialog: MutableState<Boolean>
) {
    FloatingActionButton(
        onClick = {
            newScript()
            openDialog.value = true
        },
        modifier = Modifier.offset(x = (-10).dp, y = (-10).dp)
    ) {
        Icon(Icons.Filled.Add, "Add icon")
    }
}

@Composable
fun TriggerList(
    triggers: Map<EventDto, List<Script>>,
    getName: (UUID) -> String,
    select: (UUID, Script) -> Unit,
) {
    LazyColumn {
        items(triggers.entries.toList()) {entry ->
            TriggerItem(
                trigger = entry.key,
                scripts = entry.value,
                getName,
                select
            )
        }
    }
}

@Composable
fun TriggerItem(
    trigger: EventDto,
    scripts: List<Script>,
    getName: (UUID) -> String,
    select: (UUID, Script) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        Modifier
            .padding(all = 15.dp)
            .fillMaxWidth()
            .clickable { expanded = expanded.not() },
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Row {
            Text(
                stringResource(R.string.device_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                trigger.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row {
            Text(
                stringResource(R.string.event_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                trigger.type.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if(expanded) {
            ScriptList(scripts, getName) {
                select(trigger.deviceId, it)
            }
        }
    }
}

@Composable
fun ScriptList(
    scripts: List<Script>,
    getName: (UUID) -> String,
    select: (Script) -> Unit
) {
    Column(
        modifier = Modifier.padding(all = 10.dp)
    ) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(scripts) { script ->
                ScriptItem(script = script, getName = getName, select = { select(script) })
            }
        }
    }
}

@Composable
fun ScriptItem(
    script: Script,
    getName: (UUID) -> String,
    select: () -> Unit
) {
    //TODO man... (i already have string resources)
    Card(
        modifier = Modifier.clickable { select() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(all = 5.dp)
        ) {
            Text("Guard: ${script.guard.name} ${script.guardValue.orEmpty()}")
            Text("Target: ${getName(script.deviceId)}")
            Text("Action: ${script.commandAction.name}")
        }
    }
}
