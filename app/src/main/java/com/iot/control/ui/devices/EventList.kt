package com.iot.control.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import com.iot.control.model.Event
import com.iot.control.model.enums.EventType
import com.iot.control.ui.connections.SimpleChip
import com.iot.control.viewmodel.Marked


@Composable
fun EventList(
    events: List<Marked<Event>>,
    editEvent: (Event) -> Unit,
    addEvent: (EventType) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { visible = visible.not() }
    ) {
        Text(
            stringResource(R.string.events_label),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall)
        Divider()

        if(visible) {
            LazyColumn {
                items(events) { event ->
                    EventListItem(event.item, editEvent)
                }
                item { Divider() }
                items(EventType.values()) { type ->
                    if(events.find { it.item.type == type } == null)
                        NewItemList(
                            stringResource(R.string.add_new_event, type.name)
                        ) { addEvent(type) }
                }
            }
        }
    }
}

@Composable
fun EventListItem(event: Event, editEvent: (Event) -> Unit)
{
    Column(
        modifier = Modifier
            .padding(all = 5.dp)
            .fillMaxWidth()
            .clickable { editEvent(event) } ,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            event.type.name,
            style= MaterialTheme.typography.bodyLarge)
        Text(
            event.topic,
            style= MaterialTheme.typography.bodySmall,
            color= MaterialTheme.colorScheme.secondary)
        SimpleChip(
            if(event.isJson) R.string.json_label else R.string.plain_label
        )
    }
}

@Composable
fun NewItemList(title: String, action: () -> Unit)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 3.dp)
            .clickable { action() },
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add new",
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = title,
            style=MaterialTheme.typography.bodyMedium,
            color= MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top=2.dp, bottom = 2.dp)
        )
    }
}