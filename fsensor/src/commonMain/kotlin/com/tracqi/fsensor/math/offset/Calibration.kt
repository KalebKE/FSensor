package com.tracqi.fsensor.math.offset

data class Calibration(val scalar: Array<DoubleArray>, val offset: DoubleArray) {
    fun calibrate(vector: FloatArray): FloatArray {
        val x = vector[0].toDouble() - offset[0]
        val y = vector[1].toDouble() - offset[1]
        val z = vector[2].toDouble() - offset[2]
        return floatArrayOf(
            (scalar[0][0] * x + scalar[0][1] * y + scalar[0][2] * z).toFloat(),
            (scalar[1][0] * x + scalar[1][1] * y + scalar[1][2] * z).toFloat(),
            (scalar[2][0] * x + scalar[2][1] * y + scalar[2][2] * z).toFloat()
        )
    }
}
