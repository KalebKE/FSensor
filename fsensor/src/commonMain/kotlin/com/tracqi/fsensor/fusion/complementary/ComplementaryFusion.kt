package com.tracqi.fsensor.fusion.complementary

import com.tracqi.fsensor.fusion.FusionAlgorithm
import com.tracqi.fsensor.math.angle.Angles
import com.tracqi.fsensor.math.rotation.Quaternion
import com.tracqi.fsensor.math.rotation.RotationMath
import kotlin.math.sqrt

class ComplementaryFusion(private val timeConstant: Float = 0.18f) : FusionAlgorithm {
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

        val alpha = timeConstant / (timeConstant + dt)
        val oneMinusAlpha = 1.0f - alpha

        if (magnetic != null) {
            val rm = RotationMath.getRotationMatrix(acceleration, magnetic)
            if (rm != null) {
                val accelMagQuat = RotationMath.quaternionFromMatrix(rm)
                rotationVector = RotationMath.integrateGyroscope(rotationVector, gyroscope, dt)
                val scaledGyro = rotationVector.scale(alpha.toDouble())
                val scaledAccelMag = accelMagQuat.scale(oneMinusAlpha.toDouble())
                rotationVector = scaledGyro.add(scaledAccelMag).normalize()
            }
        } else {
            rotationVector = RotationMath.integrateGyroscope(rotationVector, gyroscope, dt)
        }
    }

    override fun getOrientation(): FloatArray =
        Angles.getAngles(rotationVector.w, rotationVector.x, rotationVector.y, rotationVector.z)

    override fun getQuaternion(): DoubleArray =
        doubleArrayOf(rotationVector.w, rotationVector.x, rotationVector.y, rotationVector.z)

    override fun reset() {
        rotationVector = Quaternion.IDENTITY
        initialized = false
    }
}
