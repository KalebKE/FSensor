@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.tracqi.fsensor.platform

import com.tracqi.fsensor.fusion.FusionAlgorithm
import com.tracqi.fsensor.math.gravity.Gravity
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMDeviceMotion
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

class IosSensorProvider(private val fusion: FusionAlgorithm) {
    private val motionManager = CMMotionManager()
    private var lastTimestamp = 0.0
    private var callback: ((FloatArray) -> Unit)? = null

    fun startOrientation(rateHz: Int, onUpdate: (orientation: FloatArray) -> Unit) {
        callback = onUpdate
        motionManager.deviceMotionUpdateInterval = 1.0 / rateHz

        motionManager.startDeviceMotionUpdatesToQueue(
            NSOperationQueue.mainQueue
        ) { motion, _ ->
            val m = motion ?: return@startDeviceMotionUpdatesToQueue
            val (accel, gyro, mag) = extractSensorData(m)
            val now = m.timestamp
            if (lastTimestamp > 0.0) {
                val dt = (now - lastTimestamp).toFloat()
                val hasMag = mag[0] != 0f || mag[1] != 0f || mag[2] != 0f
                fusion.update(accel, if (hasMag) mag else null, gyro, dt)
                onUpdate(fusion.getOrientation())
            }
            lastTimestamp = now
        }
    }

    fun startLinearAcceleration(rateHz: Int, onUpdate: (linearAccel: FloatArray) -> Unit) {
        motionManager.deviceMotionUpdateInterval = 1.0 / rateHz

        motionManager.startDeviceMotionUpdatesToQueue(
            NSOperationQueue.mainQueue
        ) { motion, _ ->
            val m = motion ?: return@startDeviceMotionUpdatesToQueue
            val (accel, gyro, mag) = extractSensorData(m)
            val now = m.timestamp
            if (lastTimestamp > 0.0) {
                val dt = (now - lastTimestamp).toFloat()
                val hasMag = mag[0] != 0f || mag[1] != 0f || mag[2] != 0f
                fusion.update(accel, if (hasMag) mag else null, gyro, dt)
                val orientation = fusion.getOrientation()
                val gravityVec = Gravity.getGravityFromOrientation(orientation)
                onUpdate(floatArrayOf(
                    accel[0] - gravityVec[0],
                    accel[1] - gravityVec[1],
                    accel[2] - gravityVec[2]
                ))
            }
            lastTimestamp = now
        }
    }

    private fun extractSensorData(motion: CMDeviceMotion): Triple<FloatArray, FloatArray, FloatArray> {
        val accel = motion.gravity.useContents {
            val ua = motion.userAcceleration
            ua.useContents {
                floatArrayOf(
                    ((this@useContents.x + this.x) * 9.81).toFloat(),
                    ((this@useContents.y + this.y) * 9.81).toFloat(),
                    ((this@useContents.z + this.z) * 9.81).toFloat()
                )
            }
        }
        val gyro = motion.rotationRate.useContents {
            floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
        }
        val mag = motion.magneticField.useContents {
            floatArrayOf(field.x.toFloat(), field.y.toFloat(), field.z.toFloat())
        }
        return Triple(accel, gyro, mag)
    }

    fun stop() {
        motionManager.stopDeviceMotionUpdates()
        lastTimestamp = 0.0
        fusion.reset()
        callback = null
    }
}
