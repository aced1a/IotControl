package com.iot.control.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.iot.control.ui.connections.ConnectionsScreen
import com.iot.control.ui.dashboard.DashboardScreen
import com.iot.control.ui.devices.DeviceDialog
import com.iot.control.ui.devices.DevicesScreen
import com.iot.control.ui.scripts.ScriptScreen
import com.iot.control.viewmodel.*
import androidx.activity.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import com.iot.control.ui.settings.SettingsScreen
import java.util.UUID

@Composable
fun IotControlNavGraph(
    navController: NavHostController,
    navigation: IotControlNavigation,
    modifier: Modifier = Modifier
) {
    NavHost(navController=navController, startDestination=IotControlNavigation.CONNECTIONS,  modifier=modifier)
    {
        composable(IotControlNavigation.DASHBOARD) {
            val dashboardViewModel = hiltViewModel<DashboardViewModel>()
            DashboardScreen(dashboardViewModel)
        }

        composable(IotControlNavigation.CONNECTIONS) {
            val connectionsViewModel = hiltViewModel<ConnectionsViewModel>()
            ConnectionsScreen(connectionsViewModel, navigation.navigateToDevices)
        }

        composable(IotControlNavigation.SCRIPTS) {
            val scriptViewModel = hiltViewModel<ScriptViewModel>()
            ScriptScreen(scriptViewModel)
        }
        composable(IotControlNavigation.SETTINGS) {
            val settingsViewModel = hiltViewModel<SettingsViewModel>()
            SettingsScreen(settingsViewModel)
        }

        composable(
            "${IotControlNavigation.DEVICES}/{connectionId}",
            arguments = listOf(
                navArgument("connectionId") { type = NavType.StringType }
            )
        ) {

            val devicesViewModel = hiltViewModel<DevicesViewModel>()
            DevicesScreen(devicesViewModel, navigation.navigateToDeviceDetail)
        }

        composable(
            "${IotControlNavigation.DEVICE_DETAILS}/{connectionId}?deviceId={deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("connectionId") { type = NavType.StringType }
            )
        ) {
            val deviceDetailViewModel = hiltViewModel<DeviceDetailViewModel>()
            DeviceDialog(deviceDetailViewModel, back = navigation.back)
        }
    }
}