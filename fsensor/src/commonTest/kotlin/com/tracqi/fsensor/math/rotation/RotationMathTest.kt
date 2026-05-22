package com.tracqi.fsensor.math.rotation

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class RotationMathTest {

    @Test
    fun quaternionFromMatrix_identity() {
        val identity = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
        val q = RotationMath.quaternionFromMatrix(identity)
        assertEquals(1.0, q.w, 1e-6)
        assertEquals(0.0, q.x, 1e-6)
        assertEquals(0.0, q.y, 1e-6)
        assertEquals(0.0, q.z, 1e-6)
    }

    @Test
    fun quaternionFromMatrix_90degAroundZ() {
        val rot90z = floatArrayOf(0f, -1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        val q = RotationMath.quaternionFromMatrix(rot90z)
        val norm = q.norm
        assertEquals(1.0, norm, 1e-6)
        assertTrue(abs(q.z) > 0.3, "Should have Z component")
    }

    @Test
    fun quaternionFromMatrix_180deg() {
        val rot180z = floatArrayOf(-1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f)
        val q = RotationMath.quaternionFromMatrix(rot180z)
        assertTrue(abs(q.z) > 0.9, "Should have strong Z component for 180 deg rotation")
    }

    @Test
    fun integrateGyroscope_zeroRate_identity() {
        val q = RotationMath.integrateGyroscope(Quaternion.IDENTITY, floatArrayOf(0f, 0f, 0f), 0.01f)
        assertEquals(1.0, q.w, 1e-6)
        assertEquals(0.0, q.x, 1e-6)
    }

    @Test
    fun integrateGyroscope_constantZ_rotates() {
        var q = Quaternion.IDENTITY
        repeat(100) { q = RotationMath.integrateGyroscope(q, floatArrayOf(0f, 0f, 1f), 0.01f) }
        assertTrue(q.w < 0.95, "Should have rotated")
        assertTrue(abs(q.z) > 0.1, "Should have Z component")
    }

    @Test
    fun getRotationMatrix_validInput_returns() {
        val accel = floatArrayOf(0f, 0f, 9.81f)
        val mag = floatArrayOf(20f, 0f, 40f)
        val rm = RotationMath.getRotationMatrix(accel, mag)
        assertTrue(rm != null, "Should return rotation matrix")
        assertEquals(9, rm!!.size)
    }

    @Test
    fun getRotationMatrix_freeFall_returnsNull() {
        val accel = floatArrayOf(0f, 0f, 0f)
        val mag = floatArrayOf(20f, 0f, 40f)
        val rm = RotationMath.getRotationMatrix(accel, mag)
        assertTrue(rm == null, "Should return null for free-fall")
    }
}
