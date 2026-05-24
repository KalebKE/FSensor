package com.tracqi.fsensorapp.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import com.tracqi.fsensorapp.ui.theme.GaugeInterior
import com.tracqi.fsensorapp.ui.theme.NeonCyan
import kotlin.math.cos
import kotlin.math.sin

private const val GRAVITY = 9.81f
private const val PI = Math.PI.toFloat()

@Composable
fun AccelerationGauge(
    x: Float,
    y: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val s = size.minDimension
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = s * 0.44f

        // Glow rings
        drawCircle(NeonCyan.copy(alpha = 0.08f), radius = outerR * 1.06f, center = Offset(cx, cy), style = Stroke(s * 0.04f))
        drawCircle(NeonCyan.copy(alpha = 0.15f), radius = outerR * 1.03f, center = Offset(cx, cy), style = Stroke(s * 0.03f))
        drawCircle(NeonCyan.copy(alpha = 0.30f), radius = outerR * 1.01f, center = Offset(cx, cy), style = Stroke(s * 0.025f))

        // Interior fill
        drawCircle(GaugeInterior, radius = outerR, center = Offset(cx, cy))

        // Main rim
        drawCircle(NeonCyan, radius = outerR, center = Offset(cx, cy), style = Stroke(s * 0.02f))

        // Inner ring
        val innerR = s * 0.30f
        drawCircle(GaugeInterior, radius = innerR, center = Offset(cx, cy))
        drawCircle(NeonCyan.copy(alpha = 0.4f), radius = innerR, center = Offset(cx, cy), style = Stroke(s * 0.008f))

        // Center dot
        drawCircle(NeonCyan.copy(alpha = 0.6f), radius = s * 0.012f, center = Offset(cx, cy))

        // Major ticks (8, every 45°)
        for (i in 0 until 8) {
            val angle = i * PI / 4f
            val tickStart = outerR - s * 0.06f
            val tickEnd = outerR - s * 0.002f
            drawLine(
                NeonCyan.copy(alpha = 0.6f),
                Offset(cx + cos(angle) * tickStart, cy + sin(angle) * tickStart),
                Offset(cx + cos(angle) * tickEnd, cy + sin(angle) * tickEnd),
                strokeWidth = s * 0.008f
            )
        }

        // Minor ticks (8, offset 22.5°)
        for (i in 0 until 8) {
            val angle = (i * 45f + 22.5f) * PI / 180f
            val tickStart = outerR - s * 0.03f
            val tickEnd = outerR - s * 0.002f
            drawLine(
                NeonCyan.copy(alpha = 0.3f),
                Offset(cx + cos(angle) * tickStart, cy + sin(angle) * tickStart),
                Offset(cx + cos(angle) * tickEnd, cy + sin(angle) * tickEnd),
                strokeWidth = s * 0.005f
            )
        }

        // Moving dot
        val clampedX = x.coerceIn(-GRAVITY, GRAVITY)
        val clampedY = y.coerceIn(-GRAVITY, GRAVITY)
        val px = cx + (-clampedX / GRAVITY) * outerR
        val py = cy + (clampedY / GRAVITY) * outerR
        val dotR = s * 0.025f

        // Dot glow
        drawCircle(NeonCyan.copy(alpha = 0.15f), radius = dotR * 3f, center = Offset(px, py))
        drawCircle(NeonCyan.copy(alpha = 0.40f), radius = dotR * 1.8f, center = Offset(px, py))
        drawCircle(NeonCyan, radius = dotR, center = Offset(px, py))
    }
}
