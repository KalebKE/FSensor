package com.tracqi.fsensorapp.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.tracqi.fsensor.filter.SensorFilter
import com.tracqi.fsensor.platform.FSensor
import com.tracqi.fsensor.platform.FSensorEvent
import com.tracqi.fsensor.platform.FSensorEventListener
import com.tracqi.fsensor.platform.FusedLinearAccelerationFSensor
import com.tracqi.fsensor.platform.FusedOrientationFSensor
import com.tracqi.fsensorapp.model.ChartData
import com.tracqi.fsensorapp.model.SensorConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var orientationSensor: FSensor? = null
    private var accelerationSensor: FSensor? = null
    private var orientationFilter: SensorFilter? = null
    private var accelerationFilter: SensorFilter? = null

    private val _config = MutableStateFlow(SensorConfig())
    val config: StateFlow<SensorConfig> = _config.asStateFlow()

    private val _orientation = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val orientation: StateFlow<FloatArray> = _orientation.asStateFlow()

    private val _acceleration = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val acceleration: StateFlow<FloatArray> = _acceleration.asStateFlow()

    private val orientationBuffer = ChartData(250)
    private val accelerationBuffer = ChartData(250)
    private var startTimeNanos = 0L

    private val _orientationHistory = MutableStateFlow(ChartData(250))
    val orientationHistory: StateFlow<ChartData> = _orientationHistory.asStateFlow()

    private val _accelerationHistory = MutableStateFlow(ChartData(250))
    val accelerationHistory: StateFlow<ChartData> = _accelerationHistory.asStateFlow()

    private var orientationListener: FSensorEventListener? = null
    private var accelerationListener: FSensorEventListener? = null

    private var isRunning = false

    fun updateConfig(newConfig: SensorConfig) {
        val wasRunning = isRunning
        if (wasRunning) stop()
        _config.value = newConfig
        rebuildSensors()
        if (wasRunning) start()
    }

    fun start() {
        if (isRunning) return
        if (orientationSensor == null) rebuildSensors()
        isRunning = true
        startTimeNanos = System.nanoTime()
        orientationBuffer.clear()
        accelerationBuffer.clear()

        val delay = _config.value.sensorRate.delay

        orientationListener = FSensorEventListener { event: FSensorEvent ->
            val values = orientationFilter?.filter(event.values) ?: event.values
            _orientation.value = values
            val t = (event.timestamp - startTimeNanos) / 1_000_000_000f
            synchronized(orientationBuffer) {
                orientationBuffer.append(
                    Math.toDegrees(values[0].toDouble()).toFloat(),
                    Math.toDegrees(values[1].toDouble()).toFloat(),
                    Math.toDegrees(values[2].toDouble()).toFloat(),
                    t
                )
                _orientationHistory.value = orientationBuffer.copy()
            }
        }
        orientationSensor?.registerListener(orientationListener!!, delay)

        accelerationListener = FSensorEventListener { event: FSensorEvent ->
            val values = accelerationFilter?.filter(event.values) ?: event.values
            _acceleration.value = values
            val t = (event.timestamp - startTimeNanos) / 1_000_000_000f
            synchronized(accelerationBuffer) {
                accelerationBuffer.append(values[0], values[1], values[2], t)
                _accelerationHistory.value = accelerationBuffer.copy()
            }
        }
        accelerationSensor?.registerListener(accelerationListener!!, delay)
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        orientationListener?.let { orientationSensor?.unregisterListener(it) }
        accelerationListener?.let { accelerationSensor?.unregisterListener(it) }
        orientationListener = null
        accelerationListener = null
    }

    override fun onCleared() {
        stop()
        super.onCleared()
    }

    private fun rebuildSensors() {
        val cfg = _config.value
        val fusion = cfg.fusionType.createInstance(cfg.fusionParams)
        val fusion2 = cfg.fusionType.createInstance(cfg.fusionParams)
        orientationSensor = FusedOrientationFSensor(sensorManager, fusion)
        accelerationSensor = FusedLinearAccelerationFSensor(sensorManager, fusion2)
        orientationFilter = cfg.filterType.createInstance(cfg.filterTimeConstant)
        accelerationFilter = cfg.filterType.createInstance(cfg.filterTimeConstant)
    }
}
