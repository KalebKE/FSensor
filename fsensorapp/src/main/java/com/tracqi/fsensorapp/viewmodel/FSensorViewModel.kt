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
import com.tracqi.fsensor.sensor.FSensor
import com.tracqi.fsensor.sensor.FSensorEvent
import com.tracqi.fsensor.sensor.FSensorEventListener
import com.tracqi.fsensor.sensor.acceleration.ComplementaryLinearAccelerationFSensor
import com.tracqi.fsensor.sensor.acceleration.KalmanLinearAccelerationFSensor
import com.tracqi.fsensor.sensor.acceleration.LowPassLinearAccelerationFSensor
import com.tracqi.fsensor.sensor.orientation.ComplementaryOrientationFSensor
import com.tracqi.fsensor.sensor.orientation.KalmanOrientationFSensor
import com.tracqi.fsensor.sensor.orientation.LowPassOrientationFSensor
import com.tracqi.fsensorapp.preference.Preferences

/*
* Copyright 2024, Tracqi Technology, LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

class FSensorViewModel(application: Application?) : AndroidViewModel(application!!) {
    private var linearAccelerationFSensor: FSensor? = null
    private var linearAccelerationFilter: SensorFilter? = null
    private var rotationFSensor: FSensor? = null
    private var rotationFilter: SensorFilter? = null

    private val sensorListenerWrapperMap: MutableMap<FSensorEventListener, FSensorEventListener> = HashMap()

    private val preferenceChangeListener = OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
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
        if (linearAccelerationFSensor == null) {
            return
        }

        val wrapper = FSensorEventListener { fSensorEvent: FSensorEvent ->
            if (linearAccelerationFilter != null) {
                sensorEventListener.onSensorChanged(FSensorEvent(fSensorEvent.sensor, fSensorEvent.accuracy, fSensorEvent.timestamp, linearAccelerationFilter!!.filter(fSensorEvent.values)))
            } else {
                sensorEventListener.onSensorChanged(fSensorEvent)
            }
        }

        sensorListenerWrapperMap[sensorEventListener] = wrapper

        val sensorDelay = Preferences.getSensorFrequencyPrefs(getApplication())
        linearAccelerationFSensor!!.registerListener(wrapper, sensorDelay)
    }

    fun unregisterLinearAccelerationSensorListener(sensorEventListener: FSensorEventListener) {
        if (linearAccelerationFSensor == null) {
            return
        }

        val wrapper = sensorListenerWrapperMap.remove(sensorEventListener)
        linearAccelerationFSensor!!.unregisterListener(wrapper)
    }

    fun registerRotationSensorListener(sensorEventListener: FSensorEventListener) {
        if (rotationFSensor == null) {
            return
        }

        val wrapper = FSensorEventListener { fSensorEvent: FSensorEvent ->
            if (rotationFilter != null) {
                sensorEventListener.onSensorChanged(FSensorEvent(fSensorEvent.sensor, fSensorEvent.accuracy, fSensorEvent.timestamp, rotationFilter!!.filter(fSensorEvent.values)))
            } else {
                sensorEventListener.onSensorChanged(fSensorEvent)
            }
        }

        sensorListenerWrapperMap[sensorEventListener] = wrapper

        val sensorDelay = Preferences.getSensorFrequencyPrefs(getApplication())
        rotationFSensor!!.registerListener(wrapper, sensorDelay)
    }

    fun unregisterRotationSensorListener(sensorEventListener: FSensorEventListener) {
        if (rotationFSensor == null) {
            return
        }

        val wrapper = sensorListenerWrapperMap.remove(sensorEventListener)
        rotationFSensor!!.unregisterListener(wrapper)
    }

    private fun initFSensors() {
        val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (Preferences.getPrefFSensorLpfLinearAccelerationEnabled(getApplication())) {
            linearAccelerationFSensor = LowPassLinearAccelerationFSensor(sensorManager)
            rotationFSensor = LowPassOrientationFSensor(sensorManager)
        } else if (Preferences.getPrefFSensorComplimentaryLinearAccelerationEnabled(getApplication())) {
            val timeConstant = Preferences.getPrefFSensorComplimentaryLinearAccelerationTimeConstant(getApplication())
            linearAccelerationFSensor = ComplementaryLinearAccelerationFSensor(sensorManager, timeConstant)
            rotationFSensor = ComplementaryOrientationFSensor(sensorManager)
        } else if (Preferences.getPrefFSensorKalmanLinearAccelerationEnabled(getApplication())) {
            linearAccelerationFSensor = KalmanLinearAccelerationFSensor(sensorManager)
            rotationFSensor = KalmanOrientationFSensor(sensorManager)
        } else {
            linearAccelerationFSensor = null
            rotationFSensor = null
        }
    }

    private fun initFilters() {
        if (Preferences.getPrefLpfSmoothingEnabled(getApplication())) {
            val timeConstant = Preferences.getPrefLpfSmoothingTimeConstant(getApplication())
            linearAccelerationFilter = LowPassFilter()
            (linearAccelerationFilter as LowPassFilter).setTimeConstant(timeConstant)
            rotationFilter = LowPassFilter()
            (rotationFilter as LowPassFilter).setTimeConstant(timeConstant)
        } else if (Preferences.getPrefMeanFilterSmoothingEnabled(getApplication())) {
            val timeConstant = Preferences.getPrefMeanFilterSmoothingTimeConstant(getApplication())
            linearAccelerationFilter = MeanFilter()
            (linearAccelerationFilter as MeanFilter).setTimeConstant(timeConstant)
            rotationFilter = MeanFilter()
            (rotationFilter as MeanFilter).setTimeConstant(timeConstant)
        } else if (Preferences.getPrefMedianFilterSmoothingEnabled(getApplication())) {
            val timeConstant = Preferences.getPrefMedianFilterSmoothingTimeConstant(getApplication())
            linearAccelerationFilter = MedianFilter()
            (linearAccelerationFilter as MedianFilter).setTimeConstant(timeConstant)
            rotationFilter = MedianFilter()
            (rotationFilter as MedianFilter).setTimeConstant(timeConstant)
        } else {
            linearAccelerationFilter = null
            rotationFilter = null
        }
    }
}
