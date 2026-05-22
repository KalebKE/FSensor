package com.tracqi.fsensor.math.gravity

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class GravityTest {

    @Test
    fun getGravityFromOrientation_flat() {
        val orientation = floatArrayOf(0f, 0f, 0f)
        val g = Gravity.getGravityFromOrientation(orientation)
        assertEquals(0f, g[0], 0.01f)
        assertEquals(0f, g[1], 0.01f)
        assertEquals(Gravity.EARTH, g[2], 0.01f)
    }

    @Test
    fun getOrientationFromGravity_flat() {
        val gravity = floatArrayOf(0f, 0f, Gravity.EARTH)
        val o = Gravity.getOrientationFromGravity(gravity)
        assertEquals(0f, o[1], 0.01f)
        assertEquals(0f, o[2], 0.01f)
    }

    @Test
    fun getOrientationFromGravity_gravityZ_zero() {
        val gravity = floatArrayOf(0f, -Gravity.EARTH, 0f)
        val o = Gravity.getOrientationFromGravity(gravity)
        assertTrue(abs(o[1]) > 1.0f, "Should detect tilt")
    }
}
