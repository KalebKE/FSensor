package com.tracqi.fsensor.filter.gps

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class GpsKalmanFilterTest {

    @Test
    fun predict_constantSpeedNorth_positionAdvances() {
        val filter = GpsKalmanFilter()
        // Warm up: correct seeds speed, first predict decomposes speed→velocity,
        // second predict uses populated vy to advance position.
        repeat(3) {
            filter.correct(0.0, 0.0, 10.0, 0.0, true)
            filter.predict(0.0, 1.0)
        }
        val state = filter.getState()
        assertTrue(state.north > 5.0, "North position should advance with northward speed, was ${state.north}")
        assertTrue(abs(state.east) < 1.0, "East should stay near zero, was ${state.east}")
    }

    @Test
    fun predict_constantSpeedEast_positionAdvancesEast() {
        val filter = GpsKalmanFilter()
        repeat(3) {
            filter.correct(0.0, 0.0, 10.0, 0.0, true)
            filter.predict(PI / 2, 1.0)
        }
        val state = filter.getState()
        assertTrue(state.east > 5.0, "East position should advance with eastward speed, was ${state.east}")
        assertTrue(abs(state.north) < 1.0, "North should stay near zero, was ${state.north}")
    }

    @Test
    fun correct_gpsPosition_pullsStateTowardMeasurement() {
        val filter = GpsKalmanFilter()
        // Multiple predict/correct cycles let P grow and Kalman gain increase
        repeat(20) {
            filter.predict(0.0, 0.1)
            filter.correct(100.0, 50.0, 0.0, 0.0, true)
        }
        val state = filter.getState()
        assertTrue(state.east > 50.0, "Position should move toward GPS east, was ${state.east}")
        assertTrue(state.north > 25.0, "Position should move toward GPS north, was ${state.north}")
    }

    @Test
    fun correct_noGpsFix_positionStable() {
        val filter = GpsKalmanFilter()
        // Converge to a known position
        repeat(50) {
            filter.predict(0.0, 0.05)
            filter.correct(100.0, 100.0, 0.0, 0.0, true)
        }
        val converged = filter.getState()
        // Now correct with bogus position but no GPS fix — gated R should ignore it
        filter.correct(0.0, 0.0, 0.0, 0.0, false)
        val afterNoFix = filter.getState()
        assertTrue(
            abs(afterNoFix.east - converged.east) < 1.0,
            "East should barely change without GPS fix: before=${converged.east}, after=${afterNoFix.east}"
        )
        assertTrue(
            abs(afterNoFix.north - converged.north) < 1.0,
            "North should barely change without GPS fix: before=${converged.north}, after=${afterNoFix.north}"
        )
    }

    @Test
    fun correct_repeatedGps_convergesOnMeasurement() {
        val filter = GpsKalmanFilter()
        repeat(100) {
            filter.predict(0.0, 0.05)
            filter.correct(100.0, 200.0, 0.0, 0.0, true)
        }
        val state = filter.getState()
        assertTrue(abs(state.east - 100.0) < 2.0, "East should converge to 100, was ${state.east}")
        assertTrue(abs(state.north - 200.0) < 2.0, "North should converge to 200, was ${state.north}")
    }

    @Test
    fun reset_returnsToInitialState() {
        val filter = GpsKalmanFilter()
        repeat(10) {
            filter.predict(0.5, 0.1)
            filter.correct(100.0, 200.0, 10.0, 1.0, true)
        }
        filter.reset()
        val state = filter.getState()
        assertEquals(0.0, state.east, 1e-10)
        assertEquals(0.0, state.north, 1e-10)
        assertEquals(0.0, state.speed, 1e-10)
    }

    @Test
    fun predict_withAcceleration_speedIncreases() {
        val filter = GpsKalmanFilter()
        filter.correct(0.0, 0.0, 10.0, 2.0, true)
        filter.predict(0.0, 1.0)
        val state = filter.getState()
        assertTrue(state.speed > 11.0, "Speed should increase with positive acceleration, was ${state.speed}")
    }

    @Test
    fun predict_headingDecomposition_velocityComponents() {
        val filter = GpsKalmanFilter()
        val heading = PI / 4
        filter.correct(0.0, 0.0, 10.0, 0.0, true)
        filter.predict(heading, 0.05)
        val state = filter.getState()
        val ratio = if (state.velocityNorth != 0.0) state.velocityEast / state.velocityNorth else 0.0
        assertTrue(abs(ratio - 1.0) < 0.1, "NE heading should give equal vx/vy ratio, was $ratio")
    }
}
