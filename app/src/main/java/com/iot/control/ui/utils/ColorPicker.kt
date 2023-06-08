package com.iot.control.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.iot.control.R

@Composable
fun ColorPickerDialog(
    color: ULong,
    open: MutableState<Boolean>,
    onChange: (ULong) -> Unit
) {
    val controller = rememberColorPickerController()
    controller.setWheelColor(Color.White)

    Dialog(onDismissRequest = { open.value = false }) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)
                ) {
                    IconButton(
                        onClick = { open.value = false },
                        modifier = Modifier.padding(end = 7.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }

                    TextButton(onClick = {
                        onChange(controller.selectedColor.value.value)
                        open.value = false
                    }) {
                        Text(stringResource(R.string.save_label))
                    }
                }

                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(horizontal = 10.dp),
                    controller = controller
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_rectangle_24),
                        contentDescription = "Color picker",
                        tint = controller.selectedColor.value
                    )

                    Text(
                        "#${controller.selectedColor.value.value.toString(16).padEnd(8,'0').substring(2, 8)}",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}