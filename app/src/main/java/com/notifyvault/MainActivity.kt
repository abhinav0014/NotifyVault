package com.notifyvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notifyvault.service.ReminderReceiver
import com.notifyvault.ui.MainViewModel
import com.notifyvault.ui.screens.HomeScreen
import com.notifyvault.ui.screens.PermissionScreen
import com.notifyvault.ui.screens.SettingsScreen
import com.notifyvault.ui.screens.SplashScreen
import com.notifyvault.ui.screens.StatsScreen
import com.notifyvault.ui.theme.NotifyVaultTheme
import com.notifyvault.utils.NotificationPermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ native splash — keeps it visible until we're ready
        val splashScreen = installSplashScreen()
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)

        ReminderReceiver.createChannel(this)

        setContent {
            val darkTheme by viewModel.darkTheme.collectAsState()
            NotifyVaultTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val hasPermission = remember {
                    NotificationPermissionHelper.isNotificationListenerEnabled(this)
                }

                // Determine start destination
                val startDest = if (hasPermission) "splash" else "permission"

                NavHost(navController = navController, startDestination = startDest) {
                    composable("splash") {
                        SplashScreen(
                            onFinished = {
                                keepSplash = false
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("permission") {
                        keepSplash = false
                        PermissionScreen(
                            onPermissionGranted = {
                                navController.navigate("splash") {
                                    popUpTo("permission") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToStats = { navController.navigate("stats") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }
                    composable("stats") {
                        StatsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
