package com.tracqi.fsensorapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tracqi.fsensorapp.ui.screen.MainScreen
import com.tracqi.fsensorapp.ui.screen.SettingsScreen
import com.tracqi.fsensorapp.ui.theme.FSensorTheme
import com.tracqi.fsensorapp.viewmodel.SensorViewModel

@Composable
fun FSensorApp(viewModel: SensorViewModel = viewModel()) {
    FSensorTheme {
        val navController = rememberNavController()
        val config by viewModel.config.collectAsState()
        val orientation by viewModel.orientation.collectAsState()
        val acceleration by viewModel.acceleration.collectAsState()
        val orientationHistory by viewModel.orientationHistory.collectAsState()
        val accelerationHistory by viewModel.accelerationHistory.collectAsState()

        DisposableEffect(Unit) {
            viewModel.start()
            onDispose { viewModel.stop() }
        }

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainScreen(
                    orientation = orientation,
                    acceleration = acceleration,
                    orientationHistory = orientationHistory,
                    accelerationHistory = accelerationHistory,
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    config = config,
                    onConfigChange = { viewModel.updateConfig(it) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
