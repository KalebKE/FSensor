package com.tracqi.fsensor.fusion.ekf

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class EkfFusionTest {
    private val G = 9.81f

    @Test
    fun predict_zeroGyro_statePersists() {
        val ekf = EkfFusion()
        ekf.predict(floatArrayOf(0f, 0f, 0f), 0.01f)
        val q = ekf.getQuaternion()
        assertEquals(1.0, q[0], 1e-6)
        assertEquals(0.0, q[1], 1e-6)
        assertEquals(0.0, q[2], 1e-6)
        assertEquals(0.0, q[3], 1e-6)
    }

    @Test
    fun predict_constantZRotation_rotatesState() {
        val ekf = EkfFusion()
        val gyroZ = floatArrayOf(0f, 0f, 1.0f)
        repeat(100) { ekf.predict(gyroZ, 0.01f) }
        val q = ekf.getQuaternion()
        assertTrue(q[0] < 0.95, "q0 should decrease from 1")
        assertTrue(q[3] > 0.1, "q3 should be positive (Z rotation)")
        val norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
        assertEquals(1.0, norm, 1e-6)
    }

    @Test
    fun correctAccelerometer_pullsTowardGravity() {
        val ekf = EkfFusion()
        ekf.predict(floatArrayOf(2.0f, 0f, 0f), 0.1f)
        val qBefore = ekf.getQuaternion().copyOf()
        ekf.correctAccelerometer(floatArrayOf(0f, 0f, G))
        val qAfter = ekf.getQuaternion()
        val distBefore = sqrt((1 - qBefore[0]) * (1 - qBefore[0]) + qBefore[1] * qBefore[1])
        val distAfter = sqrt((1 - qAfter[0]) * (1 - qAfter[0]) + qAfter[1] * qAfter[1])
        assertTrue(distAfter < distBefore, "Correction should pull state toward gravity")
    }

    @Test
    fun fullCycle_stationaryDevice_converges() {
        val ekf = EkfFusion(0.001, 0.05, 0.5)
        val accel = floatArrayOf(0f, 0f, G)
        val mag = floatArrayOf(20f, 0f, 40f)
        val gyro = floatArrayOf(0f, 0f, 0f)
        repeat(500) {
            ekf.predict(gyro, 0.01f)
            ekf.correctAccelerometer(accel)
            ekf.correctMagnetometer(mag)
        }
        val q = ekf.getQuaternion()
        val norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
        assertEquals(1.0, norm, 1e-6)
        assertTrue(abs(q[0]) > 0.85, "Should converge near identity")
    }

    @Test
    fun reset_returnsToIdentity() {
        val ekf = EkfFusion()
        ekf.predict(floatArrayOf(1f, 1f, 1f), 0.1f)
        ekf.reset()
        val q = ekf.getQuaternion()
        assertEquals(1.0, q[0], 1e-10)
        assertEquals(0.0, q[1], 1e-10)
        assertEquals(0.0, q[2], 1e-10)
        assertEquals(0.0, q[3], 1e-10)
    }

    @Test
    fun quaternionNorm_maintainedAcrossManyUpdates() {
        val ekf = EkfFusion()
        val accel = floatArrayOf(1f, 2f, G)
        val mag = floatArrayOf(15f, 5f, 35f)
        val gyro = floatArrayOf(0.3f, -0.2f, 0.5f)
        repeat(5000) { i ->
            ekf.predict(gyro, 0.01f)
            if (i % 2 == 0) ekf.correctAccelerometer(accel)
            if (i % 5 == 0) ekf.correctMagnetometer(mag)
        }
        val q = ekf.getQuaternion()
        val norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
        assertEquals(1.0, norm, 1e-5)
    }
}
