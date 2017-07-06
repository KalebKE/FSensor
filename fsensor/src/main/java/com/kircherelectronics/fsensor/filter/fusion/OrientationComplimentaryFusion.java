package com.kircherelectronics.fsensor.filter.fusion;

import android.hardware.SensorManager;
import android.util.Log;

import com.kircherelectronics.fsensor.filter.BaseFilter;

import org.apache.commons.math3.complex.Quaternion;

import java.util.Arrays;

/*
 * Copyright 2017, Kircher Electronics, LLC
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
 * OrientationComplimentaryFilter estimates the orientation of the devices based on a sensor fusion of a
 * gyroscope, accelerometer and magnetometer. The filter is backed by a quaternion based complimentary filter.
 * <p>
 * The complementary filter is a frequency domain filter. In its strictest
 * sense, the definition of a complementary filter refers to the use of two or
 * more transfer functions, which are mathematical complements of one another.
 * Thus, if the data from one sensor is operated on by G(s), then the data from
 * the other sensor is operated on by I-G(s), and the sum of the transfer
 * functions is I, the identity matrix.
 * <p>
 * OrientationComplimentaryFilter attempts to fuse magnetometer, gravity and gyroscope
 * sensors together to produce an accurate measurement of the rotation of the
 * device.
 * <p>
 * The magnetometer and acceleration sensors are used to determine one of the
 * two orientation estimations of the device. This measurement is subject to the
 * constraint that the device must not be accelerating and hard and soft-iron
 * distortions are not present in the local magnetic field..
 * <p>
 * The gyroscope is used to determine the second of two orientation estimations
 * of the device. The gyroscope can have a shorter response time and is not
 * effected by linear acceleration or magnetic field distortions, however it
 * experiences drift and has to be compensated periodically by the
 * acceleration/magnetic sensors to remain accurate.
 * <p>
 * Quaternions are used to integrate the measurements of the gyroscope and apply
 * the rotations to each sensors measurements via complementary filter. This the
 * ideal method because quaternions are not subject to many of the singularties
 * of rotation matrices, such as gimbal lock.
 * <p>
 * The quaternion for the magnetic/acceleration sensor is only needed to apply
 * the weighted quaternion to the gyroscopes weighted quaternion via
 * complementary filter to produce the fused rotation. No integrations are
 * required.
 * <p>
 * The gyroscope provides the angular rotation speeds for all three axes. To
 * find the orientation of the device, the rotation speeds must be integrated
 * over time. This can be accomplished by multiplying the angular speeds by the
 * time intervals between sensor updates. The calculation produces the rotation
 * increment. Integrating these values again produces the absolute orientation
 * of the device. Small errors are produced at each iteration causing the gyro
 * to drift away from the true orientation.
 * <p>
 * To eliminate both the drift and noise from the orientation, the gyroscope
 * measurements are applied only for orientation changes in short time
 * intervals. The magnetometer/acceleration fusion is used for long time
 * intervals. This is equivalent to low-pass filtering of the accelerometer and
 * magnetic field sensor signals and high-pass filtering of the gyroscope
 * signals.
 *
 * @author Kaleb
 *         http://developer.android.com/reference/android/hardware/SensorEvent.html#values
 */
public class OrientationComplimentaryFusion extends OrientationFusion {

    private static final String tag = OrientationComplimentaryFusion.class.getSimpleName();

    /**
     * Initialize a singleton instance.
     */
    public OrientationComplimentaryFusion() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public OrientationComplimentaryFusion(float timeConstant) {
        super(timeConstant);
    }

    /**
     * Calculate the fused orientation.
     */
    protected float[] calculateFusedOrientation(float[] gyroscope, float dt, float[] acceleration, float[] magnetic) {

        float[] baseOrientation = getBaseOrientation(acceleration, magnetic);

        if (baseOrientation != null) {

            float alpha = timeConstant / (timeConstant + dt);
            float oneMinusAlpha = (1.0f - alpha);

            Quaternion rotationVectorAccelerationMagnetic = getAccelerationMagneticRotationVector(baseOrientation);
            initializeRotationVectorGyroscopeIfRequired(rotationVectorAccelerationMagnetic);

            rotationVectorGyroscope = getGyroscopeRotationVector(rotationVectorGyroscope, gyroscope, dt);

            // Apply the complementary filter. // We multiply each rotation by their
            // coefficients (scalar matrices)...
            Quaternion scaledRotationVectorAccelerationMagnetic = rotationVectorAccelerationMagnetic.multiply
                    (oneMinusAlpha);

            // Scale our quaternion for the gyroscope
            Quaternion scaledRotationVectorGyroscope = rotationVectorGyroscope.multiply(alpha);

            // ...and then add the two quaternions together.
            // output[0] = alpha * output[0] + (1 - alpha) * input[0];
            rotationVectorGyroscope = scaledRotationVectorGyroscope.add
                    (scaledRotationVectorAccelerationMagnetic);

            // Now we get a structure we can pass to get a rotation matrix, and then
            // an orientation vector from Android.

            float[] fusedVector = new float[4];

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

            float[] fusedOrientation = new float[3];

            // Get the fused orienatation
            SensorManager.getOrientation(fusedMatrix, fusedOrientation);

            return fusedOrientation;
        }

        // The device had a problem determining the base orientation from the acceleration and magnetic sensors,
        // possible because of bad inputs or possibly because the device determined the orientation could not be
        // calculated, e.g the device is in free-fall
        Log.w(tag, "Base Device Orientation could not be computed!");

        return null;
    }
}
