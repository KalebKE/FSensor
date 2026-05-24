package com.tracqi.fsensorapp.model

data class GpsUiState(
    val gpsTrack: List<Pair<Double, Double>> = emptyList(),
    val filteredTrack: List<Pair<Double, Double>> = emptyList(),
    val speedMps: Double = 0.0,
    val eastM: Double = 0.0,
    val northM: Double = 0.0,
    val hasGpsFix: Boolean = false,
    val accuracyM: Float = 0f,
    val fixCount: Int = 0
)
