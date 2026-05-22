package com.tracqi.fsensor.math.rotation

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object RotationMath {

    private const val EPSILON = 0.000000001f

    fun integrateGyroscope(previous: Quaternion, rateOfRotation: FloatArray, dt: Float): Quaternion {
        var wx = rateOfRotation[0]
        var wy = rateOfRotation[1]
        var wz = rateOfRotation[2]

        val magnitude = sqrt(wx * wx + wy * wy + wz * wz)
        if (magnitude > EPSILON) {
            wx /= magnitude
            wy /= magnitude
            wz /= magnitude
        }

        val thetaOverTwo = magnitude * dt / 2.0f
        val sinThetaOverTwo = sin(thetaOverTwo)
        val cosThetaOverTwo = cos(thetaOverTwo)

        val delta = Quaternion(
            cosThetaOverTwo.toDouble(),
            (sinThetaOverTwo * wx).toDouble(),
            (sinThetaOverTwo * wy).toDouble(),
            (sinThetaOverTwo * wz).toDouble()
        )

        return previous.multiply(delta)
    }

    fun quaternionFromMatrix(m: FloatArray): Quaternion {
        val m00 = m[0].toDouble(); val m01 = m[1].toDouble(); val m02 = m[2].toDouble()
        val m10 = m[3].toDouble(); val m11 = m[4].toDouble(); val m12 = m[5].toDouble()
        val m20 = m[6].toDouble(); val m21 = m[7].toDouble(); val m22 = m[8].toDouble()

        val trace = m00 + m11 + m22
        val w: Double; val x: Double; val y: Double; val z: Double

        if (trace > 0) {
            val s = 0.5 / sqrt(trace + 1.0)
            w = 0.25 / s
            x = (m21 - m12) * s
            y = (m02 - m20) * s
            z = (m10 - m01) * s
        } else if (m00 > m11 && m00 > m22) {
            val s = 2.0 * sqrt(1.0 + m00 - m11 - m22)
            w = (m21 - m12) / s
            x = 0.25 * s
            y = (m01 + m10) / s
            z = (m02 + m20) / s
        } else if (m11 > m22) {
            val s = 2.0 * sqrt(1.0 + m11 - m00 - m22)
            w = (m02 - m20) / s
            x = (m01 + m10) / s
            y = 0.25 * s
            z = (m12 + m21) / s
        } else {
            val s = 2.0 * sqrt(1.0 + m22 - m00 - m11)
            w = (m10 - m01) / s
            x = (m02 + m20) / s
            y = (m12 + m21) / s
            z = 0.25 * s
        }

        val norm = sqrt(w * w + x * x + y * y + z * z)
        return Quaternion(w / norm, x / norm, y / norm, z / norm)
    }

    fun getRotationMatrix(acceleration: FloatArray, magnetic: FloatArray): FloatArray? {
        var ax = acceleration[0]; var ay = acceleration[1]; var az = acceleration[2]
        val normsqA = ax * ax + ay * ay + az * az
        val g = 9.81f
        val freeFallGravitySquared = 0.01f * (g * g)
        if (normsqA < freeFallGravitySquared) return null

        val ex = magnetic[0]; val ey = magnetic[1]; val ez = magnetic[2]

        // East = magnetic x gravity (cross product)
        var hx = ey * az - ez * ay
        var hy = ez * ax - ex * az
        var hz = ex * ay - ey * ax
        val normH = sqrt(hx * hx + hy * hy + hz * hz)
        if (normH < 0.1f) return null

        val invH = 1.0f / normH
        hx *= invH; hy *= invH; hz *= invH

        // Normalize gravity (up)
        val invA = 1.0f / sqrt(normsqA)
        ax *= invA; ay *= invA; az *= invA

        // North = gravity x east
        val mx = ay * hz - az * hy
        val my = az * hx - ax * hz
        val mz = ax * hy - ay * hx

        return floatArrayOf(
            hx, hy, hz,
            mx, my, mz,
            ax, ay, az
        )
    }
}
