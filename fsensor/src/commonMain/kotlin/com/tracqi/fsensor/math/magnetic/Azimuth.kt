package com.tracqi.fsensor.math.magnetic

import kotlin.math.atan2

object Azimuth {
    fun getAzimuth(magnetic: FloatArray): Float {
        val radians = atan2(magnetic[0].toDouble(), magnetic[1].toDouble())
        val azimuth = (radians * 180.0 / kotlin.math.PI).toFloat()
        return (azimuth + 360f) % 360f
    }
}
