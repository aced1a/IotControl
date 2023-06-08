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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Event
import com.iot.control.ui.connections.SimpleChip
import com.iot.control.ui.utils.GeneralField
import com.iot.control.ui.utils.LabeledValue
import com.iot.control.viewmodel.Marked


@Composable
fun EventList(
    events: List<Marked<Event>>,
    editEvent: (Event) -> Unit,
    addEvent: () -> Unit,
    deleteEvent: (Marked<Event>) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { visible = visible.not() }
    ) {
        GeneralField(stringResource(R.string.events_label)) {
            if(visible) {
                LazyColumn {
                    item {
                        NewItemList(stringResource(R.string.add_new_event, "")) {
                            addEvent()
                        }
                        Divider()
                    }
                    items(events) { item ->
                        val event = item.item
                        LabeledValue(event.type.name, event.topic, modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editEvent(event) }
                        ) {
                            IconButton(onClick = { deleteEvent(item) }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete event")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NewItemList(
    title: String,
    center: Boolean = false,
    action: () -> Unit
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 2.dp)
            .clickable { action() },
        horizontalArrangement = if(center) Arrangement.Center else Arrangement.Start
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