package com.iot.control.ui.scripts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Script
import com.iot.control.model.Timer
import com.iot.control.ui.connections.SimpleChip
import com.iot.control.ui.devices.NewItemList
import com.iot.control.ui.utils.DashedDivider
import com.iot.control.ui.utils.LabeledValue
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.ScriptDialogUiState
import com.iot.control.viewmodel.ScriptViewModel

@Composable
fun TimerDialog(
    openDialog: MutableState<Boolean>,
    state: ScriptDialogUiState,
    scriptViewModel: ScriptViewModel,
    openScriptDialog: () -> Unit)
{
    TopBarDialog(
        title = stringResource(R.string.new_script_label),
        close = { openDialog.value = false },
        save = {
            scriptViewModel.saveTimer()
            openDialog.value = false
        }
    ) {
        Surface(modifier = Modifier.padding(horizontal = 30.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                DashedDivider()
                TimerOptions(
                    state,
                    scriptViewModel::updateDialogState,
                    scriptViewModel::updateDate,
                    scriptViewModel::updateTime
                )
                DashedDivider()

                Spacer(modifier = Modifier.height(10.dp))

                TimerScriptList(
                    scriptViewModel,
                    scriptViewModel.getScriptsForTimer(),
                    editScript =  { script ->
                        val timer = scriptViewModel.uiState.value.selectedTimer
                        if(timer != null) {
                            scriptViewModel.editTimerTriggeredScript(timer, script)
                            openScriptDialog()
                        }
                    },
                    newScript = {
                        val timerId = scriptViewModel.uiState.value.selectedTimer
                        val timer = scriptViewModel.uiState.value.timer

                        scriptViewModel.newScript(timerId, timer)
                        openScriptDialog()
                    },
                    delete = scriptViewModel::deleteScript
                )
            }
        }
    }
}

@Composable
fun TimerScriptList(
    scriptViewModel: ScriptViewModel,
    scripts: List<Script>,
    editScript: (Script) -> Unit,
    newScript: () -> Unit,
    delete: (Script) -> Unit
) {
    LazyColumn {

        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .fillMaxWidth(),
            ) {
                Text(stringResource(R.string.name_label), color = MaterialTheme.colorScheme.secondary)
                Text(stringResource(R.string.commands_label), color = MaterialTheme.colorScheme.secondary)
                Text(stringResource(R.string.action_label), color = MaterialTheme.colorScheme.secondary)
            }

            DashedDivider()

            NewItemList(stringResource(R.string.new_script_label), center = true) { newScript() }
        }
        items(scripts) { script ->
            LabeledValue(
                scriptViewModel.getDeviceName(script.deviceId),
                script.commandAction.name,
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .fillMaxWidth()
            ) {
                Row {
                    IconButton(onClick = { editScript(script) }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit event")
                    }
                    IconButton(onClick = { delete(script) }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete event")
                    }
                }
            }
            DashedDivider()
        }
    }
}

@Composable
fun SimpleTimer(
    timer: Timer?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clickable { onClick() }
            .padding(bottom = 5.dp)
    ) {
        if(timer == null)
            Text(stringResource(R.string.not_selected))
        else {
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
    }
}