package com.tracqi.fsensorapp.model

class ChartData(private val capacity: Int = 250) {
    private val x = FloatArray(capacity)
    private val y = FloatArray(capacity)
    private val z = FloatArray(capacity)
    private val timestamps = FloatArray(capacity)
    private var writeIndex = 0
    var count = 0
        private set

    fun append(xVal: Float, yVal: Float, zVal: Float, timeSeconds: Float) {
        x[writeIndex] = xVal
        y[writeIndex] = yVal
        z[writeIndex] = zVal
        timestamps[writeIndex] = timeSeconds
        writeIndex = (writeIndex + 1) % capacity
        if (count < capacity) count++
    }

    fun getSample(index: Int): Sample {
        val actualIndex = if (count < capacity) {
            index
        } else {
            (writeIndex + index) % capacity
        }
        return Sample(timestamps[actualIndex], x[actualIndex], y[actualIndex], z[actualIndex])
    }

    fun copy(): ChartData {
        val copy = ChartData(capacity)
        x.copyInto(copy.x)
        y.copyInto(copy.y)
        z.copyInto(copy.z)
        timestamps.copyInto(copy.timestamps)
        copy.writeIndex = writeIndex
        copy.count = count
        return copy
    }

    fun clear() {
        writeIndex = 0
        count = 0
    }

    data class Sample(val time: Float, val x: Float, val y: Float, val z: Float)
}
