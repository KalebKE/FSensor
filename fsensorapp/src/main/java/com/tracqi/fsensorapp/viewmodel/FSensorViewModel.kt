package com.tracqi.fsensorapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;

import com.tracqi.fsensor.filter.LowPassFilter;
import com.tracqi.fsensor.filter.MeanFilter;
import com.tracqi.fsensor.filter.MedianFilter;
import com.tracqi.fsensor.filter.SensorFilter;
import com.tracqi.fsensor.sensor.FSensor;
import com.tracqi.fsensor.sensor.FSensorEvent;
import com.tracqi.fsensor.sensor.FSensorEventListener;
import com.tracqi.fsensor.sensor.acceleration.ComplementaryLinearAccelerationFSensor;
import com.tracqi.fsensor.sensor.acceleration.KalmanLinearAccelerationFSensor;
import com.tracqi.fsensor.sensor.acceleration.LowPassLinearAccelerationFSensor;
import com.tracqi.fsensor.sensor.orientation.ComplementaryOrientationFSensor;
import com.tracqi.fsensor.sensor.orientation.KalmanOrientationFSensor;
import com.tracqi.fsensor.sensor.orientation.LowPassOrientationFSensor;
import com.tracqi.fsensorapp.preference.Preferences;

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.AndroidViewModel;

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

/**
 * Created by kaleb on 7/7/17.
 */
public class FSensorViewModel extends AndroidViewModel {

    private FSensor linearAccelerationFSensor;
    private SensorFilter linearAccelerationFilter;
    private FSensor rotationFSensor;
    private SensorFilter rotationFilter;

    private final Map<FSensorEventListener, FSensorEventListener> sensorListenerWrapperMap = new HashMap<>();

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        initFSensors();
        initFilters();
    };

    public FSensorViewModel(Application application) {
        super(application);

        initFSensors();
        initFilters();

        Preferences.registerPreferenceChangeListener(application, preferenceChangeListener);
    }

    @Override
    protected void onCleared() {
        Preferences.unregisterPreferenceChangeListener(getApplication(), preferenceChangeListener);
        super.onCleared();
    }

    public void registerLinearAccelerationSensorListener(FSensorEventListener sensorEventListener) {
        if(linearAccelerationFSensor == null) {
            return;
        }

        FSensorEventListener wrapper = fSensorEvent -> {
            if (linearAccelerationFilter != null) {
                sensorEventListener.onSensorChanged(new FSensorEvent(fSensorEvent.sensor(), fSensorEvent.accuracy(), fSensorEvent.timestamp(), linearAccelerationFilter.filter(fSensorEvent.values())));
            } else {
                sensorEventListener.onSensorChanged(fSensorEvent);
            }
        };

        sensorListenerWrapperMap.put(sensorEventListener, wrapper);

        int sensorDelay = Preferences.getSensorFrequencyPrefs(getApplication());
        linearAccelerationFSensor.registerListener(wrapper, sensorDelay);
    }

    public void unregisterLinearAccelerationSensorListener(FSensorEventListener sensorEventListener) {
        if(linearAccelerationFSensor == null) {
            return;
        }

        FSensorEventListener wrapper = sensorListenerWrapperMap.remove(sensorEventListener);
        linearAccelerationFSensor.unregisterListener(wrapper);
    }

    public void registerRotationSensorListener(FSensorEventListener sensorEventListener) {
        if(rotationFSensor == null) {
            return;
        }

        FSensorEventListener wrapper = fSensorEvent -> {
            if (rotationFilter != null) {
                sensorEventListener.onSensorChanged(new FSensorEvent(fSensorEvent.sensor(), fSensorEvent.accuracy(), fSensorEvent.timestamp(), rotationFilter.filter(fSensorEvent.values())));
            } else {
                sensorEventListener.onSensorChanged(fSensorEvent);
            }
        };

        sensorListenerWrapperMap.put(sensorEventListener, wrapper);

        int sensorDelay = Preferences.getSensorFrequencyPrefs(getApplication());
        rotationFSensor.registerListener(wrapper, sensorDelay);
    }

    public void unregisterRotationSensorListener(FSensorEventListener sensorEventListener) {
        if(rotationFSensor == null) {
            return;
        }

        FSensorEventListener wrapper = sensorListenerWrapperMap.remove(sensorEventListener);
        rotationFSensor.unregisterListener(wrapper);
    }

    private void initFSensors() {
        SensorManager sensorManager = (SensorManager) getApplication().getSystemService(Context.SENSOR_SERVICE);

        if(Preferences.getPrefFSensorLpfLinearAccelerationEnabled(getApplication())){
            linearAccelerationFSensor = new LowPassLinearAccelerationFSensor(sensorManager);
            rotationFSensor = new LowPassOrientationFSensor(sensorManager);
        } else if(Preferences.getPrefFSensorComplimentaryLinearAccelerationEnabled(getApplication())) {
            float timeConstant = Preferences.getPrefFSensorComplimentaryLinearAccelerationTimeConstant(getApplication());
            linearAccelerationFSensor = new ComplementaryLinearAccelerationFSensor(sensorManager, timeConstant);
            rotationFSensor = new ComplementaryOrientationFSensor(sensorManager);
        } else if(Preferences.getPrefFSensorKalmanLinearAccelerationEnabled(getApplication())) {
            linearAccelerationFSensor =  new KalmanLinearAccelerationFSensor(sensorManager);
            rotationFSensor = new KalmanOrientationFSensor(sensorManager);
        } else {
            linearAccelerationFSensor = null;
            rotationFSensor = null;
        }
    }

    private void initFilters() {
        if (Preferences.getPrefLpfSmoothingEnabled(getApplication())) {
            float timeConstant = Preferences.getPrefLpfSmoothingTimeConstant(getApplication());
            linearAccelerationFilter = new LowPassFilter();
            linearAccelerationFilter.setTimeConstant(timeConstant);
            rotationFilter = new LowPassFilter();
            rotationFilter.setTimeConstant(timeConstant);
        } else if (Preferences.getPrefMeanFilterSmoothingEnabled(getApplication())) {
            float timeConstant = Preferences.getPrefMeanFilterSmoothingTimeConstant(getApplication());
            linearAccelerationFilter = new MeanFilter();
            linearAccelerationFilter.setTimeConstant(timeConstant);
            rotationFilter = new MeanFilter();
            rotationFilter.setTimeConstant(timeConstant);
        } else if (Preferences.getPrefMedianFilterSmoothingEnabled(getApplication())) {
            float timeConstant = Preferences.getPrefMedianFilterSmoothingTimeConstant(getApplication());
            linearAccelerationFilter = new MedianFilter();
            linearAccelerationFilter.setTimeConstant(timeConstant);
            rotationFilter = new MedianFilter();
            rotationFilter.setTimeConstant(timeConstant);
        } else {
            linearAccelerationFilter = null;
            rotationFilter = null;
        }
    }
}
