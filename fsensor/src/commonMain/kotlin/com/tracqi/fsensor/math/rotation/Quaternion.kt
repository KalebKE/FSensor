package com.tracqi.fsensor.math.rotation

import kotlin.math.sqrt

data class Quaternion(val w: Double, val x: Double, val y: Double, val z: Double) {

    fun multiply(other: Quaternion): Quaternion = Quaternion(
        w * other.w - x * other.x - y * other.y - z * other.z,
        w * other.x + x * other.w + y * other.z - z * other.y,
        w * other.y - x * other.z + y * other.w + z * other.x,
        w * other.z + x * other.y - y * other.x + z * other.w
    )

    fun normalize(): Quaternion {
        val norm = sqrt(w * w + x * x + y * y + z * z)
        if (norm == 0.0) return IDENTITY
        return Quaternion(w / norm, x / norm, y / norm, z / norm)
    }

    fun scale(s: Double): Quaternion = Quaternion(w * s, x * s, y * s, z * s)

    fun add(other: Quaternion): Quaternion = Quaternion(
        w + other.w, x + other.x, y + other.y, z + other.z
    )

    fun dot(other: Quaternion): Double = w * other.w + x * other.x + y * other.y + z * other.z

    fun conjugate(): Quaternion = Quaternion(w, -x, -y, -z)

    val norm: Double get() = sqrt(w * w + x * x + y * y + z * z)

    companion object {
        val IDENTITY = Quaternion(1.0, 0.0, 0.0, 0.0)
    }
}
