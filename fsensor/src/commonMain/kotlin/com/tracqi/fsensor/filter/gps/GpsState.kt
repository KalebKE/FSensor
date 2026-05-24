package com.tracqi.fsensor.filter.gps

data class GpsState(
    val east: Double,
    val north: Double,
    val velocityEast: Double,
    val velocityNorth: Double,
    val speed: Double,
    val longitudinalAcceleration: Double
)
