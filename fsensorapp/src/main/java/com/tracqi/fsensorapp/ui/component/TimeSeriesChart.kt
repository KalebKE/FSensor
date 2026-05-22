package com.tracqi.fsensorapp.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracqi.fsensorapp.model.ChartData
import com.tracqi.fsensorapp.ui.theme.ChartBlue
import com.tracqi.fsensorapp.ui.theme.ChartGreen
import com.tracqi.fsensorapp.ui.theme.ChartRed

@Composable
fun TimeSeriesChart(
    chartData: ChartData,
    yRange: Float,
    yLabel: String,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val count = chartData.count
        if (count < 2) return@Canvas

        val padding = 40.dp.toPx()
        val chartLeft = padding
        val chartRight = size.width - 12.dp.toPx()
        val chartTop = 12.dp.toPx()
        val chartBottom = size.height - 24.dp.toPx()
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        val gridLines = listOf(-yRange, -yRange / 2, 0f, yRange / 2, yRange)
        for (v in gridLines) {
            val yPos = chartTop + chartHeight * (1f - (v + yRange) / (2f * yRange))
            drawLine(gridColor, Offset(chartLeft, yPos), Offset(chartRight, yPos), strokeWidth = 1f)
            val label = if (v == 0f) "0" else String.format("%.0f", v)
            drawText(textMeasurer, label, Offset(2.dp.toPx(), yPos - 6.dp.toPx()), style = labelStyle)
        }

        val latestTime = chartData.getSample(count - 1).time
        val windowSeconds = 5f

        fun mapX(t: Float): Float {
            val age = latestTime - t
            return chartRight - (age / windowSeconds) * chartWidth
        }

        fun mapY(v: Float): Float {
            val clamped = v.coerceIn(-yRange, yRange)
            return chartTop + chartHeight * (1f - (clamped + yRange) / (2f * yRange))
        }

        drawLine(chartData, count, ChartRed, ::mapX, ::mapY) { it.x }
        drawLine(chartData, count, ChartGreen, ::mapX, ::mapY) { it.y }
        drawLine(chartData, count, ChartBlue, ::mapX, ::mapY) { it.z }
    }
}

private fun DrawScope.drawLine(
    data: ChartData,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    mapX: (Float) -> Float,
    mapY: (Float) -> Float,
    getValue: (ChartData.Sample) -> Float
) {
    val path = Path()
    var started = false
    for (i in 0 until count) {
        val sample = data.getSample(i)
        val x = mapX(sample.time)
        val y = mapY(getValue(sample))
        if (!started) {
            path.moveTo(x, y)
            started = true
        } else {
            path.lineTo(x, y)
        }
    }
    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}
