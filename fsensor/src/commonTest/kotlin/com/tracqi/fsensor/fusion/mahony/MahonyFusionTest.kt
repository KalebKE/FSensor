package com.tracqi.fsensor.fusion.mahony

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class MahonyFusionTest {
    private val G = 9.81f

    @Test
    fun update_stationary_convergesFromIdentity() {
        val fusion = MahonyFusion(2.0f, 0.1f)
        val accel = floatArrayOf(0f, 0f, G)
        val mag = floatArrayOf(20f, 0f, 40f)
        val gyro = floatArrayOf(0f, 0f, 0f)
        repeat(500) { fusion.update(accel, mag, gyro, 0.01f) }
        val q = fusion.getQuaternion()
        val norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
        assertEquals(1.0, norm, 1e-6)
        assertTrue(abs(q[0]) > 0.9, "Should converge near identity")
    }

    @Test
    fun update_integralTerm_compensatesGyroBias() {
        val withKi = MahonyFusion(1.0f, 0.5f)
        val noKi = MahonyFusion(1.0f, 0.0f)
        val accel = floatArrayOf(0f, 0f, G)
        val mag = floatArrayOf(20f, 0f, 40f)
        val biasedGyro = floatArrayOf(0.1f, 0.05f, 0.0f)
        repeat(2000) {
            withKi.update(accel, mag, biasedGyro, 0.01f)
            noKi.update(accel, mag, biasedGyro, 0.01f)
        }
        val qWithKi = withKi.getQuaternion()
        val qNoKi = noKi.getQuaternion()
        val distWithKi = sqrt((1 - qWithKi[0]) * (1 - qWithKi[0]) + qWithKi[1] * qWithKi[1] + qWithKi[2] * qWithKi[2] + qWithKi[3] * qWithKi[3])
        val distNoKi = sqrt((1 - qNoKi[0]) * (1 - qNoKi[0]) + qNoKi[1] * qNoKi[1] + qNoKi[2] * qNoKi[2] + qNoKi[3] * qNoKi[3])
        assertTrue(distWithKi < distNoKi, "Ki should help compensate gyro bias")
    }

    @Test
    fun update_6dofMode_worksWithoutMag() {
        val fusion = MahonyFusion(2.0f, 0.0f)
        val accel = floatArrayOf(0f, 0f, G)
        val gyro = floatArrayOf(0f, 0f, 0f)
        repeat(100) { fusion.update(accel, null, gyro, 0.01f) }
        val q = fusion.getQuaternion()
        val norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3])
        assertEquals(1.0, norm, 1e-6)
    }

    @Test
    fun reset_clearsState() {
        val fusion = MahonyFusion(2.0f, 0.5f)
        fusion.update(floatArrayOf(G, 0f, 0f), null, floatArrayOf(1f, 1f, 1f), 0.1f)
        fusion.reset()
        val q = fusion.getQuaternion()
        assertEquals(1.0, q[0], 1e-10)
        assertEquals(0.0, q[1], 1e-10)
        assertEquals(0.0, q[2], 1e-10)
        assertEquals(0.0, q[3], 1e-10)
    }
}
