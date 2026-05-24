package com.tracqi.fsensor.fusion.madgwick

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class MadgwickFusionTest {
    private val G = 9.81f

    @Test
    fun update_stationary_convergesFromIdentity() {
        val fusion = MadgwickFusion(0.1f)
        val accel = floatArrayOf(0f, 0f, G)
        val mag = floatArrayOf(20f, 0f, 40f)
        val gyro = floatArrayOf(0f, 0f, 0f)
        repeat(500) { fusion.update(accel, mag, gyro, 0.01f) }
        val q = fusion.getQuaternion()
        val norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
        assertEquals(1.0, norm, 1e-6)
        assertTrue(abs(q[0]) > 0.9, "q0 should be close to 1")
    }

    @Test
    fun update_constantRotationZ_tracksGyro() {
        val fusion = MadgwickFusion(0.033f)
        val accel = floatArrayOf(0f, 0f, G)
        val mag = floatArrayOf(20f, 0f, 40f)
        val gyro = floatArrayOf(0f, 0f, 1.0f)
        repeat(100) { fusion.update(accel, mag, gyro, 0.01f) }
        val q = fusion.getQuaternion()
        assertTrue(abs(q[3]) > 0.1, "Z rotation should be tracked")
    }

    @Test
    fun update_6dofMode_worksWithoutMagnetometer() {
        val fusion = MadgwickFusion(0.1f)
        val accel = floatArrayOf(0f, 0f, G)
        val gyro = floatArrayOf(0f, 0f, 0f)
        repeat(100) { fusion.update(accel, null, gyro, 0.01f) }
        val q = fusion.getQuaternion()
        val norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
        assertEquals(1.0, norm, 1e-6)
    }

    @Test
    fun reset_returnsToIdentity() {
        val fusion = MadgwickFusion(0.1f)
        fusion.update(floatArrayOf(G, 0f, 0f), null, floatArrayOf(1f, 1f, 1f), 0.1f)
        fusion.reset()
        val q = fusion.getQuaternion()
        assertEquals(1.0, q[0], 1e-10)
        assertEquals(0.0, q[1], 1e-10)
        assertEquals(0.0, q[2], 1e-10)
        assertEquals(0.0, q[3], 1e-10)
    }

    @Test
    fun update_9dofVs6dof_pitchRollShouldAgree() {
        // Both filters start from identity, converge to the same tilted state using 6-DOF.
        // Then fusion9 switches to 9-DOF (mag enabled). Adding magnetometer should only
        // affect heading — pitch/roll should stay consistent with the 6-DOF filter.
        val fusion6 = MadgwickFusion(0.1f)
        val fusion9 = MadgwickFusion(0.1f)
        val tiltedAccel = floatArrayOf(3f, 0f, sqrt(G * G - 9f))
        val mag = floatArrayOf(20f, 5f, 42f)
        val gyro = floatArrayOf(0f, 0f, 0f)

        repeat(500) {
            fusion6.update(tiltedAccel, null, gyro, 0.01f)
            fusion9.update(tiltedAccel, null, gyro, 0.01f)
        }

        repeat(1000) {
            fusion6.update(tiltedAccel, null, gyro, 0.01f)
            fusion9.update(tiltedAccel, mag, gyro, 0.01f)
        }

        val orient6 = fusion6.getOrientation()
        val orient9 = fusion9.getOrientation()
        val pitch6 = Math.toDegrees(orient6[1].toDouble())
        val roll6 = Math.toDegrees(orient6[2].toDouble())
        val pitch9 = Math.toDegrees(orient9[1].toDouble())
        val roll9 = Math.toDegrees(orient9[2].toDouble())

        assertTrue(abs(pitch9 - pitch6) < 5.0,
            "9-DOF pitch ($pitch9) should match 6-DOF ($pitch6)")
        assertTrue(abs(roll9 - roll6) < 5.0,
            "9-DOF roll ($roll9) should match 6-DOF ($roll6)")
    }

    @Test
    fun update_higherBeta_fasterConvergence() {
        val slow = MadgwickFusion(0.01f)
        val fast = MadgwickFusion(0.5f)
        val accel = floatArrayOf(0f, G, 0f)
        val mag = floatArrayOf(20f, 0f, 40f)
        val gyro = floatArrayOf(0f, 0f, 0f)
        repeat(50) {
            slow.update(accel, mag, gyro, 0.01f)
            fast.update(accel, mag, gyro, 0.01f)
        }
        val distSlow = abs(1.0 - slow.getQuaternion()[0])
        val distFast = abs(1.0 - fast.getQuaternion()[0])
        assertTrue(distFast > distSlow, "Higher beta should converge faster")
    }
}
