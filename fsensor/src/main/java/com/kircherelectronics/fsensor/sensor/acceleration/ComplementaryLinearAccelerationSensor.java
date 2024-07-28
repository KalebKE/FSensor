package com.kircherelectronics.fsensor.sensor.acceleration;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.kircherelectronics.fsensor.sensor.orientation.fusion.fusion.complementary.ComplimentaryOrientation;
import com.kircherelectronics.fsensor.sensor.FSensor;
import com.kircherelectronics.fsensor.sensor.FSensorEvent;
import com.kircherelectronics.fsensor.sensor.FSensorEventListener;
import com.kircherelectronics.fsensor.util.gravity.GravityUtil;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;

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

public class ComplementaryLinearAccelerationSensor implements FSensor {
    private static final String TAG = ComplementaryLinearAccelerationSensor.class.getSimpleName();

    private final SensorManager sensorManager;
    private final List<FSensorEventListener> fSensorEventListeners = new ArrayList<>();
    private final SensorEventListener sensorEventListener = new SensorListener();

    private final float[] acceleration = new float[3];
    private final float[] magnetic = new float[3];
    private final float[] rotation = new float[3];
    private final float[] output = new float[4];
    private long rotationTimestamp;

    private final ComplimentaryOrientation orientationFusionComplimentary = new ComplimentaryOrientation();

    public ComplementaryLinearAccelerationSensor(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    @Override
    public void registerListener(FSensorEventListener sensorEventListener, int sensorDelay) {
        if (fSensorEventListeners.isEmpty()) {
            registerSensors(sensorDelay);
        }

        fSensorEventListeners.add(sensorEventListener);
    }

    @Override
    public void unregisterListener(FSensorEventListener sensorEventListener) {
        this.fSensorEventListeners.remove(sensorEventListener);
        if (fSensorEventListeners.isEmpty()) {
            unregisterSensors();
        }
    }

    private void copyAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void copyMagnetic(float[] magnetic) {
        System.arraycopy(magnetic, 0, this.magnetic, 0, this.magnetic.length);
    }

    private void copyRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.length);
    }

    private void registerSensors(int sensorDelay) {
        orientationFusionComplimentary.reset();
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorDelay);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorDelay);
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    private void calculateLinerAcceleration(float[] acceleration, float[] gravity) {
        // Determine the linear acceleration
        output[0] = acceleration[0] - gravity[0];
        output[1] = acceleration[1] - gravity[1];
        output[2] = acceleration[2] - gravity[2];
    }

    private class SensorListener implements SensorEventListener {
        private boolean hasAcceleration = false;
        private boolean hasRotation = false;
        private boolean hasMagnetic = false;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                copyAcceleration(event.values);
                hasAcceleration = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                copyMagnetic(event.values);
                hasMagnetic = true;
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                copyRotation(event.values);
                rotationTimestamp = event.timestamp;
                hasRotation = true;
            }

            if (hasAcceleration && hasRotation && hasMagnetic) {
                hasAcceleration = false;
                hasRotation = false;
                hasMagnetic = false;

                if (!orientationFusionComplimentary.isBaseOrientationSet()) {
                    orientationFusionComplimentary.setBaseOrientation(RotationUtil.getOrientationVector(acceleration, magnetic));
                } else {
                    float[] gravity = GravityUtil.getGravityFromOrientation(orientationFusionComplimentary.calculateFusedOrientation(rotation, rotationTimestamp, acceleration, magnetic));
                    calculateLinerAcceleration(event.values, gravity);
                    for (FSensorEventListener fSensorEventListeners : fSensorEventListeners) {
                        fSensorEventListeners.onSensorChanged(new FSensorEvent(event.sensor, event.accuracy, event.timestamp, output));
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
