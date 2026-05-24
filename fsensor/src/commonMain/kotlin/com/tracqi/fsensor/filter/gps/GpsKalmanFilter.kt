package com.tracqi.fsensor.filter.gps

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * 8-state linear Kalman filter for GPS/IMU position fusion.
 *
 * State vector: [east, north, vx, vy, ax, ay, speed, longitudinal_accel]
 * Measurement vector: [gps_east, gps_north, gps_speed, imu_accel]
 *
 * The caller provides a heading angle that decomposes scalar speed and
 * longitudinal acceleration into east/north components via the state
 * transition matrix.
 *
 * Coordinate convention: East-North-Up (ENU). Use [com.tracqi.fsensor.math.coordinate.CoordinateConversion]
 * to convert GPS lat/lon to ENU meters relative to a chosen origin.
 */
class GpsKalmanFilter(
    private val positionNoiseVariance: Double = 9.0,
    private val velocityNoiseVariance: Double = 0.09,
    private val accelNoiseVariance: Double = 0.0225
) {
    private val n = 8
    private val m = 4

    private val qDiag = doubleArrayOf(0.05, 0.05, 2.0, 2.0, 10.0, 10.0, 0.1, 10.0)
    private val p0Diag = doubleArrayOf(1.0, 1.0, 5.0, 5.0, 10.0, 10.0, 1.0, 1.0)

    private var x = DoubleArray(n)
    private var p = diagonal(p0Diag)

    private val h = buildH()

    fun predict(headingRadians: Double, dt: Double) {
        val a = buildA(dt, headingRadians)
        x = matVecMul(a, x)
        val at = transpose(a)
        p = matAdd(matMul(matMul(a, p), at), diagonal(qDiag))
    }

    fun correct(
        eastMeters: Double,
        northMeters: Double,
        speedMps: Double,
        longitudinalAccelMps2: Double,
        hasGpsFix: Boolean
    ) {
        val z = doubleArrayOf(eastMeters, northMeters, speedMps, longitudinalAccelMps2)
        val r = if (hasGpsFix) {
            diagonal(doubleArrayOf(positionNoiseVariance, positionNoiseVariance, velocityNoiseVariance, accelNoiseVariance))
        } else {
            diagonal(doubleArrayOf(1e6, 1e6, velocityNoiseVariance, accelNoiseVariance))
        }

        val ht = transpose(h)
        val s = matAdd(matMul(matMul(h, p), ht), r)
        val sInv = invert(s) ?: return
        val k = matMul(matMul(p, ht), sInv)

        val innovation = DoubleArray(m) { z[it] - dotRow(h, it, x) }
        val kInnovation = matVecMul(k, innovation)
        for (i in 0 until n) x[i] += kInnovation[i]

        // Joseph form: P = (I - K*H) * P * (I - K*H)^T + K * R * K^T
        val iKH = matSub(identity(n), matMul(k, h))
        val iKHt = transpose(iKH)
        val kt = transpose(k)
        p = matAdd(matMul(matMul(iKH, p), iKHt), matMul(matMul(k, r), kt))
    }

    fun getState(): GpsState = GpsState(
        east = x[0], north = x[1],
        velocityEast = x[2], velocityNorth = x[3],
        speed = x[6],
        longitudinalAcceleration = x[7]
    )

    fun reset() {
        x = DoubleArray(n)
        p = diagonal(p0Diag)
    }

    // --- Model builders ---

    private fun buildA(dt: Double, heading: Double): Array<DoubleArray> {
        val sinH = sin(heading)
        val cosH = cos(heading)
        val dt2h = dt.pow(2) / 2.0
        return arrayOf(
            doubleArrayOf(1.0, 0.0, dt,  0.0, dt2h, 0.0,  0.0,  0.0),
            doubleArrayOf(0.0, 1.0, 0.0, dt,  0.0,  dt2h, 0.0,  0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0,  0.0,  sinH, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0,  0.0,  cosH, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0,  0.0,  0.0,  sinH),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0,  0.0,  0.0,  cosH),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0,  0.0,  1.0,  dt),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0,  0.0,  0.0,  1.0),
        )
    }

    private fun buildH(): Array<DoubleArray> = arrayOf(
        doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0),
    )

    // --- Matrix utilities ---

    private fun dotRow(mat: Array<DoubleArray>, row: Int, vec: DoubleArray): Double {
        var sum = 0.0
        for (j in vec.indices) sum += mat[row][j] * vec[j]
        return sum
    }

    private fun matVecMul(mat: Array<DoubleArray>, vec: DoubleArray): DoubleArray {
        val rows = mat.size
        val result = DoubleArray(rows)
        for (i in 0 until rows) {
            var sum = 0.0
            for (j in vec.indices) sum += mat[i][j] * vec[j]
            result[i] = sum
        }
        return result
    }

    private fun matMul(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val rows = a.size
        val cols = b[0].size
        val inner = b.size
        return Array(rows) { i ->
            DoubleArray(cols) { j ->
                var sum = 0.0
                for (k in 0 until inner) sum += a[i][k] * b[k][j]
                sum
            }
        }
    }

    private fun matAdd(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] + b[i][j] } }
    }

    private fun matSub(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        return Array(a.size) { i -> DoubleArray(a[0].size) { j -> a[i][j] - b[i][j] } }
    }

    private fun transpose(mat: Array<DoubleArray>): Array<DoubleArray> {
        val rows = mat.size
        val cols = mat[0].size
        return Array(cols) { j -> DoubleArray(rows) { i -> mat[i][j] } }
    }

    private fun identity(size: Int): Array<DoubleArray> {
        return Array(size) { i -> DoubleArray(size) { j -> if (i == j) 1.0 else 0.0 } }
    }

    private fun diagonal(values: DoubleArray): Array<DoubleArray> {
        val size = values.size
        return Array(size) { i -> DoubleArray(size) { j -> if (i == j) values[i] else 0.0 } }
    }

    private fun invert(mat: Array<DoubleArray>): Array<DoubleArray>? {
        val size = mat.size
        val aug = Array(size) { i ->
            DoubleArray(size * 2) { j ->
                if (j < size) mat[i][j] else if (j - size == i) 1.0 else 0.0
            }
        }
        for (col in 0 until size) {
            var maxRow = col
            for (row in col + 1 until size) {
                if (abs(aug[row][col]) > abs(aug[maxRow][col])) maxRow = row
            }
            val tmp = aug[col]; aug[col] = aug[maxRow]; aug[maxRow] = tmp
            if (abs(aug[col][col]) < 1e-12) return null
            val pivot = aug[col][col]
            for (j in 0 until size * 2) aug[col][j] /= pivot
            for (row in 0 until size) {
                if (row != col) {
                    val factor = aug[row][col]
                    for (j in 0 until size * 2) aug[row][j] -= factor * aug[col][j]
                }
            }
        }
        return Array(size) { i -> DoubleArray(size) { j -> aug[i][j + size] } }
    }
}
