package com.tracqi.fsensor.filter

import kotlin.math.ceil

class MeanFilter(timeConstant: Float = DEFAULT_TIME_CONSTANT) : SensorFilter(timeConstant) {
    private val values = ArrayDeque<FloatArray>()

    override fun filter(data: FloatArray): FloatArray {
        if (startTime == 0L) {
            startTime = currentTimeNanos()
        }

        val hz = count++ / ((currentTimeNanos() - startTime) / 1_000_000_000.0f)
        val filterWindow = ceil(hz * timeConstant).toInt()

        values.addLast(data.copyOf())
        while (values.size > filterWindow) {
            values.removeFirst()
        }

        if (values.isNotEmpty()) {
            val mean = getMean(values)
            mean.copyInto(output)
        } else {
            data.copyInto(output)
        }
        return output
    }

    private fun getMean(data: ArrayDeque<FloatArray>): FloatArray {
        val mean = FloatArray(data.first().size)
        for (axis in data) {
            for (i in axis.indices) {
                mean[i] += axis[i]
            }
        }
        for (i in mean.indices) {
            mean[i] /= data.size
        }
        return mean
    }
}
