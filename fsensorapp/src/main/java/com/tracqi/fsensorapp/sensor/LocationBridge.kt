package com.tracqi.fsensorapp.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.tracqi.fsensor.math.coordinate.CoordinateConversion

class LocationBridge(context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var ltpLatDeg = 0.0
    private var ltpLonDeg = 0.0
    private var ltpAltM = 0.0
    var hasLtpOrigin = false
        private set

    var onLocation: ((
        enuEast: Double, enuNorth: Double, speedMps: Double,
        accuracyM: Float, bearingDeg: Float, hasBearing: Boolean,
        timestampNanos: Long
    ) -> Unit)? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val lat = location.latitude
            val lon = location.longitude
            val alt = location.altitude
            val speed = location.speed.toDouble()
            val bearing = location.bearing
            val hasBearing = location.hasBearing()
            val accuracy = location.accuracy
            val ts = location.elapsedRealtimeNanos

            if (!hasLtpOrigin) {
                ltpLatDeg = lat
                ltpLonDeg = lon
                ltpAltM = alt
                hasLtpOrigin = true
            }

            val enu = CoordinateConversion.llaToEnu(
                lat, lon, alt,
                ltpLatDeg, ltpLonDeg, ltpAltM
            )
            onLocation?.invoke(enu[0], enu[1], speed, accuracy, bearing, hasBearing, ts)
        }

        @Deprecated("Required for older API levels")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun start(intervalMs: Long = 1000L) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            intervalMs,
            0f,
            locationListener,
            Looper.getMainLooper()
        )
    }

    fun stop() {
        locationManager.removeUpdates(locationListener)
        hasLtpOrigin = false
    }
}
