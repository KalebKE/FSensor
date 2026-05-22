package com.tracqi.fsensorapp.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.tracqi.fsensor.filter.LowPassFilter
import com.tracqi.fsensor.filter.MeanFilter
import com.tracqi.fsensor.filter.MedianFilter
import com.tracqi.fsensor.filter.SensorFilter
import com.tracqi.fsensor.fusion.complementary.ComplementaryFusion
import com.tracqi.fsensor.fusion.kalman.KalmanFusion
import com.tracqi.fsensor.fusion.madgwick.MadgwickFusion
import com.tracqi.fsensor.platform.FSensor
import com.tracqi.fsensor.platform.FSensorEvent
import com.tracqi.fsensor.platform.FSensorEventListener
import com.tracqi.fsensor.platform.FusedLinearAccelerationFSensor
import com.tracqi.fsensor.platform.FusedOrientationFSensor
import com.tracqi.fsensorapp.preference.Preferences

class FSensorViewModel(application: Application?) : AndroidViewModel(application!!) {
    private var linearAccelerationFSensor: FSensor? = null
    private var linearAccelerationFilter: SensorFilter? = null
    private var rotationFSensor: FSensor? = null
    private var rotationFilter: SensorFilter? = null

    private val sensorListenerWrapperMap: MutableMap<FSensorEventListener, FSensorEventListener> = HashMap()

    private val preferenceChangeListener = OnSharedPreferenceChangeListener { _: SharedPreferences?, _: String? ->
        initFSensors()
        initFilters()
    }

    init {
        initFSensors()
        initFilters()
        Preferences.registerPreferenceChangeListener(application!!, preferenceChangeListener)
    }

    override fun onCleared() {
        Preferences.unregisterPreferenceChangeListener(getApplication(), preferenceChangeListener)
        super.onCleared()
    }

    fun registerLinearAccelerationSensorListener(sensorEventListener: FSensorEventListener) {
        val sensor = linearAccelerationFSensor ?: return
        val wrapper = FSensorEventListener { event: FSensorEvent ->
            if (linearAccelerationFilter != null) {
                sensorEventListener.onSensorChanged(FSensorEvent(event.sensorType, event.accuracy, event.timestamp, linearAccelerationFilter!!.filter(event.values)))
            } else {
                sensorEventListener.onSensorChanged(event)
            }
        }
        sensorListenerWrapperMap[sensorEventListener] = wrapper
        val sensorDelay = Preferences.getSensorFrequencyPrefs(getApplication())
        sensor.registerListener(wrapper, sensorDelay)
    }

    fun unregisterLinearAccelerationSensorListener(sensorEventListener: FSensorEventListener) {
        val sensor = linearAccelerationFSensor ?: return
        val wrapper = sensorListenerWrapperMap.remove(sensorEventListener) ?: return
        sensor.unregisterListener(wrapper)
    }

    fun registerRotationSensorListener(sensorEventListener: FSensorEventListener) {
        val sensor = rotationFSensor ?: return
        val wrapper = FSensorEventListener { event: FSensorEvent ->
            if (rotationFilter != null) {
                sensorEventListener.onSensorChanged(FSensorEvent(event.sensorType, event.accuracy, event.timestamp, rotationFilter!!.filter(event.values)))
            } else {
                sensorEventListener.onSensorChanged(event)
            }
        }
        sensorListenerWrapperMap[sensorEventListener] = wrapper
        val sensorDelay = Preferences.getSensorFrequencyPrefs(getApplication())
        sensor.registerListener(wrapper, sensorDelay)
    }

    fun unregisterRotationSensorListener(sensorEventListener: FSensorEventListener) {
        val sensor = rotationFSensor ?: return
        val wrapper = sensorListenerWrapperMap.remove(sensorEventListener) ?: return
        sensor.unregisterListener(wrapper)
    }

    private fun initFSensors() {
        val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (Preferences.getPrefFSensorLpfLinearAccelerationEnabled(getApplication())) {
            val fusion = MadgwickFusion(0.033f)
            linearAccelerationFSensor = FusedLinearAccelerationFSensor(sensorManager, fusion)
            rotationFSensor = FusedOrientationFSensor(sensorManager, MadgwickFusion(0.033f))
        } else if (Preferences.getPrefFSensorComplimentaryLinearAccelerationEnabled(getApplication())) {
            val fusion = ComplementaryFusion()
            linearAccelerationFSensor = FusedLinearAccelerationFSensor(sensorManager, fusion)
            rotationFSensor = FusedOrientationFSensor(sensorManager, ComplementaryFusion())
        } else if (Preferences.getPrefFSensorKalmanLinearAccelerationEnabled(getApplication())) {
            val fusion = KalmanFusion()
            linearAccelerationFSensor = FusedLinearAccelerationFSensor(sensorManager, fusion)
            rotationFSensor = FusedOrientationFSensor(sensorManager, KalmanFusion())
        } else {
            linearAccelerationFSensor = null
            rotationFSensor = null
        }
    }

    private fun initFilters() {
        if (Preferences.getPrefLpfSmoothingEnabled(getApplication())) {
            val tc = Preferences.getPrefLpfSmoothingTimeConstant(getApplication())
            linearAccelerationFilter = LowPassFilter(tc)
            rotationFilter = LowPassFilter(tc)
        } else if (Preferences.getPrefMeanFilterSmoothingEnabled(getApplication())) {
            val tc = Preferences.getPrefMeanFilterSmoothingTimeConstant(getApplication())
            linearAccelerationFilter = MeanFilter(tc)
            rotationFilter = MeanFilter(tc)
        } else if (Preferences.getPrefMedianFilterSmoothingEnabled(getApplication())) {
            val tc = Preferences.getPrefMedianFilterSmoothingTimeConstant(getApplication())
            linearAccelerationFilter = MedianFilter(tc)
            rotationFilter = MedianFilter(tc)
        } else {
            linearAccelerationFilter = null
            rotationFilter = null
        }
    }
}
