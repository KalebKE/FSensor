package com.tracqi.fsensor.filter

abstract class SensorFilter(var timeConstant: Float = DEFAULT_TIME_CONSTANT) {
    protected var startTime: Long = 0L
    protected var count: Int = 0
    protected val output: FloatArray = FloatArray(3)

    abstract fun filter(data: FloatArray): FloatArray

    companion object {
        const val DEFAULT_TIME_CONSTANT: Float = 0.18f
    }
}
