package com.iot.control.ui.scripts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.ui.utils.TopBarDialog
import com.iot.control.viewmodel.ScriptUiState
import com.iot.control.viewmodel.ScriptViewModel

@Composable
fun EventTriggeredEventGroupDialog(
    open: MutableState<Boolean>,
    scriptViewModel: ScriptViewModel,
    screenState: ScriptUiState,
    openScriptDialog: () -> Unit
) {
    TopBarDialog(stringResource(R.string.new_script_label), close = { open.value = false }, save = { open.value = false }) {
        Surface(modifier = Modifier.padding(horizontal = 30.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                EventTriggeredEventGroup(
                    scriptViewModel,
                    screenState,
                    openScriptDialog
                )
            }
        }
    }
}

@Composable
fun EventTriggeredEventGroup(
    scriptViewModel: ScriptViewModel,
    screenState: ScriptUiState,
    openScriptDialog: () -> Unit
) {
    val dto = screenState.dto

    Row {
//        Text(
//            stringResource(R.string.event_label),
//            color = MaterialTheme.colorScheme.secondary,
//            fontSize = TextUnit(26f, TextUnitType.Sp)
//        )
        Text(
            "${dto?.name} / ${dto?.type?.name}",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = TextUnit(26f, TextUnitType.Sp)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    
    TimerScriptList(
        scriptViewModel,
        scriptViewModel.getScriptsForEvent(),
        editScript = {
            if(dto != null) {
                scriptViewModel.editEventTriggeredScript(dto.deviceId, it)
                openScriptDialog()
            }
        },
        newScript = {
           scriptViewModel.newScript()
           openScriptDialog()
        },
        delete = {
            scriptViewModel.deleteScript(it)
        }
    )

}
