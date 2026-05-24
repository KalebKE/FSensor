package com.tracqi.fsensor.math.gravity

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object Gravity {
    const val EARTH: Float = 9.81f

    fun getGravityFromOrientation(orientation: FloatArray): FloatArray {
        val pitch = orientation[1]
        val roll = orientation[2]
        return floatArrayOf(
            -(EARTH * -cos(pitch) * sin(roll)),
            (EARTH * -sin(pitch)),
            (EARTH * cos(pitch) * cos(roll))
        )
    }

    fun getOrientationFromGravity(gravity: FloatArray): FloatArray {
        val pitch = atan2(-gravity[1], gravity[2])
        val roll = asin(max(-1.0f, min(1.0f, gravity[0] / EARTH)))
        return floatArrayOf(0f, pitch, roll)
    }
}
