package com.iot.control.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Command
import com.iot.control.model.enums.CommandAction
import com.iot.control.ui.connections.SimpleChip
import com.iot.control.viewmodel.Marked

@Composable
fun CommandList(
    commands: List<Marked<Command>>,
    editCommand: (Command) -> Unit,
    addCommand: (CommandAction) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { visible = visible.not() }
    ) {
        Text(
            stringResource(R.string.commands_label),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall)
        Divider()

        if(visible) {
            LazyColumn {
                items(commands) { command ->
                    CommandListItem(command.item, editCommand)
                }
                item { Divider() }
                items(CommandAction.values()) { action ->
                    if(commands.find { it.item.action == action } == null)
                        NewItemList(
                            stringResource(R.string.add_new_command, action.name)
                        ) { addCommand(action) }
                }
            }
        }
    }
}

@Composable
fun CommandListItem(command: Command, editCommand: (Command) -> Unit)
{
    Column(
        modifier = Modifier
            .padding(all = 5.dp)
            .fillMaxWidth()
            .clickable { editCommand(command) } ,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            command.action.name,
            style= MaterialTheme.typography.bodyLarge)
        Text(
            command.topic,
            style= MaterialTheme.typography.bodySmall,
            color= MaterialTheme.colorScheme.secondary)
        SimpleChip(
            if(command.isJson) R.string.json_label else R.string.plain_label
        )
    }
}