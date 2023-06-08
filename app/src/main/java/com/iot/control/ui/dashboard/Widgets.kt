package com.iot.control.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.iot.control.R
import com.iot.control.model.Device
import com.iot.control.model.Widget
import com.iot.control.model.enums.CommandAction
import com.iot.control.model.enums.Icons
import com.iot.control.model.enums.WidgetType
import com.iot.control.ui.utils.ColorPickerDialog
import com.iot.control.ui.utils.DashedDivider

@Composable
fun DeviceWidget(
    widget: Widget,
    device: Device,
    format: (String?, String) -> String,
    parseColor: (String) -> ULong,
    send: (Device, CommandAction, String?) -> Unit
) {
    when(widget.type) {
        WidgetType.State -> StateWidget(widget, device)
        WidgetType.Switch -> SwitchWidget(widget, device, send)
        WidgetType.Button -> ButtonWidget(widget, device, send)
        WidgetType.Value -> ValueWidget(widget, device, format, send)
        WidgetType.Color -> ColorWidget(widget, device, parseColor, send)
    }
}

@Composable
fun DefaultWidget(
    name: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    icon: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .padding(all = 10.dp)
            .height(165.dp)
            .width(165.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,)

            icon()
        }

        DashedDivider(
            modifier = Modifier
                .width(120.dp)
                .padding(start = 10.dp)
        )

        content()
    }
}

@Composable
fun ColorWidget(widget: Widget, device: Device, parseColor: (String) -> ULong, send: (Device, CommandAction, String?) -> Unit) {
    val color = parseColor(device.value)
    val open = remember { mutableStateOf(false) }

    DefaultWidget(widget.name, expanded = widget.expanded, icon = { WidgetIcon(widget, 30) }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            IconButton(onClick = { open.value = true }) {
                Icon(
                    painterResource(R.drawable.baseline_rectangle_24),
                    contentDescription = "Color",
                    modifier = Modifier
                        .height(80.dp)
                        .width(80.dp),
                    tint = Color(color)
                )
            }

            Text("#${color.toString(16).substring(2,8)}")
        }
    }
    if(open.value) ColorPickerDialog(color, open) {
        send(device, CommandAction.Set, it.toString(16).substring(2, 8))
    }
}

@Composable
fun ValueWidget(widget: Widget, device: Device, format: (String?, String) -> String, send: (Device, CommandAction, String?) -> Unit) {
    val value = format(widget.formatter, device.value)
    val open = remember { mutableStateOf(false) }

    DefaultWidget(widget.name, expanded = widget.expanded, icon = { WidgetIcon(widget, 30)}) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                value,
                fontSize = TextUnit(40f, TextUnitType.Sp),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clickable { open.value = true }
            )
            if(widget.subtext != null)
                Text(
                    widget.subtext,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 20.dp)
                )
        }
    }
    if(open.value) ValueDialog(open, widget.name, device, send)
}

@Composable
fun StateWidget(widget: Widget, device: Device) {
    DefaultWidget(widget.name, expanded = widget.expanded, icon = { WidgetIcon(widget, 30)}) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(widget.useIcon) {
                WidgetIcon(widget, 80)
            }
            else {
                Text(
                    device.value,
                    fontSize = TextUnit(40f, TextUnitType.Sp),
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Composable
fun ButtonWidget(widget: Widget, device: Device, send: (Device, CommandAction, String?) -> Unit) {
    DefaultWidget(widget.name, expanded = widget.expanded, icon = { WidgetIcon(widget, 30) }) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            IconButton(
                onClick = { send(device, CommandAction.Send, null) },
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(Icons.Button.id),
                    contentDescription = "Button",
                    modifier = Modifier.width(80.dp).height(80.dp)
                )
            }
        }

    }
}

@Composable
fun SwitchWidget(widget: Widget, device: Device, send: (Device, CommandAction, String?) -> Unit) {
    val value = device.value == Device.ON

    DefaultWidget(widget.name, expanded = widget.expanded, icon = {  WidgetIcon(widget, 30) }) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if(widget.useIcon) {
                WidgetIcon(widget, 80)
            }
            else {
                Text(
                    device.value,
                    fontSize = TextUnit(40f, TextUnitType.Sp),
                    modifier = Modifier.padding(top = 20.dp)
                )
            }

            Switch(value, onCheckedChange = {
                if(value) send(device, CommandAction.Off, null)
                else send(device, CommandAction.On, null)
            })
        }
    }
}


@Composable
fun WidgetIcon(widget: Widget, size: Int, on: Boolean=false) {

    Icon(
        painter = painterResource(widget.icon.id), "Widget icon",
        tint=Color(if(on) widget.onColor.toULong() else widget.offColor.toULong()),
        modifier = Modifier
            .padding(top = 5.dp)
            .width(size.dp)
            .height(size.dp),
    )
}

@Composable
fun ValueDialog(
    open: MutableState<Boolean>,
    label: String,
    device: Device,
    send: (Device, CommandAction, String?) -> Unit
) {
    var state by remember { mutableStateOf(device.value) }

    Dialog(onDismissRequest = { open.value = false }) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.height(180.dp)
        ) {
            Column(
                modifier = Modifier.padding(all = 10.dp)
            ) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                ) {
                    IconButton(
                        onClick = { open.value = false },
                        modifier = Modifier.padding(end = 7.dp)
                    ) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Filled.Close, contentDescription = "Close")
                    }

                    Text(label)
                }

                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it},
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        send(device, CommandAction.Send, state)
                        open.value = false
                    }) {
                        Text("Send")
                    }

                    TextButton(onClick = {
                        send(device, CommandAction.Set, state)
                        open.value = false
                    }) {
                        Text("Set")
                    }
                }
            }
        }
    }
}
