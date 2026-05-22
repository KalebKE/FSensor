package com.tracqi.fsensorapp.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

private const val GRAVITY = 9.81f

@Composable
fun AccelerationGauge(
    x: Float,
    y: Float,
    modifier: Modifier = Modifier
) {
    val rimColor = MaterialTheme.colorScheme.outline
    val dotColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceContainerLow

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val s = size.minDimension
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = s * 0.46f
        val innerR = s * 0.30f
        val dotR = s * 0.03f

        drawCircle(color = bgColor, radius = outerR, center = Offset(cx, cy))
        drawCircle(color = rimColor, radius = outerR, center = Offset(cx, cy), style = Stroke(s * 0.02f))
        drawCircle(color = bgColor, radius = innerR, center = Offset(cx, cy))
        drawCircle(color = rimColor, radius = innerR, center = Offset(cx, cy), style = Stroke(s * 0.01f))
        drawCircle(color = rimColor, radius = s * 0.015f, center = Offset(cx, cy))

        val clampedX = x.coerceIn(-GRAVITY, GRAVITY)
        val clampedY = y.coerceIn(-GRAVITY, GRAVITY)
        val px = cx + (-clampedX / GRAVITY) * outerR
        val py = cy + (clampedY / GRAVITY) * outerR

        drawCircle(color = dotColor, radius = dotR, center = Offset(px, py))
    }
}
