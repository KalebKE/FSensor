package com.tracqi.fsensor.fusion

interface FusionAlgorithm {
    fun update(acceleration: FloatArray, magnetic: FloatArray?, gyroscope: FloatArray, dt: Float)
    fun getOrientation(): FloatArray
    fun getQuaternion(): DoubleArray
    fun reset()
}
