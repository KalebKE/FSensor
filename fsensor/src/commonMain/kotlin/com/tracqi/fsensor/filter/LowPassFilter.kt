package com.tracqi.fsensor.filter

class LowPassFilter(timeConstant: Float = DEFAULT_TIME_CONSTANT) : SensorFilter(timeConstant) {

    override fun filter(data: FloatArray): FloatArray {
        if (startTime == 0L) {
            startTime = currentTimeNanos()
        }

        val dt = 1.0f / (count++ / ((currentTimeNanos() - startTime) / 1_000_000_000.0f))
        val alpha = timeConstant / (timeConstant + dt)
        val oneMinusAlpha = 1.0f - alpha

        output[0] = alpha * output[0] + oneMinusAlpha * data[0]
        output[1] = alpha * output[1] + oneMinusAlpha * data[1]
        output[2] = alpha * output[2] + oneMinusAlpha * data[2]

        return output
    }
}
