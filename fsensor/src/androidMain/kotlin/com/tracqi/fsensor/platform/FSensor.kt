package com.tracqi.fsensor.platform

interface FSensor {
    fun registerListener(listener: FSensorEventListener, sensorDelay: Int)
    fun unregisterListener(listener: FSensorEventListener)
}

fun interface FSensorEventListener {
    fun onSensorChanged(event: FSensorEvent)
}

data class FSensorEvent(
    val sensorType: Int,
    val accuracy: Int,
    val timestamp: Long,
    val values: FloatArray
)
