package com.kircherelectronics.fsensor.sensor.acceleration;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import com.kircherelectronics.fsensor.filter.LowPassFilter;
import com.kircherelectronics.fsensor.sensor.FSensor;
import com.kircherelectronics.fsensor.sensor.FSensorEvent;
import com.kircherelectronics.fsensor.sensor.FSensorEventListener;

import java.util.ArrayList;
import java.util.List;

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

public class LowPassLinearAccelerationSensor implements FSensor {
    private static final String TAG = LowPassLinearAccelerationSensor.class.getSimpleName();

    private final SensorManager sensorManager;

    private final float[] output = new float[3];

    private final List<FSensorEventListener> fSensorEventListeners = new ArrayList<>();
    private final SensorEventListener sensorEventListener = new SensorListener();
    private final LowPassFilter lowPassFilter;

    public LowPassLinearAccelerationSensor(@NonNull SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.lowPassFilter = new LowPassFilter();
    }

    public LowPassLinearAccelerationSensor(@NonNull SensorManager sensorManager, @NonNull LowPassFilter lowPassFilter) {
        this.sensorManager = sensorManager;
        this.lowPassFilter = lowPassFilter;
    }

    @Override
    public void registerListener(FSensorEventListener sensorEventListener, int sensorDelay) {
        if(fSensorEventListeners.isEmpty()) {
            registerSensors(sensorDelay);
        }

        fSensorEventListeners.add(sensorEventListener);
    }

    @Override
    public void unregisterListener(FSensorEventListener sensorEventListener) {
        this.fSensorEventListeners.remove(sensorEventListener);
        if(fSensorEventListeners.isEmpty()) {
            unregisterSensors();
        }
    }

    private void calculateLinerAcceleration(float[] acceleration, float[] gravity) {
        // Determine the linear acceleration
        output[0] = acceleration[0] - gravity[0];
        output[1] = acceleration[1] - gravity[1];
        output[2] = acceleration[2] - gravity[2];
    }

    private void registerSensors(int sensorDelay) {
        // Register for sensor updates.
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay);
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    private class SensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                calculateLinerAcceleration(event.values, lowPassFilter.filter(event.values));

                for (FSensorEventListener fSensorEventListeners : fSensorEventListeners) {
                    fSensorEventListeners.onSensorChanged(new FSensorEvent(event.sensor, event.accuracy, event.timestamp, output));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
}
