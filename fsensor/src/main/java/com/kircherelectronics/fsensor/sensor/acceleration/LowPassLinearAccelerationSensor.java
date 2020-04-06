package com.kircherelectronics.fsensor.sensor.acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;
import com.kircherelectronics.fsensor.linearacceleration.LinearAcceleration;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationAveraging;
import com.kircherelectronics.fsensor.observer.SensorSubject;
import com.kircherelectronics.fsensor.sensor.FSensor;

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

public class LowPassLinearAccelerationSensor implements FSensor {
    private static final String TAG = LowPassLinearAccelerationSensor.class.getSimpleName();

    private final SensorManager sensorManager;
    private final SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private float[] rawAcceleration = new float[3];
    private float[] acceleration = new float[3];
    private float[] output = new float[4];

    private LinearAcceleration linearAccelerationFilterLpf;

    private LowPassFilter lpfGravity;

    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;

    private final SensorSubject sensorSubject;

    public LowPassLinearAccelerationSensor(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.listener = new SimpleSensorListener();
        this.sensorSubject = new SensorSubject();
        initializeFSensorFusions();
    }

    /**
     * Stop the sensor.
     */
    @Override
    public void start() {
        startTime = 0;
        count = 0;

        registerSensors(sensorDelay);
    }

    /**
     * Stop the sensor.
     */
    @Override
    public void stop() {
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

    public void setFSensorLpfLinearAccelerationTimeConstant(float timeConstant) {
        lpfGravity.setTimeConstant(timeConstant);
    }

    public void reset() {
        stop();
        acceleration = new float[3];
        rawAcceleration = new float[3];
        output = new float[4];
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

    private float[] invert(float[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = -values[i];
        }

        return values;
    }

    private void initializeFSensorFusions() {
        lpfGravity = new LowPassFilter();
        linearAccelerationFilterLpf = new LinearAccelerationAveraging(lpfGravity);
    }

    private void processRawAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.rawAcceleration, 0, this.rawAcceleration.length);
    }

    private void processAcceleration(float[] acceleration) {
        System.arraycopy(acceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void registerSensors(int sensorDelay) {

        lpfGravity.reset();

        // Register for sensor updates.
        sensorManager.registerListener(listener, sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
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

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                processRawAcceleration(event.values);
                lpfGravity.filter(rawAcceleration);
                processAcceleration(linearAccelerationFilterLpf.filter(rawAcceleration));
                setOutput(acceleration);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
