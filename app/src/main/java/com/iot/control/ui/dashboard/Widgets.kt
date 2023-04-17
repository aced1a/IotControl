package com.iot.control.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iot.control.R
import com.iot.control.model.Device
import com.iot.control.model.enums.DeviceType
import com.iot.control.ui.theme.IotControlTheme


@Composable
fun Widget(device: Device) {
    when(device.type) {
        DeviceType.Light -> LightWidget(device)
        DeviceType.State -> StateWidget(device)
        DeviceType.Switch -> SwitchWidget(device)
        else -> {}
    }
}

@Composable
fun DefaultWidget(
    name: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.small,
        modifier = modifier.padding(all=10.dp).height(165.dp)
    ) {
        Text(
            name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Divider(
            modifier = Modifier
                .width(60.dp)
                .align(Alignment.CenterHorizontally)
        )

        content()
    }
}

@Composable
fun LightWidget(device: Device) {
    DefaultWidget(device.name) {
        Icon(
            painter = painterResource(R.drawable.baseline_lightbulb_60), null,
            tint=MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(all = 10.dp)
                .align(Alignment.CenterHorizontally)
        )

        Switch(
            checked = false,
            onCheckedChange = {},
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun StateWidget(device: Device)
{
    DefaultWidget(
        name = device.name
    ) {
        Text(
            device.value,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 10.dp)
        )
    }
}

@Preview
@Composable
fun PreviewStateWidget()
{
    IotControlTheme {
        StateWidget(Device(name = "Kitchen light", value="On"))
    }
}


@Composable
fun SwitchWidget(device: Device)
{
    DefaultWidget(name = device.name) {
        Text(
            device.value,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 10.dp)
        )

        Switch(
            checked = false,
            onCheckedChange = {},
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}