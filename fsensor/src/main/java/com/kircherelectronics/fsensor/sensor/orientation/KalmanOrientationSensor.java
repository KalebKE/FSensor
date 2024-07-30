package com.kircherelectronics.fsensor.sensor.orientation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.kircherelectronics.fsensor.orientation.fusion.kalman.KalmanOrientation;
import com.kircherelectronics.fsensor.observer.SensorSubject;
import com.kircherelectronics.fsensor.sensor.FSensor;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;

/*
 * Copyright 2018, Kircher Electronics, LLC
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

public class KalmanOrientationSensor implements FSensor {
    private static final String TAG = KalmanOrientationSensor.class.getSimpleName();

    private final SensorManager sensorManager;
    private final SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private float[] magnetic = new float[3];
    private float[] acceleration = new float[3];
    private float[] rotation = new float[3];
    private float[] output = new float[4];

    private KalmanOrientation orientationFusionKalman;

    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
    private int sensorType = Sensor.TYPE_GYROSCOPE;

    private final SensorSubject sensorSubject;

    public KalmanOrientationSensor(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.listener = new SimpleSensorListener();
        this.sensorSubject = new SensorSubject();
        initializeFSensorFusions();
    }

    /**
     * Start the sensor.
     */
    @Override
    public void start() {
        startTime = 0;
        count = 0;
        registerSensors(sensorDelay);
        orientationFusionKalman.startFusion();
    }

    /**
     * Stop the sensor.
     */
    @Override
    public void stop() {
        orientationFusionKalman.stopFusion();
        unregisterSensors();
    }

    @Override
    public void register(SensorSubject.SensorObserver sensorObserver) {
        sensorSubject.register(sensorObserver);
    }

    @Override
    public void unregister(SensorSubject.SensorObserver sensorObserver) {
        sensorSubject.unregister(sensorObserver);
    }

    /**
     * Set the gyroscope sensor type.
     * @param sensorType must be Sensor.TYPE_GYROSCOPE or Sensor.TYPE_GYROSCOPE_UNCALIBRATED
     */
    public void setSensorType(int sensorType) {
        if(sensorType != Sensor.TYPE_GYROSCOPE && sensorType != Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
            throw new IllegalStateException("Sensor Type must be Sensor.TYPE_GYROSCOPE or Sensor.TYPE_GYROSCOPE_UNCALIBRATED");
        }

        this.sensorType = sensorType;
    }

    /**
     * Set the sensor frequency.
     * @param sensorDelay Must be SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_NORMAL or SensorManager.SENSOR_DELAY_UI
     */
    public void setSensorDelay(int sensorDelay) {
        if(sensorDelay != SensorManager.SENSOR_DELAY_FASTEST && sensorDelay != SensorManager.SENSOR_DELAY_GAME && sensorDelay != SensorManager.SENSOR_DELAY_NORMAL && sensorDelay != SensorManager.SENSOR_DELAY_UI) {
            throw new IllegalStateException("Sensor Frequency must be SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_NORMAL or " +
                    "SensorManager.SENSOR_DELAY_UI");
        }
        this.sensorDelay = sensorDelay;
    }

    public void reset() {
        stop();
        magnetic = new float[3];
        acceleration = new float[3];
        rotation = new float[3];
        output = new float[4];
        listener.reset();
        start();
    }

    private float calculateSensorFrequency() {
        // Initialize the start time.
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        long timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.

        return (count++ / ((timestamp - startTime) / 1000000000.0f));
    }

    private void initializeFSensorFusions() {
        orientationFusionKalman = new KalmanOrientation();
    }

    private void processAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void processMagnetic(float[] magnetic) {
        System.arraycopy(magnetic, 0, this.magnetic, 0, this.magnetic.length);
    }

    private void processRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.length);
    }

    private void registerSensors(int sensorDelay) {

        orientationFusionKalman.reset();

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorDelay);

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                sensorDelay);

        // Register for sensor updates.
        sensorManager.registerListener(listener,
                sensorManager.getDefaultSensor(sensorType),
                sensorDelay);

    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(listener);
    }

    private void setOutput(float[] value) {
        System.arraycopy(value, 0, output, 0, value.length);
        output[3] = calculateSensorFrequency();
        sensorSubject.onNext(output);
    }

    private class SimpleSensorListener implements SensorEventListener {

        private boolean hasAcceleration = false;
        private boolean hasMagnetic = false;

        private void reset() {
            hasAcceleration = false;
            hasMagnetic = false;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                processAcceleration(event.values);
                hasAcceleration = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                processMagnetic(event.values);
                hasMagnetic = true;
            } else if (event.sensor.getType() == sensorType) {
                processRotation(event.values);

                if (!orientationFusionKalman.isBaseOrientationSet()) {
                    if (hasAcceleration && hasMagnetic) {
                        orientationFusionKalman.setBaseOrientation(RotationUtil.getOrientationVector(acceleration, magnetic));
                    }
                } else {
                    setOutput(orientationFusionKalman.calculateFusedOrientation(rotation, event.timestamp, acceleration, magnetic));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
