package com.tracqi.fsensorapp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
        val gpsUiState by viewModel.gpsUiState.collectAsState()
        val hasLocationPermission by viewModel.locationPermissionGranted.collectAsState()

        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            viewModel.onLocationPermissionResult(granted)
        }

        DisposableEffect(Unit) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (alreadyGranted) viewModel.onLocationPermissionResult(true)
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
                    fusionName = config.fusionType.label,
                    gpsUiState = gpsUiState,
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onSettingsClick = { navController.navigate("settings") },
                    onReset = { viewModel.reset() }
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
