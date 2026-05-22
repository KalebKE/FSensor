package com.tracqi.fsensor.fusion.ekf

import com.tracqi.fsensor.fusion.FusionAlgorithm
import com.tracqi.fsensor.math.angle.Angles
import kotlin.math.sqrt

class EkfFusion(
    private val processNoiseVariance: Double = 0.001,
    private val accelNoiseVariance: Double = 0.1,
    private val magNoiseVariance: Double = 0.5
) : FusionAlgorithm {

    private var q = doubleArrayOf(1.0, 0.0, 0.0, 0.0)
    private var p = identityScaled(0.1)

    override fun update(acceleration: FloatArray, magnetic: FloatArray?, gyroscope: FloatArray, dt: Float) {
        predict(gyroscope, dt)
        correctAccelerometer(acceleration)
        if (magnetic != null) correctMagnetometer(magnetic)
    }

    fun predict(gyroscope: FloatArray, dt: Float) {
        val wx = gyroscope[0].toDouble()
        val wy = gyroscope[1].toDouble()
        val wz = gyroscope[2].toDouble()

        val omega = arrayOf(
            doubleArrayOf(0.0, -wx, -wy, -wz),
            doubleArrayOf(wx, 0.0, wz, -wy),
            doubleArrayOf(wy, -wz, 0.0, wx),
            doubleArrayOf(wz, wy, -wx, 0.0)
        )

        val f = Array(4) { i -> DoubleArray(4) { j ->
            (if (i == j) 1.0 else 0.0) + 0.5 * dt * omega[i][j]
        }}

        val newQ = matVecMul4(f, q)
        val norm = sqrt(newQ[0] * newQ[0] + newQ[1] * newQ[1] + newQ[2] * newQ[2] + newQ[3] * newQ[3])
        for (i in 0..3) q[i] = newQ[i] / norm

        val fArr = Array(4) { i -> f[i].copyOf() }
        val ft = transpose4(fArr)
        val fpft = matMul4(matMul4(fArr, p), ft)
        for (i in 0..3) for (j in 0..3) {
            p[i][j] = fpft[i][j] + (if (i == j) processNoiseVariance else 0.0)
        }
    }

    fun correctAccelerometer(acceleration: FloatArray) {
        var ax = acceleration[0].toDouble()
        var ay = acceleration[1].toDouble()
        var az = acceleration[2].toDouble()
        val normA = sqrt(ax * ax + ay * ay + az * az)
        if (normA == 0.0) return
        ax /= normA; ay /= normA; az /= normA

        val q0 = q[0]; val q1 = q[1]; val q2 = q[2]; val q3 = q[3]
        val hx = 2.0 * (q1 * q3 - q0 * q2)
        val hy = 2.0 * (q0 * q1 + q2 * q3)
        val hz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3

        val h = arrayOf(
            doubleArrayOf(-2.0 * q2, 2.0 * q3, -2.0 * q0, 2.0 * q1),
            doubleArrayOf(2.0 * q1, 2.0 * q0, 2.0 * q3, 2.0 * q2),
            doubleArrayOf(2.0 * q0, -2.0 * q1, -2.0 * q2, 2.0 * q3)
        )

        val innovation = doubleArrayOf(ax - hx, ay - hy, az - hz)
        applyCorrection(h, innovation, accelNoiseVariance)
    }

    fun correctMagnetometer(magnetic: FloatArray) {
        var mx = magnetic[0].toDouble()
        var my = magnetic[1].toDouble()
        var mz = magnetic[2].toDouble()
        val normM = sqrt(mx * mx + my * my + mz * mz)
        if (normM == 0.0) return
        mx /= normM; my /= normM; mz /= normM

        val q0 = q[0]; val q1 = q[1]; val q2 = q[2]; val q3 = q[3]

        val rotMx = mx * (q0 * q0 + q1 * q1 - q2 * q2 - q3 * q3) + 2.0 * my * (q1 * q2 - q0 * q3) + 2.0 * mz * (q1 * q3 + q0 * q2)
        val rotMy = 2.0 * mx * (q1 * q2 + q0 * q3) + my * (q0 * q0 - q1 * q1 + q2 * q2 - q3 * q3) + 2.0 * mz * (q2 * q3 - q0 * q1)

        val bx = sqrt(rotMx * rotMx + rotMy * rotMy)
        val bz = 2.0 * mx * (q1 * q3 - q0 * q2) + 2.0 * my * (q2 * q3 + q0 * q1) + mz * (q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3)

        val hx = bx * (q0 * q0 + q1 * q1 - q2 * q2 - q3 * q3) + bz * 2.0 * (q1 * q3 - q0 * q2)
        val hy = bx * 2.0 * (q1 * q2 - q0 * q3) + bz * 2.0 * (q0 * q1 + q2 * q3)
        val hz = bx * 2.0 * (q0 * q2 + q1 * q3) + bz * (q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3)

        val hmx = 2.0 * (q1 * q3 - q0 * q2)
        val hmy = 2.0 * (q0 * q1 + q2 * q3)
        val hmz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3

        val h = arrayOf(
            doubleArrayOf(-2.0 * q2, 2.0 * q3, -2.0 * q0, 2.0 * q1),
            doubleArrayOf(2.0 * q1, 2.0 * q0, 2.0 * q3, 2.0 * q2),
            doubleArrayOf(2.0 * q0, -2.0 * q1, -2.0 * q2, 2.0 * q3)
        )

        val innovation = doubleArrayOf(mx - hmx, my - hmy, mz - hmz)
        applyCorrection(h, innovation, magNoiseVariance)
    }

    private fun applyCorrection(h: Array<DoubleArray>, innovation: DoubleArray, noiseVariance: Double) {
        val ht = Array(4) { i -> DoubleArray(3) { j -> h[j][i] } }
        val pht = Array(4) { i -> DoubleArray(3) { j ->
            var sum = 0.0
            for (k in 0..3) sum += p[i][k] * ht[k][j]
            sum
        }}

        val s = Array(3) { i -> DoubleArray(3) { j ->
            var sum = 0.0
            for (k in 0..3) sum += h[i][k] * pht[k][j]
            sum + (if (i == j) noiseVariance else 0.0)
        }}

        val sInv = invert3(s) ?: return

        val k = Array(4) { i -> DoubleArray(3) { j ->
            var sum = 0.0
            for (m in 0..2) sum += pht[i][m] * sInv[m][j]
            sum
        }}

        for (i in 0..3) {
            for (j in 0..2) {
                q[i] += k[i][j] * innovation[j]
            }
        }

        val norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
        for (i in 0..3) q[i] /= norm

        val kh = Array(4) { i -> DoubleArray(4) { j ->
            var sum = 0.0
            for (m in 0..2) sum += k[i][m] * h[m][j]
            sum
        }}

        val newP = Array(4) { i -> DoubleArray(4) { j ->
            p[i][j] - run {
                var sum = 0.0
                for (m in 0..3) sum += kh[i][m] * p[m][j]
                sum
            }
        }}
        for (i in 0..3) for (j in 0..3) p[i][j] = newP[i][j]
    }

    override fun getOrientation(): FloatArray = Angles.getAngles(q[0], q[1], q[2], q[3])
    override fun getQuaternion(): DoubleArray = q.copyOf()
    override fun reset() {
        q = doubleArrayOf(1.0, 0.0, 0.0, 0.0)
        p = identityScaled(0.1)
    }

    private fun identityScaled(scale: Double) = Array(4) { i -> DoubleArray(4) { j -> if (i == j) scale else 0.0 } }

    private fun matVecMul4(m: Array<DoubleArray>, v: DoubleArray): DoubleArray {
        return DoubleArray(4) { i -> m[i][0] * v[0] + m[i][1] * v[1] + m[i][2] * v[2] + m[i][3] * v[3] }
    }

    private fun matMul4(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        return Array(4) { i -> DoubleArray(4) { j ->
            var sum = 0.0
            for (k in 0..3) sum += a[i][k] * b[k][j]
            sum
        }}
    }

    private fun transpose4(m: Array<DoubleArray>): Array<DoubleArray> {
        return Array(4) { i -> DoubleArray(4) { j -> m[j][i] } }
    }

    private fun invert3(m: Array<DoubleArray>): Array<DoubleArray>? {
        val a = m[0][0]; val b = m[0][1]; val c = m[0][2]
        val d = m[1][0]; val e = m[1][1]; val f = m[1][2]
        val g = m[2][0]; val h = m[2][1]; val i = m[2][2]

        val det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g)
        if (kotlin.math.abs(det) < 1e-12) return null
        val invDet = 1.0 / det

        return arrayOf(
            doubleArrayOf((e * i - f * h) * invDet, (c * h - b * i) * invDet, (b * f - c * e) * invDet),
            doubleArrayOf((f * g - d * i) * invDet, (a * i - c * g) * invDet, (c * d - a * f) * invDet),
            doubleArrayOf((d * h - e * g) * invDet, (b * g - a * h) * invDet, (a * e - b * d) * invDet)
        )
    }
}
