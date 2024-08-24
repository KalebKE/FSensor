package com.tracqi.fsensor.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.tracqi.fsensor.rotation.Rotation;
import com.tracqi.fsensor.sensor.acceleration.ComplementaryLinearAccelerationFSensor;

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

public abstract class BaseFSensor implements FSensor {
    private static final String TAG = ComplementaryLinearAccelerationFSensor.class.getSimpleName();

    protected final SensorManager sensorManager;
    protected final List<FSensorEventListener> fSensorEventListeners = new ArrayList<>();

    protected final Rotation rotation;

    protected final float[] output = new float[3];

    public BaseFSensor(SensorManager sensorManager, Rotation rotation) {
        this.sensorManager = sensorManager;
        this.rotation = rotation;
    }

    @Override
    public void registerListener(FSensorEventListener sensorEventListener, int sensorDelay) {
        if (fSensorEventListeners.isEmpty()) {
            registerSensors(sensorDelay);
            rotation.start(sensorDelay);
        }

        fSensorEventListeners.add(sensorEventListener);
    }

    @Override
    public void unregisterListener(FSensorEventListener sensorEventListener) {
        this.fSensorEventListeners.remove(sensorEventListener);
        if (fSensorEventListeners.isEmpty()) {
            unregisterSensors();
            rotation.stop();
        }
    }

    private void registerSensors(int sensorDelay) {
        sensorManager.registerListener(getSensorEventListener(), sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay);
        sensorManager.registerListener(getSensorEventListener(), sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorDelay);
        sensorManager.registerListener(getSensorEventListener(), sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorDelay);
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(getSensorEventListener());
    }

    protected abstract SensorEventListener getSensorEventListener();
}

