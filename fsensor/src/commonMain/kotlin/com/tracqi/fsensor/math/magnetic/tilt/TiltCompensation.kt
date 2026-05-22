package com.tracqi.fsensor.math.magnetic.tilt

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object TiltCompensation {

    fun getRotationFromAcceleration(acceleration: FloatArray): FloatArray {
        val phi = atan2(acceleration[1], acceleration[2])
        val theta = atan2(-acceleration[0], acceleration[2])
        return floatArrayOf(phi, theta, 0f)
    }

    fun compensateTilt(magnetic: FloatArray, rotation: FloatArray): FloatArray {
        val bpx = magnetic[0]
        val bpy = magnetic[1]
        var bpz = magnetic[2]

        var sinVal = sin(rotation[0])
        var cosVal = cos(rotation[0])

        val bfy = bpy * cosVal - bpz * sinVal
        bpz = bpy * sinVal + bpz * cosVal

        sinVal = sin(rotation[1])
        cosVal = cos(rotation[1])

        val bfx = bpx * cosVal + bpz * sinVal
        val bfz = -bpx * sinVal + bpz * cosVal

        return floatArrayOf(bfx, bfy, bfz)
    }
}
