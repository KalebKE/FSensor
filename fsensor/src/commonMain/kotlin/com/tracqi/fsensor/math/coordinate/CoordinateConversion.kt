package com.tracqi.fsensor.math.coordinate

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CoordinateConversion {

    private const val a = 6378137.0
    private const val b = 6356752.314245
    private const val f = (a - b) / a
    private const val e_sq = f * (2 - f)

    private fun toRadians(deg: Double): Double = deg * PI / 180.0
    private fun toDegrees(rad: Double): Double = rad * 180.0 / PI

    fun llaToEcef(lat: Double, lon: Double, h: Double): DoubleArray {
        val lambda = toRadians(lat)
        val phi = toRadians(lon)
        val sinLambda = sin(lambda)
        val cosLambda = cos(lambda)
        val cosPhi = cos(phi)
        val sinPhi = sin(phi)
        val N = a / sqrt(1 - e_sq * sinLambda * sinLambda)

        return doubleArrayOf(
            (h + N) * cosLambda * cosPhi,
            (h + N) * cosLambda * sinPhi,
            (h + (1 - e_sq) * N) * sinLambda,
        )
    }

    fun ecefToLla(x: Double, y: Double, z: Double): DoubleArray {
        val eps = e_sq / (1.0 - e_sq)
        val p = sqrt(x * x + y * y)
        val q = atan2(z * a, p * b)
        val sinQ = sin(q)
        val cosQ = cos(q)
        val sinQ3 = sinQ * sinQ * sinQ
        val cosQ3 = cosQ * cosQ * cosQ
        val phi = atan2(z + eps * b * sinQ3, p - e_sq * a * cosQ3)
        val lambda = atan2(y, x)
        val sinPhi = sin(phi)
        val v = a / sqrt(1.0 - e_sq * sinPhi * sinPhi)
        val h = (p / cos(phi)) - v

        return doubleArrayOf(toDegrees(phi), toDegrees(lambda), h)
    }

    fun ecefToEnu(
        x: Double, y: Double, z: Double,
        lat0: Double, lon0: Double, h0: Double,
    ): DoubleArray {
        val lambda = toRadians(lat0)
        val phi = toRadians(lon0)
        val sinLambda = sin(lambda)
        val cosLambda = cos(lambda)
        val cosPhi = cos(phi)
        val sinPhi = sin(phi)
        val N = a / sqrt(1 - e_sq * sinLambda * sinLambda)

        val x0 = (h0 + N) * cosLambda * cosPhi
        val y0 = (h0 + N) * cosLambda * sinPhi
        val z0 = (h0 + (1 - e_sq) * N) * sinLambda

        val xd = x - x0
        val yd = y - y0
        val zd = z - z0

        return doubleArrayOf(
            -sinPhi * xd + cosPhi * yd,
            -cosPhi * sinLambda * xd - sinLambda * sinPhi * yd + cosLambda * zd,
            cosLambda * cosPhi * xd + cosLambda * sinPhi * yd + sinLambda * zd,
        )
    }

    fun enuToEcef(
        xEast: Double, yNorth: Double, zUp: Double,
        lat0: Double, lon0: Double, h0: Double,
    ): DoubleArray {
        val lambda = toRadians(lat0)
        val phi = toRadians(lon0)
        val sinLambda = sin(lambda)
        val cosLambda = cos(lambda)
        val cosPhi = cos(phi)
        val sinPhi = sin(phi)
        val N = a / sqrt(1 - e_sq * sinLambda * sinLambda)

        val x0 = (h0 + N) * cosLambda * cosPhi
        val y0 = (h0 + N) * cosLambda * sinPhi
        val z0 = (h0 + (1 - e_sq) * N) * sinLambda

        val xd = -sinPhi * xEast - cosPhi * sinLambda * yNorth + cosLambda * cosPhi * zUp
        val yd = cosPhi * xEast - sinLambda * sinPhi * yNorth + cosLambda * sinPhi * zUp
        val zd = cosLambda * yNorth + sinLambda * zUp

        return doubleArrayOf(xd + x0, yd + y0, zd + z0)
    }

    fun llaToEnu(
        lat: Double, lon: Double, h: Double,
        lat0: Double, lon0: Double, h0: Double,
    ): DoubleArray {
        val ecef = llaToEcef(lat, lon, h)
        return ecefToEnu(ecef[0], ecef[1], ecef[2], lat0, lon0, h0)
    }

    fun enuToLla(
        xEast: Double, yNorth: Double, zUp: Double,
        lat0: Double, lon0: Double, h0: Double,
    ): DoubleArray {
        val ecef = enuToEcef(xEast, yNorth, zUp, lat0, lon0, h0)
        return ecefToLla(ecef[0], ecef[1], ecef[2])
    }
}
