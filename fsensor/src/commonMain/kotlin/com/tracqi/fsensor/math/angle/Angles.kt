package com.tracqi.fsensor.math.angle

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2

object Angles {

    fun getAngles(w: Double, z: Double, x: Double, y: Double): FloatArray {
        val test = x * y + z * w
        if (test > 0.499) {
            val heading = 2.0 * atan2(x, w)
            return floatArrayOf(heading.toFloat(), (-PI / 2).toFloat(), 0f)
        }
        if (test < -0.499) {
            val heading = -2.0 * atan2(x, w)
            return floatArrayOf(heading.toFloat(), (PI / 2).toFloat(), 0f)
        }
        val sqx = x * x
        val sqy = y * y
        val sqz = z * z
        val heading = -atan2(2.0 * y * w - 2.0 * x * z, 1.0 - 2.0 * sqy - 2.0 * sqz)
        val pitch = -asin(2.0 * test)
        val roll = -atan2(2.0 * x * w - 2.0 * y * z, 1.0 - 2.0 * sqx - 2.0 * sqz)
        return floatArrayOf(heading.toFloat(), pitch.toFloat(), roll.toFloat())
    }
}
