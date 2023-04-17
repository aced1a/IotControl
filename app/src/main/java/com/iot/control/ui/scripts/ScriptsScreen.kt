package com.iot.control.ui.scripts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iot.control.model.EventDto
import com.iot.control.model.Script
import com.iot.control.viewmodel.ScriptViewModel

@Composable
fun ScriptScreen(
    scriptViewModel: ScriptViewModel
) {

}

@Composable
fun TriggerList(
    triggers: Map<EventDto, List<Script>>,
    select: () -> Unit,
    create: () -> Unit
) {
    LazyColumn {
        items(triggers.entries.toList()) {entry ->
            TriggerItem(
                trigger = entry.key,
                scripts = entry.value,
                false,
                select,
                create
            )
        }
    }
}

@Composable
fun TriggerItem(
    trigger: EventDto,
    scripts: List<Script>,
    scriptListOpened: Boolean,
    select: () -> Unit,
    create: () -> Unit
) {
    Column(
        Modifier
            .padding(all = 15.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Text("${trigger.name}/${trigger.type.name}", style = MaterialTheme.typography.headlineSmall)
        //Text(connection.address, style= MaterialTheme.typography.bodyMedium, color= MaterialTheme.colorScheme.secondary)

        if(scriptListOpened) {
            ScriptList(scripts, select, create)
        }
    }
}

@Composable
fun ScriptList(
    scripts: List<Script>,
    select: () -> Unit,
    create: () -> Unit
) {
    LazyColumn {
        item {
            Text(
                "Add new action",
                modifier = Modifier.clickable { create() }
            )
        }
        items(scripts) { script -> 
            ScriptItem(script = script)
        }
    }
}

@Composable
fun ScriptItem(script: Script) {
    Text("Action: ${script.commandAction.name}")
    Text("Guard: ${script.guard.name} ${script.guardValue}")
}
