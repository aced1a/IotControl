package com.iot.control.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.navigation.compose.rememberNavController
import com.iot.control.R
import com.iot.control.ui.theme.IotControlTheme

@Composable
fun IotControlApp() {
    IotControlTheme {
        val navController = rememberNavController()
        val actions = remember(navController) {
            IotControlNavigation(navController)
        }
        
        Scaffold(
            bottomBar = { BottomNavigation(navController, actions) }
        ) {
            Box(modifier = Modifier.padding(it)) {
                IotControlNavGraph(navController, actions)
            }
        }
    }
}


@Composable
fun BottomNavigation(navController: NavHostController, actions: IotControlNavigation)
{
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentPath = navBackStackEntry?.destination?.route ?: IotControlNavigation.CONNECTIONS

        NavigationBarItem(
            label = { Text(stringResource(R.string.dashboard), style=MaterialTheme.typography.labelSmall) },
            icon = { Icon( painter = painterResource(R.drawable.baseline_dashboard_24), null )},
            selected = currentPath == IotControlNavigation.DASHBOARD,
            onClick = { actions.navigateToDashboard() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationBarItem(
            label = {
                Text(stringResource(R.string.connections), style=MaterialTheme.typography.labelSmall, softWrap = false)
            },
            icon = { Icon( painter = painterResource(R.drawable.baseline_devices_other_24), null )},
            selected = currentPath == IotControlNavigation.CONNECTIONS,
            onClick = { actions.navigateToConnections() },
            modifier = Modifier
        )
        NavigationBarItem(
            label = { Text(stringResource(R.string.scripts), style=MaterialTheme.typography.labelSmall) },
            icon = { Icon( painter = painterResource(R.drawable.baseline_call_split_24), null )},
            selected = currentPath == IotControlNavigation.SCRIPTS,
            onClick = { actions.navigateToScripts() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationBarItem(
            label = { Text(stringResource(R.string.settings), style=MaterialTheme.typography.labelSmall) },
            icon = { Icon(Icons.Filled.Settings, null)},
            selected = currentPath == IotControlNavigation.SETTINGS,
            onClick = { actions.navigateToSettings() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}


@Preview
@Composable
fun PreviewIotControlApp()
{
    IotControlTheme {
        IotControlApp()
    }
}