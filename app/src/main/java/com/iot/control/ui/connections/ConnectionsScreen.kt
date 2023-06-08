package com.iot.control.ui.connections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.Connection
import com.iot.control.model.enums.ConnectionType
import com.iot.control.viewmodel.ConnectionsViewModel
import java.util.*


@Composable
fun ConnectionsScreen(
    connectionsViewModel: ConnectionsViewModel,
    toDevices: (UUID) -> Unit,
) {
    val openDialog = remember { mutableStateOf(false ) }
    val dialogState by connectionsViewModel.dialogState.collectAsStateWithLifecycle()
    Scaffold(
        floatingActionButton = { NewConnectionFab(connectionsViewModel::newConnection, openDialog) },
        floatingActionButtonPosition = FabPosition.End
    ) {
        val state by connectionsViewModel.uiState.collectAsStateWithLifecycle()
        ConnectionsList(
            connections = state.connections,
            connectionsViewModel::active,
            connectionsViewModel::toggleConnection,
            toDevices,
            { connection ->
                connectionsViewModel.loadSelectedConnection(connection)
                openDialog.value = true
            },
            connectionsViewModel::deleteConnection,
            modifier = Modifier.padding(it)
        )

        if(openDialog.value)
            Dialog(
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = { openDialog.value = false}
            ) {
                ConnectionDialog(
                    dialogState,
                    connectionsViewModel::updateDialogModel,
                    {
                        connectionsViewModel.saveConnection()
                        openDialog.value = false
                    },
                    openDialog
                )
            }
    }
}

@Composable
fun NewConnectionFab(
    newConnection: () -> Unit,
    openDialog: MutableState<Boolean>
)
{
    FloatingActionButton(
        onClick = {
            newConnection()
            openDialog.value = true
        },
        modifier = Modifier.offset(x = (-10).dp, y = (-10).dp)
    ) {
        Icon(Icons.Filled.Add, null)
    }
}

@Composable
fun ConnectionsList(
    connections: List<Connection>,
    getConnectionStatus: (Connection) -> Boolean,
    toggleConnection: (Connection) -> Boolean,
    toDeviceList: (UUID) -> Unit,
    openEditDialog: (Connection) -> Unit,
    delete: (Connection) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(state=rememberLazyListState(), modifier = modifier) {
        items(connections) { connection ->
            ConnectionItem(connection, getConnectionStatus, toggleConnection, toDeviceList, openEditDialog, delete)
        }
        item {
           Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun ConnectionItem(
    connection: Connection,
    getConnectionStatus: (Connection) -> Boolean,
    toggleConnection: (Connection) -> Boolean,
    toDeviceList: (UUID) -> Unit,
    openEditDialog: (Connection) -> Unit,
    delete: (Connection) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp) .clickable { menuOpen = menuOpen.not() },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            Modifier.padding(bottom = 15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Text(connection.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                connection.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Row(
//                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                SimpleChip(connection.type.labelId)
                SimpleChip(
                    if (connection.isCredentials) R.string.credentials_label else R.string.anon_label
                )
                if (connection.type == ConnectionType.LOCAL_MQTT) SimpleChip(R.string.local_label)
                if (connection.isSsl) SimpleChip(R.string.ssl_label)
            }
        }
        ActivityChip(
            getStatus = { getConnectionStatus(connection) },
            toggle = { toggleConnection(connection) }
        )
    }
    if(menuOpen) {
        Column(modifier = Modifier.padding(start = 15.dp)) {
            ContextMenuItem(R.string.device_list_text) { toDeviceList(connection.id) }
            ContextMenuItem(R.string.edit_label) { openEditDialog(connection) }
            ContextMenuItem(R.string.delete_label) { delete(connection) }
        }
    }

}

@Composable
fun ContextMenuItem(resourceId: Int, callback: () -> Unit)
{
    Text(
        stringResource(resourceId),
        color=MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .clickable(onClick = callback)
            .fillMaxWidth())
}

@Composable
fun ActivityChip(getStatus: () -> Boolean, toggle: () -> Boolean) {
    var status by remember { mutableStateOf(getStatus()) }

    IconButton(onClick = { status = toggle() }) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            tint = if(status) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            contentDescription = ""
        )
    }

}

@Composable
fun SimpleChip(resourceId: Int)
{
    Surface(shape=MaterialTheme.shapes.small, shadowElevation=2.dp) {

        Text(stringResource(resourceId),
            modifier = Modifier.padding(all=5.dp) ,
            style = MaterialTheme.typography.titleSmall)
    }
}
