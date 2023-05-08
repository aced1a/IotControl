package com.iot.control.ui

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import java.util.UUID

class IotControlNavigation(navController: NavHostController) {
    companion object {
        const val DASHBOARD = "dashboard"
        const val SCRIPTS = "scripts"
        const val CONNECTIONS = "connections"
        const val SETTINGS = "settings"
        const val DEVICES = "devices"
        const val DEVICE_DETAILS = "device_details"
    }

    val navigateToDashboard: () -> Unit = {
        navController.navigate(DASHBOARD) {
            popUpTo(navController.graph.findStartDestination().id)
        }
    }
    val navigateToConnections: () -> Unit = {
        navController.navigate(CONNECTIONS) {
            popUpTo(navController.graph.findStartDestination().id)
        }
    }
    val navigateToScripts: () -> Unit = {
        navController.navigate(SCRIPTS) {
            popUpTo(navController.graph.findStartDestination().id)
        }
    }
    val navigateToSettings: () -> Unit = {
        navController.navigate(SETTINGS) {
            popUpTo(navController.graph.findStartDestination().id)
        }
    }
    val navigateToDevices: (UUID) -> Unit = { id ->
        navController.navigate("${DEVICES}/$id") {
            popUpTo(navController.graph.findStartDestination().id)
        }
    }

    val navigateToDeviceDetail: (UUID?, UUID) -> Unit = { deviceId, connectionId ->

        val deviceIdArg = if(deviceId != null) "?deviceId=$deviceId" else ""
        navController.navigate("$DEVICE_DETAILS/$connectionId$deviceIdArg")
    }

    val back: () -> Unit = {
        navController.popBackStack()
    }
}