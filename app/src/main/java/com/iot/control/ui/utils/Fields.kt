package com.iot.control.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.iot.control.model.Device
import com.iot.control.ui.devices.SimpleDeviceItem

@Composable
fun GeneralField(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = MaterialTheme.colorScheme.secondary)
            Divider(color = MaterialTheme.colorScheme.secondary, thickness = 1.dp, modifier = Modifier.padding(horizontal = 5.dp))
        }
        Spacer(modifier = Modifier.height(1.dp))
        content()
    }
}

@Composable
fun DeviceSelectField(
    title: String = "Device",
    device: Device?,
    select: () -> Unit
) {
    GeneralField(title = title) {
        if(device != null)
            SimpleDeviceItem(device = device, select = select)
        else {
            SimpleDeviceItem(device = Device.getDefaultDevice(), select = select)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuField(
    label: String,
    value: String,
    expanded: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            expanded.value = expanded.value.not()
        }
    ) {
        OutlinedTextField(
            value,
            modifier = modifier
                .menuAnchor(),
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
            },
            onValueChange = {}
        )
        ExposedDropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            content()
        }
    }
}