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
import com.iot.control.viewmodel.ConnectionsViewModel
import com.iot.control.viewmodel.DashboardViewModel
import com.iot.control.viewmodel.DeviceDetailViewModel
import com.iot.control.viewmodel.DevicesViewModel
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
            val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.provideFactory())
            DashboardScreen(dashboardViewModel)
        }
        composable(IotControlNavigation.CONNECTIONS) {
            val connectionsViewModel: ConnectionsViewModel = viewModel(factory=ConnectionsViewModel.provideFactory())
            ConnectionsScreen(connectionsViewModel, navigation.navigateToDevices)
        }
        composable(IotControlNavigation.SCRIPTS) {}
        composable(IotControlNavigation.SETTINGS) {}

        composable(
            "${IotControlNavigation.DEVICES}/{connectionId}",
            arguments = listOf(
                navArgument("connectionId") { type = NavType.StringType }
            )
        ) { backStack ->
            val strId = backStack.arguments?.getString("connectionId")
            val connectionId = UUID.fromString(strId)

            val devicesViewModel: DevicesViewModel = viewModel(factory=DevicesViewModel.provideFactory(connectionId))
            DevicesScreen(devicesViewModel, navigation.navigateToDeviceDetail)
        }

        composable(
            "${IotControlNavigation.DEVICE_DETAILS}/{deviceId}/{connectionId}",
            arguments = listOf(
                navArgument("deviceId") { type = NavType.StringType },
                navArgument("connectionId") { type = NavType.StringType }
            )
        ) { backStack ->
            val strId = backStack.arguments?.getString("deviceId")
            val deviceId = if(strId == null || strId == "null") null else UUID.fromString(strId)
            val connectionId = UUID.fromString(backStack.arguments?.getString("connectionId"))

            val deviceDetailViewModel: DeviceDetailViewModel = viewModel(factory=DeviceDetailViewModel.provideFactory(deviceId, connectionId))
            DeviceDialog(deviceDetailViewModel, back = navigation.back)
        }
    }
}