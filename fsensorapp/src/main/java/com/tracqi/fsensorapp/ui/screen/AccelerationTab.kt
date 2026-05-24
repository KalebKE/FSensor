package com.tracqi.fsensorapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.tracqi.fsensorapp.model.ChartData
import com.tracqi.fsensorapp.ui.component.AccelerationGauge
import com.tracqi.fsensorapp.ui.component.TimeSeriesChart
import com.tracqi.fsensorapp.ui.theme.NeonCyan
import com.tracqi.fsensorapp.ui.theme.NeonGreen
import com.tracqi.fsensorapp.ui.theme.NeonMagenta

@Composable
fun AccelerationTab(acceleration: FloatArray, history: ChartData, fusionName: String, onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            fusionName,
            style = MaterialTheme.typography.titleSmall,
            color = NeonCyan
        )

        Spacer(Modifier.height(8.dp))

        AccelerationGauge(
            x = acceleration[0],
            y = acceleration[1],
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LabeledValue("X", acceleration[0], NeonCyan)
            LabeledValue("Y", acceleration[1], NeonMagenta)
            LabeledValue("Z", acceleration[2], NeonGreen)
        }

        Spacer(Modifier.height(16.dp))

        TimeSeriesChart(
            chartData = history,
            yRange = 20f,
            yLabel = "m/s²",
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
            border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan))
        ) {
            Text("Reset")
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: Float, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(
            String.format("%.2f", value),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}
