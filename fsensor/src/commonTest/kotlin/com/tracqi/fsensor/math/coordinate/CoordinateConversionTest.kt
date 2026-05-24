package com.tracqi.fsensor.math.coordinate

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class CoordinateConversionTest {

    @Test
    fun llaToEnu_originPoint_returnsZero() {
        val lat0 = 37.7749
        val lon0 = -122.4194
        val h0 = 0.0
        val enu = CoordinateConversion.llaToEnu(lat0, lon0, h0, lat0, lon0, h0)
        assertTrue(abs(enu[0]) < 1e-6, "East should be zero at origin, was ${enu[0]}")
        assertTrue(abs(enu[1]) < 1e-6, "North should be zero at origin, was ${enu[1]}")
        assertTrue(abs(enu[2]) < 1e-6, "Up should be zero at origin, was ${enu[2]}")
    }

    @Test
    fun llaToEnu_roundTrip_preservesCoordinates() {
        val lat0 = 40.7128
        val lon0 = -74.0060
        val h0 = 10.0
        val lat = 40.7138
        val lon = -74.0050
        val h = 15.0

        val enu = CoordinateConversion.llaToEnu(lat, lon, h, lat0, lon0, h0)
        val lla = CoordinateConversion.enuToLla(enu[0], enu[1], enu[2], lat0, lon0, h0)

        assertTrue(abs(lla[0] - lat) < 1e-8, "Lat round-trip failed: ${lla[0]} vs $lat")
        assertTrue(abs(lla[1] - lon) < 1e-8, "Lon round-trip failed: ${lla[1]} vs $lon")
        assertTrue(abs(lla[2] - h) < 0.01, "Alt round-trip failed: ${lla[2]} vs $h")
    }

    @Test
    fun llaToEnu_northDisplacement_isPositiveNorth() {
        val lat0 = 45.0
        val lon0 = 0.0
        val h0 = 0.0
        val enu = CoordinateConversion.llaToEnu(45.001, 0.0, 0.0, lat0, lon0, h0)
        assertTrue(enu[1] > 100.0, "Moving north should give positive north ENU, was ${enu[1]}")
        assertTrue(abs(enu[0]) < 1.0, "East should be near zero for pure north move, was ${enu[0]}")
    }

    @Test
    fun llaToEnu_eastDisplacement_isPositiveEast() {
        val lat0 = 45.0
        val lon0 = 0.0
        val h0 = 0.0
        val enu = CoordinateConversion.llaToEnu(45.0, 0.001, 0.0, lat0, lon0, h0)
        assertTrue(enu[0] > 50.0, "Moving east should give positive east ENU, was ${enu[0]}")
        assertTrue(abs(enu[1]) < 1.0, "North should be near zero for pure east move, was ${enu[1]}")
    }
}
