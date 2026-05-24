package com.tracqi.fsensor.platform

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.tracqi.fsensor.fusion.FusionAlgorithm
import com.tracqi.fsensor.math.angle.Angles

class FusedOrientationFSensor(
    private val sensorManager: SensorManager,
    private val fusion: FusionAlgorithm
) : FSensor {

    private val listeners = mutableListOf<FSensorEventListener>()
    private val acceleration = FloatArray(3)
    private val magnetic = FloatArray(3)
    private val gyroscope = FloatArray(3)
    private var timestamp = 0L
    private val output = FloatArray(3)

    private val sensorListener = object : SensorEventListener {
        private var hasAccel = false
        private var hasMag = false
        private var hasGyro = false

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    event.values.copyInto(acceleration)
                    hasAccel = true
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    event.values.copyInto(magnetic)
                    hasMag = true
                }
                Sensor.TYPE_GYROSCOPE -> {
                    event.values.copyInto(gyroscope)
                    hasGyro = true
                }
            }

            if (hasAccel && hasGyro) {
                val now = event.timestamp
                if (timestamp != 0L) {
                    val dt = (now - timestamp) * 1e-9f
                    fusion.update(acceleration, if (hasMag) magnetic else null, gyroscope, dt)
                    val orientation = fusion.getOrientation()
                    orientation.copyInto(output)
                    val fsEvent = FSensorEvent(event.sensor.type, event.accuracy, now, output.copyOf())
                    listeners.forEach { it.onSensorChanged(fsEvent) }
                }
                timestamp = now
                hasAccel = false
                hasGyro = false
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun registerListener(listener: FSensorEventListener, sensorDelay: Int) {
        if (listeners.isEmpty()) {
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay)
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorDelay)
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorDelay)
        }
        listeners.add(listener)
    }

    override fun unregisterListener(listener: FSensorEventListener) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            sensorManager.unregisterListener(sensorListener)
            fusion.reset()
            timestamp = 0L
        }
    }
}
