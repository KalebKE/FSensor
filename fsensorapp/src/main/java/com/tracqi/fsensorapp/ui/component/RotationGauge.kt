package com.tracqi.fsensorapp.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun RotationGauge(
    heading: Float,
    pitch: Float,
    roll: Float,
    modifier: Modifier = Modifier
) {
    val rimColor = MaterialTheme.colorScheme.outline
    val skyColor = MaterialTheme.colorScheme.primaryContainer
    val groundColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceContainerLow

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val s = size.minDimension
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = s * 0.44f

        drawCircle(color = rimColor, radius = outerR + s * 0.02f, center = Offset(cx, cy), style = Stroke(s * 0.02f))

        val clipCircle = Path().apply {
            addOval(Rect(cx - outerR, cy - outerR, cx + outerR, cy + outerR))
        }

        val rollDeg = Math.toDegrees(roll.toDouble()).toFloat()
        val pitchOffset = (pitch / (Math.PI.toFloat() / 2f)) * outerR

        clipPath(clipCircle) {
            rotate(degrees = -rollDeg, pivot = Offset(cx, cy)) {
                val horizonY = cy + pitchOffset
                drawRect(color = skyColor, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, horizonY))
                drawRect(color = groundColor, topLeft = Offset(0f, horizonY), size = androidx.compose.ui.geometry.Size(size.width, size.height - horizonY))
                drawLine(color = Color.White, start = Offset(0f, horizonY), end = Offset(size.width, horizonY), strokeWidth = s * 0.005f)
            }
        }

        drawCircle(color = rimColor, radius = outerR, center = Offset(cx, cy), style = Stroke(s * 0.015f))

        drawLine(color = Color.White, start = Offset(cx - s * 0.08f, cy), end = Offset(cx + s * 0.08f, cy), strokeWidth = s * 0.006f)
        drawCircle(color = Color.White, radius = s * 0.012f, center = Offset(cx, cy), style = Stroke(s * 0.004f))
    }
}
