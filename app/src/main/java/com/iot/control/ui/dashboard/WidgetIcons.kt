package com.iot.control.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.iot.control.R
import com.iot.control.model.enums.DeviceType

@Composable
fun WidgetIcon(type: DeviceType)
{
    when(type) {
        DeviceType.Light -> LightWidgetIcon()
        DeviceType.State -> StateWidgetIcon()
        DeviceType.Button -> ButtonWidgetIcon()
        DeviceType.Switch -> SwitchWidgetIcon()
    }
}

@Composable
fun LightWidgetIcon()
{
    Icon(painter = painterResource(R.drawable.baseline_lightbulb_48), null)
}

@Composable
fun LightDimmingWidgetIcon()
{
    Box{
        Icon(painter = painterResource(R.drawable.baseline_lightbulb_48), null)
        Icon(
            painter = painterResource(R.drawable.baseline_switch_left_12),
            null,
            modifier = Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
fun ButtonWidgetIcon() {
    Icon(painter = painterResource(R.drawable.baseline_radio_button_checked_24), null)
}

@Composable
fun StateWidgetIcon() {
    Icon(painter = painterResource(R.drawable.baseline_pin_48), null)
}

@Composable
fun SwitchWidgetIcon() {
    Icon(painter = painterResource(R.drawable.baseline_toggle_on_48), null)
}