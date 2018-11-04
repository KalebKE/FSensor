package com.kircherelectronics.fsensor.filter.gyroscope;

import android.util.Log;

import com.kircherelectronics.fsensor.BaseFilter;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;

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
 * gyroscope, accelerometer and magnetometer. The fusedOrientation is backed by a quaternion based complimentary fusedOrientation.
 * <p>
 * The complementary fusedOrientation is a frequency domain fusedOrientation. In its strictest
 * sense, the definition of a complementary fusedOrientation refers to the use of two or
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
 * the rotations to each sensors measurements via complementary fusedOrientation. This the
 * ideal method because quaternions are not subject to many of the singularties
 * of rotation matrices, such as gimbal lock.
 * <p>
 * The quaternion for the magnetic/acceleration sensor is only needed to apply
 * the weighted quaternion to the gyroscopes weighted quaternion via
 * complementary fusedOrientation to produce the fused rotation. No integrations are
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
public class OrientationGyroscope extends BaseFilter {

    private static final String TAG = OrientationGyroscope.class.getSimpleName();
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 0.000000001f;
    private Quaternion rotationVectorGyroscope;
    private float[] output;
    private long timestamp = 0;

    /**
     * Initialize a singleton instance.
     */
    public OrientationGyroscope() {
        output = new float[3];
    }

    @Override
    public float[] getOutput() {
        return output;
    }

    /**
     * Calculate the fused orientation of the device.
     * @param gyroscope the gyroscope measurements.
     * @param timestamp the gyroscope timestamp
     * @return An orientation vector -> @link SensorManager#getOrientation(float[], float[])}
     */
    public float[] calculateOrientation(float[] gyroscope, long timestamp) {
        if (isBaseOrientationSet()) {

            if (this.timestamp != 0) {
                final float dT = (timestamp - this.timestamp) * NS2S;
                rotationVectorGyroscope = RotationUtil.integrateGyroscopeRotation(rotationVectorGyroscope, gyroscope, dT, EPSILON);

                Rotation rotation = new Rotation(rotationVectorGyroscope.getQ0(), rotationVectorGyroscope.getQ1(), rotationVectorGyroscope.getQ2(),
                        rotationVectorGyroscope.getQ3(), true);

                try {
                    output = doubleToFloat(rotation.getAngles(RotationOrder.XYZ, RotationConvention.VECTOR_OPERATOR));
                } catch(Exception e) {
                    Log.d(TAG, "", e);
                }
            }

            this.timestamp = timestamp;

            return output;
        } else {
            throw new IllegalStateException("You must call setBaseOrientation() before calling calculateFusedOrientation()!");
        }
    }

    /**
     * Set the base orientation (frame of reference) to which all subsequent rotations will be applied.
     * <p>
     * To initialize to an arbitrary local frame of reference pass in the Identity Quaternion. This will initialize the base orientation as the orientation the device is
     * currently in and all subsequent rotations will be relative to this orientation.
     * <p>
     * To initialize to an absolute frame of reference (like Earth frame) the devices orientation must be determine from other sensors (such as the acceleration and magnetic
     * sensors).
     * @param baseOrientation The base orientation to which all subsequent rotations will be applied.
     */
    public void setBaseOrientation(Quaternion baseOrientation) {
        rotationVectorGyroscope = baseOrientation;
    }

    public void reset() {
        rotationVectorGyroscope = null;
        timestamp = 0;
    }

    public boolean isBaseOrientationSet() {
        return rotationVectorGyroscope != null;
    }

    private static float[] doubleToFloat(double[] values) {
        float[] f = new float[values.length];

        for(int i = 0; i < f.length; i++){
            f[i] = (float) values[i];
        }

        return f;
    }
}
