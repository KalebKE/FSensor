package com.tracqi.fsensorapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracqi.fsensorapp.model.FilterType
import com.tracqi.fsensorapp.model.FusionParams
import com.tracqi.fsensorapp.model.FusionType
import com.tracqi.fsensorapp.model.SensorConfig
import com.tracqi.fsensorapp.model.SensorRate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    config: SensorConfig,
    onConfigChange: (SensorConfig) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("Fusion Algorithm")
            FusionType.entries.forEach { fusion ->
                RadioItem(
                    label = fusion.label,
                    selected = config.fusionType == fusion,
                    onClick = { onConfigChange(config.copy(fusionType = fusion)) }
                )
            }

            when (config.fusionType) {
                FusionType.MADGWICK -> {
                    ParamSlider("Beta", config.fusionParams.madgwickBeta, 0.001f, 0.5f) {
                        onConfigChange(config.copy(fusionParams = config.fusionParams.copy(madgwickBeta = it)))
                    }
                }
                FusionType.MAHONY -> {
                    ParamSlider("Kp", config.fusionParams.mahonyKp, 0.1f, 10f) {
                        onConfigChange(config.copy(fusionParams = config.fusionParams.copy(mahonyKp = it)))
                    }
                    ParamSlider("Ki", config.fusionParams.mahonyKi, 0f, 1f) {
                        onConfigChange(config.copy(fusionParams = config.fusionParams.copy(mahonyKi = it)))
                    }
                }
                FusionType.EKF -> {
                    ParamSlider("Process Noise", config.fusionParams.ekfProcessNoise, 0.0001f, 0.1f) {
                        onConfigChange(config.copy(fusionParams = config.fusionParams.copy(ekfProcessNoise = it)))
                    }
                    ParamSlider("Accel Noise", config.fusionParams.ekfAccelNoise, 0.01f, 1f) {
                        onConfigChange(config.copy(fusionParams = config.fusionParams.copy(ekfAccelNoise = it)))
                    }
                }
                FusionType.COMPLEMENTARY -> {
                    ParamSlider("Time Constant", config.fusionParams.complementaryTc, 0.01f, 1f) {
                        onConfigChange(config.copy(fusionParams = config.fusionParams.copy(complementaryTc = it)))
                    }
                }
                FusionType.KALMAN -> {
                    ParamSlider("Process Noise", config.fusionParams.kalmanProcessNoise, 0.0001f, 0.1f) {
                        onConfigChange(config.copy(fusionParams = config.fusionParams.copy(kalmanProcessNoise = it)))
                    }
                    ParamSlider("Measurement Noise", config.fusionParams.kalmanMeasurementNoise, 0.01f, 1f) {
                        onConfigChange(config.copy(fusionParams = config.fusionParams.copy(kalmanMeasurementNoise = it)))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("Smoothing Filter")
            FilterType.entries.forEach { filter ->
                RadioItem(
                    label = filter.label,
                    selected = config.filterType == filter,
                    onClick = { onConfigChange(config.copy(filterType = filter)) }
                )
            }
            if (config.filterType != FilterType.NONE) {
                ParamSlider("Time Constant", config.filterTimeConstant, 0.01f, 1f) {
                    onConfigChange(config.copy(filterTimeConstant = it))
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("Sensor Rate")
            SensorRate.entries.forEach { rate ->
                RadioItem(
                    label = rate.label,
                    selected = config.sensorRate == rate,
                    onClick = { onConfigChange(config.copy(sensorRate = rate)) }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun RadioItem(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.ListItem(
        headlineContent = { Text(label) },
        leadingContent = {
            androidx.compose.material3.RadioButton(selected = selected, onClick = onClick)
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp)
    )
}

@Composable
private fun ParamSlider(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)) {
        Text(
            "$label: ${String.format("%.4f", value)}",
            style = MaterialTheme.typography.bodySmall
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = min..max
        )
    }
}
