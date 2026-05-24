package com.tracqi.fsensor.fusion.kalman

import com.tracqi.fsensor.fusion.FusionAlgorithm
import com.tracqi.fsensor.math.angle.Angles
import com.tracqi.fsensor.math.rotation.Quaternion
import com.tracqi.fsensor.math.rotation.RotationMath
import kotlin.math.sqrt

class KalmanFusion(
    private val processNoise: Double = 0.001,
    private val measurementNoise: Double = 0.1
) : FusionAlgorithm {

    private var state = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
    private var p = Array(4) { i -> DoubleArray(4) { j -> if (i == j) 0.1 else 0.0 } }
    private var rotationVector = Quaternion.IDENTITY
    private var initialized = false

    override fun update(acceleration: FloatArray, magnetic: FloatArray?, gyroscope: FloatArray, dt: Float) {
        if (!initialized) {
            if (magnetic != null) {
                val rm = RotationMath.getRotationMatrix(acceleration, magnetic)
                if (rm != null) {
                    rotationVector = RotationMath.quaternionFromMatrix(rm)
                    initialized = true
                }
            }
            return
        }

        rotationVector = RotationMath.integrateGyroscope(rotationVector, gyroscope, dt).normalize()
        val gyroState = doubleArrayOf(
            rotationVector.x, rotationVector.y, rotationVector.z, rotationVector.w
        )

        // Predict: x = gyroState (gyro integration serves as the state transition)
        for (i in 0..3) state[i] = gyroState[i]
        for (i in 0..3) p[i][i] += processNoise

        // Correct with accelerometer/magnetometer measurement
        if (magnetic != null) {
            val rm = RotationMath.getRotationMatrix(acceleration, magnetic)
            if (rm != null) {
                val measQuat = RotationMath.quaternionFromMatrix(rm)
                val measurement = doubleArrayOf(measQuat.x, measQuat.y, measQuat.z, measQuat.w)

                // S = P + R (H=I so S = P + R*I)
                val s = Array(4) { i -> DoubleArray(4) { j ->
                    p[i][j] + (if (i == j) measurementNoise else 0.0)
                }}

                val sInv = invert4(s)
                if (sInv != null) {
                    // K = P * S^-1
                    val k = matMul4(p, sInv)

                    // innovation = z - x
                    val innovation = DoubleArray(4) { measurement[it] - state[it] }

                    // x = x + K * innovation
                    for (i in 0..3) {
                        for (j in 0..3) {
                            state[i] += k[i][j] * innovation[j]
                        }
                    }

                    // P = (I - K) * P
                    val iMinusK = Array(4) { i -> DoubleArray(4) { j ->
                        (if (i == j) 1.0 else 0.0) - k[i][j]
                    }}
                    p = matMul4(iMinusK, p)
                }
            }
        }

        // Normalize state quaternion
        val norm = sqrt(state[0] * state[0] + state[1] * state[1] + state[2] * state[2] + state[3] * state[3])
        if (norm > 0.0) {
            for (i in 0..3) state[i] /= norm
        }

        // state is [x, y, z, w] internally
        rotationVector = Quaternion(state[3], state[0], state[1], state[2])
    }

    override fun getOrientation(): FloatArray =
        Angles.getAngles(rotationVector.w, rotationVector.x, rotationVector.y, rotationVector.z)

    override fun getQuaternion(): DoubleArray =
        doubleArrayOf(rotationVector.w, rotationVector.x, rotationVector.y, rotationVector.z)

    override fun reset() {
        state = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
        p = Array(4) { i -> DoubleArray(4) { j -> if (i == j) 0.1 else 0.0 } }
        rotationVector = Quaternion.IDENTITY
        initialized = false
    }

    private fun matMul4(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        return Array(4) { i -> DoubleArray(4) { j ->
            var sum = 0.0
            for (k in 0..3) sum += a[i][k] * b[k][j]
            sum
        }}
    }

    private fun invert4(m: Array<DoubleArray>): Array<DoubleArray>? {
        val a = Array(4) { i -> DoubleArray(8) { j ->
            if (j < 4) m[i][j] else if (j - 4 == i) 1.0 else 0.0
        }}
        for (col in 0..3) {
            var maxRow = col
            for (row in col + 1..3) {
                if (kotlin.math.abs(a[row][col]) > kotlin.math.abs(a[maxRow][col])) maxRow = row
            }
            val tmp = a[col]; a[col] = a[maxRow]; a[maxRow] = tmp
            if (kotlin.math.abs(a[col][col]) < 1e-12) return null
            val pivot = a[col][col]
            for (j in 0..7) a[col][j] /= pivot
            for (row in 0..3) {
                if (row != col) {
                    val factor = a[row][col]
                    for (j in 0..7) a[row][j] -= factor * a[col][j]
                }
            }
        }
        return Array(4) { i -> DoubleArray(4) { j -> a[i][j + 4] } }
    }
}
