package com.iot.control.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.iot.control.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iot.control.ui.connections.ConnectionDialogBody


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTextDialog(
    message: String,
    accept: () -> Unit,
    visibility: MutableState<Boolean> = remember { mutableStateOf(true) }
) {
    AlertDialog(onDismissRequest = { visibility.value = false }) {
        Surface(
            modifier = Modifier.padding(all=20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    message,
                    modifier = Modifier.padding(all=10.dp)
                )

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { visibility.value = false }) {
                        Text(stringResource(R.string.cancel_label))
                    }
                    TextButton(
                        onClick = {
                            accept()
                            visibility.value = false }
                    ) {
                        Text(stringResource(R.string.accept_label))
                    }
                }
            }
        }
    }

}


@Composable
fun TopBarDialog(
    title: String,
    close: () -> Unit,
    save: () -> Unit,
    content: @Composable () -> Unit
){
    Scaffold(
        topBar = {
            ConnectionDialogTopBar(title = title, close = close, save = save)
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDialogTopBar(
    title: String,
    close: () -> Unit,
    save: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = close) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close dialog")
            }
        },
        actions = {
            IconButton(onClick = save) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = "Save data")
            }
        }
    )
}