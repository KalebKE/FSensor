package com.tracqi.fsensor.rotation.fusion.kalman;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.tracqi.fsensor.filter.LowPassFilter;
import com.tracqi.fsensor.rotation.fusion.FusedRotation;
import com.tracqi.fsensor.rotation.fusion.kalman.filter.KalmanFilter;
import com.tracqi.fsensor.rotation.fusion.kalman.filter.RotationProcessModel;
import com.tracqi.fsensor.rotation.fusion.kalman.filter.RotationMeasurementModel;
import com.tracqi.fsensor.math.angle.Angles;
import com.tracqi.fsensor.math.rotation.Rotation;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;

import java.util.Arrays;

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

/**
 * An implementation of a Kalman fusedOrientation based orientation sensor fusion.
 * * <p>
 * The fusedOrientation attempts to fuse magnetometer, gravity and gyroscope
 * sensors together to produce an accurate measurement of the rotation of the
 * device.
 * <p>
 * The magnetometer and acceleration sensors are used to determine one of the
 * two orientation estimations of the device. This measurement is subject to the
 * constraint that the device must not be accelerating and hard and soft-iron
 * distortions are not present in the local magnetic field.
 * <p>
 * The gyroscope is used to determine the second of two orientation estimations
 * of the device. The gyroscope can have a shorter response time and is not
 * effected by linear acceleration or magnetic field distortions, however it
 * experiences drift and has to be compensated periodically by the
 * acceleration/magnetic sensors to remain accurate.
 * <p>
 * Quaternions are used to integrate the measurements of the gyroscope and apply
 * the rotations to each sensors measurements via Kalman fusedOrientation. This the
 * ideal method because quaternions are not subject to many of the singularties
 * of rotation matrices, such as gimbal lock.
 * <p>
 */

public class KalmanRotation extends FusedRotation {

    private static final String TAG = KalmanRotation.class.getSimpleName();

    private final SensorManager sensorManager;
    private final SensorEventListener sensorEventListener = new SensorListener();

    private final KalmanFilter kalmanFilter;
    private final LowPassFilter accelerationFilter = new LowPassFilter();

    private volatile float dT;

    private final float[] magnetic = new float[3];
    private final float[] acceleration = new float[3];
    private final float[] rotation = new float[3];

    private final float[] output = new float[3];

    private long rotationTimestamp;

    private long timestamp;

    private volatile Quaternion rotationVectorAccelerationMagnetic;
    private final double[] vectorGyroscope = new double[4];
    private final double[] vectorAccelerationMagnetic = new double[4];


    public KalmanRotation(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.kalmanFilter = new KalmanFilter(new RotationProcessModel(), new RotationMeasurementModel());
    }

    public KalmanRotation(SensorManager sensorManager, ProcessModel processModel, MeasurementModel measurementModel) {
        this.sensorManager = sensorManager;
        this.kalmanFilter = new KalmanFilter(processModel, measurementModel);
    }

    @Override
    public void start(int sensorDelay) {
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorDelay);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorDelay);
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public float[] getOrientation() {
        return output;
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

    /**
     * Calculate the fused orientation of the device.
     *
     * @param gyroscope    the gyroscope measurements.
     * @param timestamp    the gyroscope timestamp
     * @param acceleration the acceleration measurements
     * @param magnetic     the magnetic measurements
     * @return the fused orientation estimation.
     */
    private void calculateFusedOrientation(float[] gyroscope, long timestamp, float[] acceleration, float[] magnetic) {
        if (isBaseOrientationSet()) {
            if (this.timestamp != 0) {
                dT = (timestamp - this.timestamp) * NS2S;

                // Low-pass filter raw acceleration to isolate gravity component
                float[] gravity = accelerationFilter.filter(acceleration);

                rotationVectorAccelerationMagnetic = Rotation.getOrientationVector(gravity, magnetic);

                if(rotationVectorAccelerationMagnetic != null) {
                    rotationVector = Rotation.integrateGyroscopeRotation(rotationVector, gyroscope, dT, EPSILON);

                    vectorGyroscope[0] = rotationVector.getVectorPart()[0];
                    vectorGyroscope[1] = rotationVector.getVectorPart()[1];
                    vectorGyroscope[2] = rotationVector.getVectorPart()[2];
                    vectorGyroscope[3] = rotationVector.getScalarPart();

                    vectorAccelerationMagnetic[0] = rotationVectorAccelerationMagnetic.getVectorPart()[0];
                    vectorAccelerationMagnetic[1] = rotationVectorAccelerationMagnetic.getVectorPart()[1];
                    vectorAccelerationMagnetic[2] = rotationVectorAccelerationMagnetic.getVectorPart()[2];
                    vectorAccelerationMagnetic[3] = rotationVectorAccelerationMagnetic.getScalarPart();

                    kalmanFilter.predict(vectorGyroscope);
                    kalmanFilter.correct(vectorAccelerationMagnetic);

                    // Normalize the Kalman state to maintain unit quaternion
                    double[] state = kalmanFilter.getStateEstimation();
                    double norm = Math.sqrt(state[0]*state[0] + state[1]*state[1] + state[2]*state[2] + state[3]*state[3]);
                    Quaternion result = new Quaternion(state[3] / norm, state[0] / norm, state[1] / norm, state[2] / norm);

                    // Update rotation vector for next iteration
                    rotationVector = result;

                    float[] angles = Angles.getAngles(result.getQ0(), result.getQ1(), result.getQ2(), result.getQ3());
                    System.arraycopy(angles, 0, this.output, 0, angles.length);
                }
            }
            this.timestamp = timestamp;
        } else {
            throw new IllegalStateException("You must call setBaseOrientation() before calling calculateFusedOrientation()!");
        }
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

                if (!isBaseOrientationSet()) {
                    setBaseOrientation(Rotation.getOrientationVector(acceleration, magnetic));
                } else {
                    calculateFusedOrientation(rotation, rotationTimestamp, acceleration, magnetic);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}

