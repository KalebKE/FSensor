package com.kircherelectronics.fsensor.filter.fusion;

import android.hardware.SensorManager;
import android.util.Log;

import com.kircherelectronics.fsensor.filter.kalman.RotationKalmanFilter;
import com.kircherelectronics.fsensor.filter.kalman.RotationMeasurementModel;
import com.kircherelectronics.fsensor.filter.kalman.RotationProcessModel;

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
 * An implementation of a Kalman filter based orientation sensor fusion.
 *  * <p>
 * The filter attempts to fuse magnetometer, gravity and gyroscope
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
 * the rotations to each sensors measurements via Kalman filter. This the
 * ideal method because quaternions are not subject to many of the singularties
 * of rotation matrices, such as gimbal lock.
 * <p>
 * Created by kaleb on 7/6/17.
 */

public class OrientationKalmanFusion extends OrientationFusion {

    private static final String tag = OrientationComplimentaryFusion.class.getSimpleName();

    private RotationKalmanFilter kalmanFilter;
    private RotationProcessModel pm;
    private RotationMeasurementModel mm;
    private volatile boolean run;
    private volatile float dt;
    private volatile float[] fusedOrientation = new float[3];
    private volatile float[] acceleration = new float[3];
    private volatile float[] magnetic = new float[3];
    private volatile float[] gyroscope = new float[4];
    private Thread thread;

    public OrientationKalmanFusion() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public OrientationKalmanFusion(float timeConstant) {
        super(timeConstant);

        pm = new RotationProcessModel();
        mm = new RotationMeasurementModel();

        kalmanFilter = new RotationKalmanFilter(pm, mm);
    }

    @Override
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

    @Override
    public void stopFusion() {
        if (run == true && thread != null) {
            run = false;
            thread.interrupt();
            thread = null;
        }
    }

    private float[] calculate() {
        float[] baseOrientation = getBaseOrientation(acceleration, magnetic);

        if (baseOrientation != null) {

            Quaternion rotationVectorAccelerationMagnetic = getAccelerationMagneticRotationVector(baseOrientation);
            initializeRotationVectorGyroscopeIfRequired(rotationVectorAccelerationMagnetic);

            rotationVectorGyroscope = getGyroscopeRotationVector(rotationVectorGyroscope, gyroscope, dt);

            // Since we have to sample at a different rate than the samples are delivered, we integrate and the reset
            // when
            // we sample in the thread...
            dt = 0;

            double[] vectorGyroscope = new double[4];

            vectorGyroscope[0] = (float) rotationVectorGyroscope.getVectorPart()[0];
            vectorGyroscope[1] = (float) rotationVectorGyroscope.getVectorPart()[1];
            vectorGyroscope[2] = (float) rotationVectorGyroscope.getVectorPart()[2];
            vectorGyroscope[3] = (float) rotationVectorGyroscope.getScalarPart();

            double[] vectorAccelerationMagnetic = new double[4];

            vectorAccelerationMagnetic[0] = (float) rotationVectorAccelerationMagnetic.getVectorPart()[0];
            vectorAccelerationMagnetic[1] = (float) rotationVectorAccelerationMagnetic.getVectorPart()[1];
            vectorAccelerationMagnetic[2] = (float) rotationVectorAccelerationMagnetic.getVectorPart()[2];
            vectorAccelerationMagnetic[3] = (float) rotationVectorAccelerationMagnetic.getScalarPart();

            // Apply the Kalman filter... Note that the prediction and correction
            // inputs could be swapped, but the filter is much more stable in this
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
            SensorManager.getOrientation(fusedMatrix, fusedOrientation);

            return fusedOrientation;
        }

        // The device had a problem determining the base orientation from the acceleration and magnetic sensors,
        // possible because of bad inputs or possibly because the device determined the orientation could not be
        // calculated, e.g the device is in free-fall
        Log.w(tag, "Base Device Orientation could not be computed!");

        return null;
    }

    /**
     * Calculate the fused orientation.
     */
    protected float[] calculateFusedOrientation(float[] gyroscope, float dt, float[] acceleration, float[] magnetic) {
        this.gyroscope = gyroscope;
        // Since we have to sample at a different rate than the samples are delivered, we integrate and the reset when
        // we sample in the thread...
        this.dt += dt;
        this.acceleration = acceleration;
        this.magnetic = magnetic;

        return fusedOrientation;
    }
}
