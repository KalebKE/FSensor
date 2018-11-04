package com.kircherelectronics.fsensor.sensor.acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter;
import com.kircherelectronics.fsensor.linearacceleration.LinearAcceleration;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationAveraging;
import com.kircherelectronics.fsensor.sensor.FSensor;

import io.reactivex.subjects.PublishSubject;

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

    private SensorManager sensorManager;
    private SimpleSensorListener listener;
    private float startTime = 0;
    private int count = 0;

    private float[] rawAcceleration = new float[3];
    private float[] acceleration = new float[3];
    private float[] output = new float[4];

    private LinearAcceleration linearAccelerationFilterLpf;

    private LowPassFilter lpfGravity;

    private int sensorFrequency = SensorManager.SENSOR_DELAY_FASTEST;

    private PublishSubject<float[]> publishSubject;

    public LowPassLinearAccelerationSensor(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.listener = new SimpleSensorListener();
        this.publishSubject = PublishSubject.create();
        initializeFSensorFusions();
    }

    @Override
    public PublishSubject<float[]> getPublishSubject() {
        return publishSubject;
    }

    public void onStart() {
        startTime = 0;
        count = 0;

        registerSensors(sensorFrequency);
    }

    public void onStop() {
        unregisterSensors();
    }

    public void setSensorFrequency(int sensorFrequency) {
        this.sensorFrequency = sensorFrequency;
    }

    public void setFSensorLpfLinearAccelerationTimeConstant(float timeConstant) {
        lpfGravity.setTimeConstant(timeConstant);
    }

    public void reset() {
        onStop();
        acceleration = new float[3];
        output = new float[4];
        onStart();
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
        publishSubject.onNext(output);
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
