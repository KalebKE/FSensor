package com.tracqi.fsensorapp.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import com.tracqi.fsensorapp.ui.theme.NeonAmber
import com.tracqi.fsensorapp.ui.theme.NeonCyan
import com.tracqi.fsensorapp.ui.theme.RotationGround
import com.tracqi.fsensorapp.ui.theme.RotationSky
import kotlin.math.cos
import kotlin.math.sin

private const val PI = Math.PI.toFloat()

@Composable
fun RotationGauge(
    heading: Float,
    pitch: Float,
    roll: Float,
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

        // Glow rings (behind everything)
        drawCircle(NeonCyan.copy(alpha = 0.08f), radius = outerR * 1.06f, center = Offset(cx, cy), style = Stroke(s * 0.04f))
        drawCircle(NeonCyan.copy(alpha = 0.15f), radius = outerR * 1.03f, center = Offset(cx, cy), style = Stroke(s * 0.03f))
        drawCircle(NeonCyan.copy(alpha = 0.30f), radius = outerR * 1.01f, center = Offset(cx, cy), style = Stroke(s * 0.025f))

        // Tick marks on bezel (outside clip, 4 cardinal + 4 intercardinal)
        for (i in 0 until 8) {
            val angle = i * PI / 4f
            val isCardinal = i % 2 == 0
            val tickLen = if (isCardinal) s * 0.06f else s * 0.04f
            val alpha = if (isCardinal) 0.7f else 0.4f
            val tickStart = outerR + s * 0.005f
            val tickEnd = tickStart + tickLen
            drawLine(
                NeonCyan.copy(alpha = alpha),
                Offset(cx + cos(angle - PI / 2f) * tickStart, cy + sin(angle - PI / 2f) * tickStart),
                Offset(cx + cos(angle - PI / 2f) * tickEnd, cy + sin(angle - PI / 2f) * tickEnd),
                strokeWidth = s * 0.008f
            )
        }

        // Azimuth indicator on bezel
        val headingAngle = heading - PI / 2f
        val triCenter = outerR + s * 0.04f
        val triHalf = s * 0.022f
        val triTip = outerR + s * 0.005f
        val triCos = cos(headingAngle)
        val triSin = sin(headingAngle)
        val perpCos = cos(headingAngle + PI / 2f)
        val perpSin = sin(headingAngle + PI / 2f)
        val triPath = Path().apply {
            moveTo(cx + triCos * triTip, cy + triSin * triTip)
            lineTo(cx + triCos * triCenter + perpCos * triHalf, cy + triSin * triCenter + perpSin * triHalf)
            lineTo(cx + triCos * triCenter - perpCos * triHalf, cy + triSin * triCenter - perpSin * triHalf)
            close()
        }
        drawPath(triPath, NeonAmber)

        val clipCircle = Path().apply {
            addOval(Rect(cx - outerR, cy - outerR, cx + outerR, cy + outerR))
        }

        val rollDeg = Math.toDegrees(roll.toDouble()).toFloat()
        val pitchOffset = (-pitch / (PI / 2f)) * outerR

        clipPath(clipCircle) {
            rotate(degrees = -rollDeg, pivot = Offset(cx, cy)) {
                val horizonY = cy + pitchOffset
                drawRect(RotationSky, topLeft = Offset(0f, 0f), size = Size(size.width, horizonY))
                drawRect(RotationGround, topLeft = Offset(0f, horizonY), size = Size(size.width, size.height - horizonY))
                drawLine(NeonCyan.copy(alpha = 0.7f), Offset(0f, horizonY), Offset(size.width, horizonY), strokeWidth = s * 0.006f)
            }
        }

        // Main rim
        drawCircle(NeonCyan, radius = outerR, center = Offset(cx, cy), style = Stroke(s * 0.02f))

        // Center reference
        drawLine(NeonCyan.copy(alpha = 0.8f), Offset(cx - s * 0.08f, cy), Offset(cx + s * 0.08f, cy), strokeWidth = s * 0.006f)
        drawCircle(NeonCyan.copy(alpha = 0.8f), radius = s * 0.012f, center = Offset(cx, cy), style = Stroke(s * 0.004f))
    }
}
