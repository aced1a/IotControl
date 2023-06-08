package com.iot.control.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DurablePicker(
    visible: MutableState<Boolean>,
    hours: Int?,
    minutes: Int,
    seconds: Int,
    onChange: (Int, Int, Int) -> Unit
) {
    val hour = remember { mutableStateOf(hours ?: 0) }
    val minute = remember { mutableStateOf(minutes) }
    val second = remember { mutableStateOf(seconds) }

    if(visible.value)
        Dialog(
            onDismissRequest = {
                visible.value = false
                onChange(hour.value, minute.value, second.value)
            }
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.padding(all = 5.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = {
                        visible.value = false
                        onChange(hour.value, minute.value, second.value)
                    }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close durable picker")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if(hours != null)
                        LabeledPicker("Hours", hour, 0 .. 24)

                    LabeledPicker("Minutes", minute,0 .. 59)
                    LabeledPicker("Seconds", second, 0 .. 59)
                }
            }
        }
}

@Composable
fun LabeledPicker(
    label: String,
    value: MutableState<Int>,
    range: IntRange,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 5.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.secondary)
        NumberPicker(state = value, range = range, onStateChanged = {})
    }
}