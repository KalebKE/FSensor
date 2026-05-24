package com.tracqi.fsensor.fusion.mahony

import com.tracqi.fsensor.fusion.FusionAlgorithm
import com.tracqi.fsensor.math.angle.Angles
import kotlin.math.sqrt

class MahonyFusion(private val kp: Float = 1.0f, private val ki: Float = 0.0f) : FusionAlgorithm {
    private var q0 = 1.0
    private var q1 = 0.0
    private var q2 = 0.0
    private var q3 = 0.0

    private var integralFBx = 0.0
    private var integralFBy = 0.0
    private var integralFBz = 0.0

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

        var ex: Double; var ey: Double; var ez: Double

        if (magnetic != null) {
            var mx = magnetic[0].toDouble()
            var my = magnetic[1].toDouble()
            var mz = magnetic[2].toDouble()
            val normM = sqrt(mx * mx + my * my + mz * mz)
            if (normM == 0.0) {
                ex = 0.0; ey = 0.0; ez = 0.0
            } else {
                mx /= normM; my /= normM; mz /= normM
                val q0q0 = q0 * q0; val q0q1 = q0 * q1; val q0q2 = q0 * q2; val q0q3 = q0 * q3
                val q1q1 = q1 * q1; val q1q2 = q1 * q2; val q1q3 = q1 * q3
                val q2q2 = q2 * q2; val q2q3 = q2 * q3; val q3q3 = q3 * q3

                val hx = 2.0 * (mx * (0.5 - q2q2 - q3q3) + my * (q1q2 - q0q3) + mz * (q1q3 + q0q2))
                val hy = 2.0 * (mx * (q1q2 + q0q3) + my * (0.5 - q1q1 - q3q3) + mz * (q2q3 - q0q1))
                val bx = sqrt(hx * hx + hy * hy)
                val bz = 2.0 * (mx * (q1q3 - q0q2) + my * (q2q3 + q0q1) + mz * (0.5 - q1q1 - q2q2))

                val vx = 2.0 * (q1q3 - q0q2)
                val vy = 2.0 * (q0q1 + q2q3)
                val vz = q0q0 - q1q1 - q2q2 + q3q3

                val wx = 2.0 * bx * (0.5 - q2q2 - q3q3) + 2.0 * bz * (q1q3 - q0q2)
                val wy = 2.0 * bx * (q1q2 - q0q3) + 2.0 * bz * (q0q1 + q2q3)
                val wz = 2.0 * bx * (q0q2 + q1q3) + 2.0 * bz * (0.5 - q1q1 - q2q2)

                ex = (ay * vz - az * vy) + (my * wz - mz * wy)
                ey = (az * vx - ax * vz) + (mz * wx - mx * wz)
                ez = (ax * vy - ay * vx) + (mx * wy - my * wx)
            }
        } else {
            val vx = 2.0 * (q1 * q3 - q0 * q2)
            val vy = 2.0 * (q0 * q1 + q2 * q3)
            val vz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3

            ex = ay * vz - az * vy
            ey = az * vx - ax * vz
            ez = ax * vy - ay * vx
        }

        if (ki > 0.0f) {
            integralFBx += ex * dt
            integralFBy += ey * dt
            integralFBz += ez * dt
            gx += ki * integralFBx
            gy += ki * integralFBy
            gz += ki * integralFBz
        }

        gx += kp * ex
        gy += kp * ey
        gz += kp * ez

        val qDot0 = 0.5 * (-q1 * gx - q2 * gy - q3 * gz)
        val qDot1 = 0.5 * (q0 * gx + q2 * gz - q3 * gy)
        val qDot2 = 0.5 * (q0 * gy - q1 * gz + q3 * gx)
        val qDot3 = 0.5 * (q0 * gz + q1 * gy - q2 * gx)

        q0 += qDot0 * dt
        q1 += qDot1 * dt
        q2 += qDot2 * dt
        q3 += qDot3 * dt

        val norm = sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3)
        q0 /= norm; q1 /= norm; q2 /= norm; q3 /= norm
    }

    override fun getOrientation(): FloatArray = Angles.getAngles(q0, q1, q2, q3)
    override fun getQuaternion(): DoubleArray = doubleArrayOf(q0, q1, q2, q3)
    override fun reset() {
        q0 = 1.0; q1 = 0.0; q2 = 0.0; q3 = 0.0
        integralFBx = 0.0; integralFBy = 0.0; integralFBz = 0.0
    }
}
