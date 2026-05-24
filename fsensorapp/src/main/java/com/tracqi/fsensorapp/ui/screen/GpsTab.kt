package com.tracqi.fsensorapp.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.tracqi.fsensorapp.model.GpsUiState
import com.tracqi.fsensorapp.ui.component.TrackPlot
import com.tracqi.fsensorapp.ui.theme.NeonAmber
import com.tracqi.fsensorapp.ui.theme.NeonCyan
import com.tracqi.fsensorapp.ui.theme.NeonGreen
import com.tracqi.fsensorapp.ui.theme.NeonMagenta
import com.tracqi.fsensorapp.ui.theme.SecondaryText

@Composable
fun GpsTab(
    gpsUiState: GpsUiState,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onReset: () -> Unit
) {
    if (!hasLocationPermission) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Location permission is required to use the GPS Kalman filter.",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRequestPermission,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                border = ButtonDefaults.outlinedButtonBorder(true)
                    .copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan))
            ) {
                Text("Grant Location Permission")
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrackPlot(
            gpsTrack = gpsUiState.gpsTrack,
            filteredTrack = gpsUiState.filteredTrack,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GpsLabeledValue("Speed", String.format("%.1f m/s", gpsUiState.speedMps), NeonCyan)
            GpsLabeledValue("East", String.format("%.1f m", gpsUiState.eastM), NeonMagenta)
            GpsLabeledValue("North", String.format("%.1f m", gpsUiState.northM), NeonGreen)
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GpsLabeledValue(
                "GPS",
                if (gpsUiState.hasGpsFix) "FIX" else "---",
                if (gpsUiState.hasGpsFix) NeonAmber else SecondaryText
            )
            GpsLabeledValue("Accuracy", String.format("%.1f m", gpsUiState.accuracyM), SecondaryText)
            GpsLabeledValue("Fixes", "${gpsUiState.fixCount}", SecondaryText)
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(NeonMagenta)
            Text("Raw GPS", style = MaterialTheme.typography.labelSmall, color = SecondaryText,
                modifier = Modifier.padding(start = 4.dp, end = 16.dp))
            LegendDot(NeonCyan)
            Text("Filtered", style = MaterialTheme.typography.labelSmall, color = SecondaryText,
                modifier = Modifier.padding(start = 4.dp))
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
            border = ButtonDefaults.outlinedButtonBorder(true)
                .copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan))
        ) {
            Text("Reset")
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Canvas(modifier = Modifier.size(10.dp)) {
        drawCircle(color, radius = size.minDimension / 2f, center = Offset(size.width / 2f, size.height / 2f))
    }
}

@Composable
private fun GpsLabeledValue(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
    }
}
