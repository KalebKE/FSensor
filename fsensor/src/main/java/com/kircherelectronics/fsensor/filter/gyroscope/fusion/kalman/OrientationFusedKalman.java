package com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman;

import android.hardware.SensorManager;
import android.util.Log;

import com.kircherelectronics.fsensor.filter.gyroscope.fusion.OrientationFused;
import com.kircherelectronics.fsensor.filter.gyroscope.fusion.complimentary.OrientationFusedComplimentary;
import com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.filter.RotationKalmanFilter;
import com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.filter.RotationMeasurementModel;
import com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.filter.RotationProcessModel;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;

import org.apache.commons.math3.complex.Quaternion;

import java.util.Arrays;

/*
 * Copyright 2017, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 * Created by kaleb on 7/6/17.
 */

public class OrientationFusedKalman extends OrientationFused {

    private static final String tag = OrientationFusedComplimentary.class.getSimpleName();

    private RotationKalmanFilter kalmanFilter;
    private RotationProcessModel pm;
    private RotationMeasurementModel mm;
    private volatile boolean run;
    private volatile float dT;
    private volatile float[] output = new float[3];
    private Thread thread;

    private volatile Quaternion rotationOrientation;

    public OrientationFusedKalman() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public OrientationFusedKalman(float timeConstant) {
        super(timeConstant);

        pm = new RotationProcessModel();
        mm = new RotationMeasurementModel();

        kalmanFilter = new RotationKalmanFilter(pm, mm);
    }

    public void startFusion() {
        if (run == false && thread == null) {
            run = true;

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (run && !Thread.interrupted()) {

                        calculate();

                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Log.e(tag, "Kalman Thread Run", e);
                            Thread.currentThread().interrupt();
                        }
                    }

                    Thread.currentThread().interrupt();
                }
            });

            thread.start();
        }
    }

    public void stopFusion() {
        if (run == true && thread != null) {
            run = false;
            thread.interrupt();
            thread = null;
        }
    }

    public float[] getOutput() {
        return output;
    }

    private float[] calculate() {
        if (rotationVectorGyroscope != null && rotationOrientation != null && dT != 0) {

            double[] vectorGyroscope = new double[4];

            vectorGyroscope[0] = (float) rotationVectorGyroscope.getVectorPart()[0];
            vectorGyroscope[1] = (float) rotationVectorGyroscope.getVectorPart()[1];
            vectorGyroscope[2] = (float) rotationVectorGyroscope.getVectorPart()[2];
            vectorGyroscope[3] = (float) rotationVectorGyroscope.getScalarPart();

            double[] vectorAccelerationMagnetic = new double[4];

            vectorAccelerationMagnetic[0] = (float) rotationOrientation.getVectorPart()[0];
            vectorAccelerationMagnetic[1] = (float) rotationOrientation.getVectorPart()[1];
            vectorAccelerationMagnetic[2] = (float) rotationOrientation.getVectorPart()[2];
            vectorAccelerationMagnetic[3] = (float) rotationOrientation.getScalarPart();

            // Apply the Kalman fusedOrientation... Note that the prediction and correction
            // inputs could be swapped, but the fusedOrientation is much more stable in this
            // configuration.
            kalmanFilter.predict(vectorGyroscope);
            kalmanFilter.correct(vectorAccelerationMagnetic);

            // rotation estimation.
            rotationVectorGyroscope = new Quaternion(kalmanFilter.getStateEstimation()[3],
                    Arrays.copyOfRange(kalmanFilter.getStateEstimation(), 0, 3));

            // Now we get a structure we can pass to get a rotation matrix, and then
            // an orientation vector from Android.

            float[] fusedVector = new float[4];

            // Now we get a structure we can pass to get a rotation matrix, and then
            // an orientation vector from Android.
            fusedVector[0] = (float) rotationVectorGyroscope.getVectorPart()[0];
            fusedVector[1] = (float) rotationVectorGyroscope.getVectorPart()[1];
            fusedVector[2] = (float) rotationVectorGyroscope.getVectorPart()[2];
            fusedVector[3] = (float) rotationVectorGyroscope.getScalarPart();

            // rotation matrix from gyro data
            float[] fusedMatrix = new float[9];

            // We need a rotation matrix so we can get the orientation vector...
            // Getting Euler
            // angles from a quaternion is not trivial, so this is the easiest way,
            // but perhaps
            // not the fastest way of doing this.
            SensorManager.getRotationMatrixFromVector(fusedMatrix, fusedVector);

            // Get the fused orientation
            SensorManager.getOrientation(fusedMatrix, output);

            return output;
        }

        return null;
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
    public float[] calculateFusedOrientation(float[] gyroscope, long timestamp, float[] acceleration, float[] magnetic) {

        if(rotationVectorGyroscope != null) {
            if (this.timestamp != 0) {
                dT = (timestamp - this.timestamp) * NS2S;

                rotationOrientation = RotationUtil.getOrientationQuaternionFromAccelerationMagnetic(acceleration, magnetic);
                rotationVectorGyroscope = RotationUtil.integrateGyroscopeRotation(rotationVectorGyroscope, gyroscope, dT, EPSILON);
            }
            this.timestamp = timestamp;

            return output;
        }  else {
            throw new IllegalStateException("You must call setBaseOrientation() before calling calculateFusedOrientation()!");
        }
    }

    /**
     * Calculate the fused orientation of the device.
     *
     * @param gyroscope   the gyroscope measurements.
     * @param timestamp   the gyroscope timestamp
     * @param orientation an estimation of device orientation.
     * @return the fused orientation estimation.
     */
    public float[] calculateFusedOrientation(float[] gyroscope, long timestamp, float[] orientation) {
        if(rotationVectorGyroscope != null) {
            if (this.timestamp != 0) {
                dT = (timestamp - this.timestamp) * NS2S;

                rotationOrientation = RotationUtil.vectorToQuaternion(orientation);
                rotationVectorGyroscope = RotationUtil.integrateGyroscopeRotation(rotationVectorGyroscope, gyroscope, dT, EPSILON);
            }
            this.timestamp = timestamp;

            return output;
        } else {
            throw new IllegalStateException("You must call setBaseOrientation() before calling calculateFusedOrientation()!");
        }
    }
}
