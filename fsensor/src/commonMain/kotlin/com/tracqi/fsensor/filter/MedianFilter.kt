package com.tracqi.fsensor.filter

import kotlin.math.ceil

class MedianFilter(timeConstant: Float = DEFAULT_TIME_CONSTANT) : SensorFilter(timeConstant) {
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
            val median = getMedian(values)
            median.copyInto(output)
        } else {
            data.copyInto(output)
        }
        return output
    }

    private fun getMedian(data: ArrayDeque<FloatArray>): FloatArray {
        val numAxes = data.first().size
        val result = FloatArray(numAxes)
        val buffer = DoubleArray(data.size)
        for (axis in 0 until numAxes) {
            var idx = 0
            for (sample in data) {
                buffer[idx++] = sample[axis].toDouble()
            }
            buffer.sort()
            val mid = buffer.size / 2
            result[axis] = if (buffer.size % 2 == 0) {
                ((buffer[mid - 1] + buffer[mid]) / 2.0).toFloat()
            } else {
                buffer[mid].toFloat()
            }
        }
        return result
    }
}
