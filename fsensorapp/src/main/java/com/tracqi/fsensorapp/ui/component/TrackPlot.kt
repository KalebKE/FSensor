package com.tracqi.fsensorapp.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracqi.fsensorapp.ui.theme.GridColor
import com.tracqi.fsensorapp.ui.theme.NeonCyan
import com.tracqi.fsensorapp.ui.theme.NeonMagenta
import com.tracqi.fsensorapp.ui.theme.SecondaryText
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun TrackPlot(
    gpsTrack: List<Pair<Double, Double>>,
    filteredTrack: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val padding = 48f
        val plotW = size.width - 2 * padding
        val plotH = size.height - 2 * padding

        if (plotW <= 0 || plotH <= 0) return@Canvas

        val allPoints = gpsTrack + filteredTrack
        if (allPoints.isEmpty()) {
            drawEmptyState(textMeasurer)
            return@Canvas
        }

        val minX = allPoints.minOf { it.first }
        val maxX = allPoints.maxOf { it.first }
        val minY = allPoints.minOf { it.second }
        val maxY = allPoints.maxOf { it.second }

        val rangeX = max(maxX - minX, 1.0)
        val rangeY = max(maxY - minY, 1.0)
        val margin = max(rangeX, rangeY) * 0.1

        val viewMinX = minX - margin
        val viewMaxX = maxX + margin
        val viewMinY = minY - margin
        val viewMaxY = maxY + margin

        val viewRangeX = viewMaxX - viewMinX
        val viewRangeY = viewMaxY - viewMinY

        val scaleX = plotW / viewRangeX
        val scaleY = plotH / viewRangeY
        val scale = minOf(scaleX, scaleY)

        val offsetX = padding + (plotW - viewRangeX * scale).toFloat() / 2f
        val offsetY = padding + (plotH - viewRangeY * scale).toFloat() / 2f

        fun toScreen(east: Double, north: Double): Offset {
            val sx = offsetX + ((east - viewMinX) * scale).toFloat()
            val sy = offsetY + ((viewMaxY - north) * scale).toFloat()
            return Offset(sx, sy)
        }

        drawGrid(viewMinX, viewMaxX, viewMinY, viewMaxY, textMeasurer, ::toScreen)

        if (gpsTrack.size >= 2) {
            drawTrackPath(gpsTrack, NeonMagenta, ::toScreen)
        }
        if (filteredTrack.size >= 2) {
            drawTrackPath(filteredTrack, NeonCyan, ::toScreen)
        }

        gpsTrack.lastOrNull()?.let { (e, n) ->
            val center = toScreen(e, n)
            drawCircle(NeonMagenta.copy(alpha = 0.3f), radius = 12f, center = center)
            drawCircle(NeonMagenta, radius = 6f, center = center)
        }
        filteredTrack.lastOrNull()?.let { (e, n) ->
            val center = toScreen(e, n)
            drawCircle(NeonCyan.copy(alpha = 0.3f), radius = 12f, center = center)
            drawCircle(NeonCyan, radius = 6f, center = center)
        }
    }
}

private fun DrawScope.drawEmptyState(textMeasurer: TextMeasurer) {
    val result = textMeasurer.measure(
        "Waiting for GPS fix…",
        style = TextStyle(
            fontSize = 16.sp,
            color = SecondaryText,
            fontFamily = FontFamily.Monospace,
        ),
    )
    drawText(
        result,
        topLeft = Offset(
            (size.width - result.size.width) / 2f,
            (size.height - result.size.height) / 2f,
        ),
    )
}

private fun DrawScope.drawTrackPath(
    points: List<Pair<Double, Double>>,
    color: androidx.compose.ui.graphics.Color,
    toScreen: (Double, Double) -> Offset,
) {
    val path = Path()
    val first = toScreen(points[0].first, points[0].second)
    path.moveTo(first.x, first.y)
    for (i in 1 until points.size) {
        val p = toScreen(points[i].first, points[i].second)
        path.lineTo(p.x, p.y)
    }
    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}

private fun DrawScope.drawGrid(
    minX: Double, maxX: Double,
    minY: Double, maxY: Double,
    textMeasurer: TextMeasurer,
    toScreen: (Double, Double) -> Offset,
) {
    val gridSpacing = niceGridSpacing(max(maxX - minX, maxY - minY))
    val labelStyle = TextStyle(fontSize = 10.sp, color = SecondaryText, fontFamily = FontFamily.Monospace)

    var gx = ceil(minX / gridSpacing) * gridSpacing
    while (gx <= maxX) {
        val top = toScreen(gx, maxY)
        val bot = toScreen(gx, minY)
        drawLine(GridColor, top, bot, strokeWidth = 1f)
        val label = "${gx.roundToInt()}m"
        val result = textMeasurer.measure(label, labelStyle)
        drawText(result, topLeft = Offset(bot.x - result.size.width / 2f, bot.y + 2f))
        gx += gridSpacing
    }

    var gy = ceil(minY / gridSpacing) * gridSpacing
    while (gy <= maxY) {
        val left = toScreen(minX, gy)
        val right = toScreen(maxX, gy)
        drawLine(GridColor, left, right, strokeWidth = 1f)
        val label = "${gy.roundToInt()}m"
        val result = textMeasurer.measure(label, labelStyle)
        drawText(result, topLeft = Offset(left.x - result.size.width - 4f, left.y - result.size.height / 2f))
        gy += gridSpacing
    }
}

private fun niceGridSpacing(range: Double): Double {
    val rough = range / 6.0
    val exponent = floor(Math.log10(rough)).toInt()
    val base = 10.0.pow(exponent)
    val normalized = rough / base
    val nice = when {
        normalized < 1.5 -> 1.0
        normalized < 3.5 -> 2.0
        normalized < 7.5 -> 5.0
        else -> 10.0
    }
    return nice * base
}
