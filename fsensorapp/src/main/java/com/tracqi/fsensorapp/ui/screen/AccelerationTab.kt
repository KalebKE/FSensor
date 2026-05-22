package com.tracqi.fsensorapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracqi.fsensorapp.model.ChartData
import com.tracqi.fsensorapp.ui.component.AccelerationGauge
import com.tracqi.fsensorapp.ui.component.TimeSeriesChart
import com.tracqi.fsensorapp.ui.theme.ChartBlue
import com.tracqi.fsensorapp.ui.theme.ChartGreen
import com.tracqi.fsensorapp.ui.theme.ChartRed

@Composable
fun AccelerationTab(acceleration: FloatArray, history: ChartData) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AccelerationGauge(
            x = acceleration[0],
            y = acceleration[1],
            modifier = Modifier.size(200.dp)
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LabeledValue("X", acceleration[0], ChartRed)
            LabeledValue("Y", acceleration[1], ChartGreen)
            LabeledValue("Z", acceleration[2], ChartBlue)
        }

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Linear Acceleration (m/s²)", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                TimeSeriesChart(
                    chartData = history,
                    yRange = 20f,
                    yLabel = "m/s²",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: Float, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(String.format("%.2f", value), style = MaterialTheme.typography.bodyMedium)
    }
}
