package com.iot.control.ui.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.iot.control.R
import com.iot.control.model.Device
import com.iot.control.model.enums.Icons
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
            DashedDivider(modifier = Modifier.padding(horizontal = 5.dp))
        }
        Spacer(modifier = Modifier.height(1.dp))
        content()
    }
}
@Composable
fun DashedDivider(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(1.dp)) {
        drawLine(
            color = Color.Gray,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedGeneralField(
    title: String,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expanded = enable && expanded.not() }
                .background(
                    color = if (enable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondary.copy(
                        alpha = 0.1F
                    ),
                    shape = MaterialTheme.shapes.small
                ),
        ) {
            Text(title, color = MaterialTheme.colorScheme.secondary)
            Divider(
                color = MaterialTheme.colorScheme.secondary,
                thickness = 1.dp,
                modifier = Modifier
                    .padding(start = 5.dp, end = 1.dp)
                    .width(240.dp)
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
        if(enable && expanded) {
            Spacer(modifier = Modifier.height(1.dp))
            content()
        }
    }
}


@Composable
fun ToggleField(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    update: (Boolean) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(
            checked = checked,
            onCheckedChange = update
        )
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
            SimpleDeviceItem(device = device, select = select, modifier = Modifier.fillMaxWidth())
        else {
            Text(stringResource(
                R.string.not_selected),
                modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp).fillMaxWidth().clickable { select() }
            )
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
    enabled: Boolean = true,
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
            enabled = enabled,
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


@Composable
fun LabeledValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    space: Boolean = true,
    icon: @Composable () -> Unit
) {
    Row(
        horizontalArrangement = if(space) Arrangement.SpaceBetween else Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.defaultMinSize(minWidth = 100.dp)
        )
        Text(value)

        icon()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectItemDialog(
    visibility: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    ModalBottomSheet(onDismissRequest = { visibility.value = false }) {
        Card(
            modifier = Modifier.padding(all = 15.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
    }
}

@Composable
fun SelectIconDialog(
    visibility: MutableState<Boolean>,
    onSelect: (Icons) -> Unit
) {
    Dialog(onDismissRequest = { visibility.value = false }) {
        Surface(
            shape = MaterialTheme.shapes.large,
        ) {
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 40.dp)) {
                items(Icons.values()) { icon ->
                    IconButton(
                        onClick = { onSelect(icon) },
                        modifier = Modifier.padding(all=2.dp)
                    ) {
                        Icon(painter = painterResource(icon.id), contentDescription = "Icon")
                    }
                }
            }
        }
    }
}