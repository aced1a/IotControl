package com.iot.control.ui.scripts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.EventDto
import com.iot.control.model.Script
import com.iot.control.model.Timer
import com.iot.control.ui.connections.ContextMenuItem
import com.iot.control.ui.connections.SimpleChip
import com.iot.control.ui.utils.DashedDivider
import com.iot.control.ui.utils.GeneralField
import com.iot.control.ui.utils.LabeledValue
import com.iot.control.viewmodel.ScriptDialogUiState
import com.iot.control.viewmodel.ScriptUiState
import com.iot.control.viewmodel.ScriptViewModel
import java.util.UUID

@Composable
fun ScriptScreen(
    scriptViewModel: ScriptViewModel
) {
    val state by scriptViewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by scriptViewModel.dialogState.collectAsStateWithLifecycle()

    var selectedIndex by remember { mutableStateOf(0) }
    val openDialog = remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = { NewScriptFab(newScript = { scriptViewModel.newScript() }, openDialog = openDialog)},
        floatingActionButtonPosition = FabPosition.End
    ) {
        Column(modifier = Modifier.padding(it)) {
            TabRow(selectedTabIndex = selectedIndex) {
                Tab(selected = (selectedIndex == 0), onClick = { selectedIndex = 0 }, modifier = Modifier.padding(all = 3.dp)) {
                    Text(stringResource(R.string.events_label))
                }
                Tab(selected = (selectedIndex == 1), onClick = { selectedIndex = 1 }, modifier = Modifier.padding(all = 3.dp)) {
                    Text(stringResource(R.string.timers_label))
                }
            }
            if(selectedIndex == 0)
                EventsTriggeredScripts(state, dialogState, scriptViewModel, state, openDialog)
            else
                TimersTriggeredScripts(state, dialogState, scriptViewModel, openDialog)
        }
    }
}

@Composable
fun EventsTriggeredScripts(
    state: ScriptUiState,
    dialogState: ScriptDialogUiState,
    scriptViewModel: ScriptViewModel,
    screenState: ScriptUiState,
    openDialog: MutableState<Boolean>
) {
    val groupDialog = remember { mutableStateOf(false) }

    LazyColumn {
        items(state.scripts.entries.toList()) {entry ->
            TriggerItem(
                trigger = entry.key,
                scripts = entry.value,
                scriptViewModel::getDeviceName,
                selectGroup = {
                    scriptViewModel.selectDto(it)
                    groupDialog.value = true
                }
            ) { id, script ->
                scriptViewModel.editEventTriggeredScript(id, script)
                openDialog.value = true
            }
        }
    }

    if(openDialog.value) {
        CallScriptDialog(
            openDialog
        ) {
            ScriptDialog(dialogState, state , scriptViewModel::updateDialogState, openDialog, scriptViewModel::saveScript)
        }
    }

    if(groupDialog.value) {
        CallScriptDialog(groupDialog) {
            EventTriggeredEventGroupDialog(groupDialog, scriptViewModel, screenState) {
                openDialog.value = true
            }
        }
    }
}

@Composable
fun TimersTriggeredScripts(
    state: ScriptUiState,
    dialogState: ScriptDialogUiState,
    scriptViewModel: ScriptViewModel,
    openDialog: MutableState<Boolean>
) {
    val editTimerDialog = remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.padding(all = 15.dp)
    ) {
        items(state.timers.entries.toList()) {entry ->
            TimerItem(
                timer = entry.key,
                scripts = entry.value,
                scriptViewModel::timerIsActive,
                scriptViewModel::toggleTimer,
                getName = scriptViewModel::getDeviceName,
                selectTimer = { timer ->
                    editTimerDialog.value = true
                    scriptViewModel.editTimer(timer.id)
                },
                deleteTimer = scriptViewModel::deleteTimer
            ) { id, script ->
                scriptViewModel.editTimerTriggeredScript(id, script)
                openDialog.value = true
            }
        }
    }

    if(openDialog.value) {
        CallScriptDialog(openDialog) {
            TimerTriggeredScriptDialog(
                dialogState,
                state,
                scriptViewModel::updateTime,
                scriptViewModel::updateDate,
                scriptViewModel::updateDialogState,
                scriptViewModel::selectTimer,
                openDialog,
                scriptViewModel::saveTimerTriggeredScript
            )
        }
    }

    if(editTimerDialog.value) {
        CallScriptDialog(editTimerDialog) {
            TimerDialog(editTimerDialog, dialogState, scriptViewModel) {
                openDialog.value = true
            }
        }
    }


}

@Composable
fun CallScriptDialog(
    openDialog: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { openDialog.value = false}
    ) {
        content()
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
fun TriggerItem(
    trigger: EventDto,
    scripts: List<Script>,
    getName: (UUID) -> String,
    selectGroup: (EventDto) -> Unit,
    select: (UUID, Script) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        Modifier
            .padding(horizontal = 15.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clickable { expanded = expanded.not() },
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Row {
            Text(
                stringResource(R.string.event_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                ": ${trigger.name} / ${trigger.type.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if(expanded) {
            GeneralField(stringResource(R.string.scripts)) {
                ScriptList(scripts, getName) {
                    select(trigger.deviceId, it)
                }
                DashedDivider()
            }

            ContextMenuItem(R.string.edit_label) { selectGroup(trigger) }
        }
    }
}

@Composable
fun TimerItem(
    timer: Timer,
    scripts: List<Script>,
    isActive: (Timer) -> Boolean,
    toggle: (Timer) -> Boolean,
    getName: (UUID) -> String,
    selectTimer: (Timer) -> Unit,
    deleteTimer: (Timer) -> Unit,
    select: (UUID, Script) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(isActive(timer)) }

    Row(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
            .clickable { expanded = expanded.not() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if(timer.date != null)
                LabeledValue(stringResource(R.string.time_label), timer.formatDate(), space = false) {}

            if(timer.repeat)
                LabeledValue(
                    stringResource(R.string.interval_label),
                    stringResource(R.string.hms_format, timer.interval / 3600000, timer.interval/60000%60, timer.interval/1000%60),
                    space = false
                ) {}
            Row {
                if(timer.repeat) SimpleChip(R.string.repeat_label)
                if(timer.initOnBoot) SimpleChip(R.string.on_boot_label)
            }
        }

        Switch(checked = active, onCheckedChange = { active = toggle(timer) })
    }
    if(expanded) {
        GeneralField(stringResource(R.string.scripts)) {
            ScriptList(scripts, getName) {
                select(timer.id, it)
            }
            DashedDivider()
        }

        ContextMenuItem(R.string.edit_label) { selectTimer(timer) }
        ContextMenuItem(R.string.delete_label) { deleteTimer(timer) }
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
    Card(
        modifier = Modifier.clickable { select() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(all = 5.dp)
        ) {
            Text(stringResource(R.string.guard_place, script.guard.name))
            Text(stringResource(R.string.script_device_label, getName(script.deviceId)))
            Text(stringResource(R.string.script_action_label, script.commandAction.name))
        }
    }
}
