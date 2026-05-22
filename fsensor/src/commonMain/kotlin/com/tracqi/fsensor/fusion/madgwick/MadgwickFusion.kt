package com.tracqi.fsensor.fusion.madgwick

import com.tracqi.fsensor.fusion.FusionAlgorithm
import com.tracqi.fsensor.math.angle.Angles
import kotlin.math.sqrt

class MadgwickFusion(private val beta: Float = 0.033f) : FusionAlgorithm {
    private var q0 = 1.0
    private var q1 = 0.0
    private var q2 = 0.0
    private var q3 = 0.0

    override fun update(acceleration: FloatArray, magnetic: FloatArray?, gyroscope: FloatArray, dt: Float) {
        var gx = gyroscope[0].toDouble()
        var gy = gyroscope[1].toDouble()
        var gz = gyroscope[2].toDouble()

        var ax = acceleration[0].toDouble()
        var ay = acceleration[1].toDouble()
        var az = acceleration[2].toDouble()

        val normA = sqrt(ax * ax + ay * ay + az * az)
        if (normA == 0.0) return
        ax /= normA; ay /= normA; az /= normA

        var s0: Double; var s1: Double; var s2: Double; var s3: Double

        if (magnetic != null) {
            var mx = magnetic[0].toDouble()
            var my = magnetic[1].toDouble()
            var mz = magnetic[2].toDouble()
            val normM = sqrt(mx * mx + my * my + mz * mz)
            if (normM == 0.0) return
            mx /= normM; my /= normM; mz /= normM

            val _2q0 = 2.0 * q0; val _2q1 = 2.0 * q1; val _2q2 = 2.0 * q2; val _2q3 = 2.0 * q3
            val q0q0 = q0 * q0; val q0q1 = q0 * q1; val q0q2 = q0 * q2; val q0q3 = q0 * q3
            val q1q1 = q1 * q1; val q1q2 = q1 * q2; val q1q3 = q1 * q3
            val q2q2 = q2 * q2; val q2q3 = q2 * q3; val q3q3 = q3 * q3

            val hx = mx * q0q0 - 2.0 * mx * q2q2 - 2.0 * mx * q3q3 + 2.0 * my * (q0q3 + q1q2) + 2.0 * my * q1q2 - 2.0 * mz * (q0q2 - q1q3) + 2.0 * mz * q1q3
            val hy = -2.0 * mx * (q0q3 - q1q2) + my * q0q0 - 2.0 * my * q1q1 - 2.0 * my * q3q3 + 2.0 * mz * (q0q1 + q2q3) + 2.0 * mz * q2q3

            val _2bx = sqrt(hx * hx + hy * hy)
            val _2bz = -2.0 * mx * (q0q2 - q1q3) - 2.0 * my * (q0q1 + q2q3) + mz * q0q0 - 2.0 * mz * q1q1 - 2.0 * mz * q2q2 + mz * q3q3

            val f1 = 2.0 * (q1q3 - q0q2) - ax
            val f2 = 2.0 * (q0q1 + q2q3) - ay
            val f3 = 2.0 * (0.5 - q1q1 - q2q2) - az
            val f4 = 2.0 * _2bx * (0.5 - q2q2 - q3q3) + 2.0 * _2bz * (q1q3 - q0q2) - mx
            val f5 = 2.0 * _2bx * (q1q2 - q0q3) + 2.0 * _2bz * (q0q1 + q2q3) - my
            val f6 = 2.0 * _2bx * (q0q2 + q1q3) + 2.0 * _2bz * (0.5 - q1q1 - q2q2) - mz

            val j11or24 = _2q2; val j12or23 = _2q3; val j13or22 = _2q0; val j14or21 = _2q1
            val j32 = 2.0 * j14or21; val j33 = 2.0 * j11or24
            val j41 = 2.0 * _2bz * q2; val j42 = 2.0 * _2bz * q3
            val j43 = 4.0 * _2bx * q2 + 2.0 * _2bz * q0; val j44 = 4.0 * _2bx * q3 - 2.0 * _2bz * q1
            val j51 = 2.0 * _2bx * q3 - 2.0 * _2bz * q1; val j52 = 2.0 * _2bx * q2 + 2.0 * _2bz * q0
            val j53 = 2.0 * _2bx * q1 + 2.0 * _2bz * q3; val j54 = 2.0 * _2bx * q0 - 2.0 * _2bz * q2
            val j61 = 2.0 * _2bx * q2; val j62 = 2.0 * _2bx * q3 - 4.0 * _2bz * q1
            val j63 = 2.0 * _2bx * q0 - 4.0 * _2bz * q2; val j64 = 2.0 * _2bx * q1

            s0 = -j13or22 * f1 + j14or21 * f2 + -j41 * f4 + -j51 * f5 + j61 * f6
            s1 = j12or23 * f1 + j13or22 * f2 - j32 * f3 + -j42 * f4 + j52 * f5 + j62 * f6
            s2 = -j11or24 * f1 + j14or21 * f2 - j33 * f3 - j43 * f4 + j53 * f5 + j63 * f6
            s3 = j12or23 * f1 + -j11or24 * f2 + -j44 * f4 - j54 * f5 + j64 * f6
        } else {
            val _2q0 = 2.0 * q0; val _2q1 = 2.0 * q1; val _2q2 = 2.0 * q2; val _2q3 = 2.0 * q3
            val _4q0 = 4.0 * q0; val _4q1 = 4.0 * q1; val _4q2 = 4.0 * q2
            val q0q0 = q0 * q0; val q1q1 = q1 * q1; val q2q2 = q2 * q2; val q3q3 = q3 * q3
            val _8q1 = 8.0 * q1; val _8q2 = 8.0 * q2

            s0 = _4q0 * q2q2 + _2q2 * ax + _4q0 * q1q1 - _2q1 * ay
            s1 = _4q1 * q3q3 - _2q3 * ax + 4.0 * q0q0 * q1 - _2q0 * ay - _4q1 + _8q1 * q1q1 + _8q1 * q2q2 + _4q1 * az
            s2 = 4.0 * q0q0 * q2 + _2q0 * ax + _4q2 * q3q3 - _2q3 * ay - _4q2 + _8q2 * q1q1 + _8q2 * q2q2 + _4q2 * az
            s3 = 4.0 * q1q1 * q3 - _2q1 * ax + 4.0 * q2q2 * q3 - _2q2 * ay
        }

        val normS = sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3)
        if (normS > 0.0) {
            s0 /= normS; s1 /= normS; s2 /= normS; s3 /= normS
        }

        val qDot0 = 0.5 * (-q1 * gx - q2 * gy - q3 * gz) - beta * s0
        val qDot1 = 0.5 * (q0 * gx + q2 * gz - q3 * gy) - beta * s1
        val qDot2 = 0.5 * (q0 * gy - q1 * gz + q3 * gx) - beta * s2
        val qDot3 = 0.5 * (q0 * gz + q1 * gy - q2 * gx) - beta * s3

        q0 += qDot0 * dt
        q1 += qDot1 * dt
        q2 += qDot2 * dt
        q3 += qDot3 * dt

        val norm = sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3)
        q0 /= norm; q1 /= norm; q2 /= norm; q3 /= norm
    }

    override fun getOrientation(): FloatArray = Angles.getAngles(q0, q1, q2, q3)
    override fun getQuaternion(): DoubleArray = doubleArrayOf(q0, q1, q2, q3)
    override fun reset() { q0 = 1.0; q1 = 0.0; q2 = 0.0; q3 = 0.0 }
}
