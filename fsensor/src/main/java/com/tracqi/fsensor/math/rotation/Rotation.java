package com.tracqi.fsensor.math.rotation;

import android.hardware.SensorManager;

import org.apache.commons.math3.complex.Quaternion;

import java.util.Arrays;

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

public class Rotation {

    /**
     * Calculates a rotation vector from the gyroscope angular speed values.
     * <p>
     * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
     *
     * @param previousRotationVector the last known orientation to which the new rotation will be applied.
     * @param rateOfRotation         the rotation measurement
     * @param dt                     the period of time over which the rotation measurement took place in units of seconds
     * @param epsilon                minimum rotation vector magnitude required to get the axis for normalization
     * @return A Quaternion representing the orientation.
     */
    public static Quaternion integrateGyroscopeRotation(Quaternion previousRotationVector, float[] rateOfRotation, float dt, float epsilon) {
        float wx = rateOfRotation[0];
        float wy = rateOfRotation[1];
        float wz = rateOfRotation[2];

        float magnitude = (float) Math.sqrt(wx * wx + wy * wy + wz * wz);

        // Normalize the rotation axis if magnitude is large enough
        if (magnitude > epsilon) {
            wx /= magnitude;
            wy /= magnitude;
            wz /= magnitude;
        }

        // Convert axis-angle to delta quaternion
        float thetaOverTwo = magnitude * dt / 2.0f;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

        Quaternion deltaQuaternion = new Quaternion(
                cosThetaOverTwo,
                sinThetaOverTwo * wx,
                sinThetaOverTwo * wy,
                sinThetaOverTwo * wz
        );

        return previousRotationVector.multiply(deltaQuaternion);
    }

    /**
     * Calculates orientation vector from accelerometer and magnetometer output.
     *
     * @param acceleration the acceleration measurement.
     * @param magnetic     the magnetic measurement.
     * @return A unit quaternion representing the orientation, or null if the rotation matrix cannot be computed.
     */
    public static Quaternion getOrientationVector(float[] acceleration, float[] magnetic) {
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magnetic)) {
            double[] q = getQuaternion(rotationMatrix);
            return new Quaternion(q[0], q[1], q[2], q[3]);
        }

        return null;
    }

    /**
     * Converts a 3x3 rotation matrix (row-major, float[9]) to a unit quaternion
     * using Shepperd's method for numerical stability across all rotation angles.
     *
     * @param m rotation matrix in row-major order: [m00, m01, m02, m10, m11, m12, m20, m21, m22]
     * @return quaternion as [w, x, y, z]
     */
    static double[] getQuaternion(float[] m) {
        double m00 = m[0], m01 = m[1], m02 = m[2];
        double m10 = m[3], m11 = m[4], m12 = m[5];
        double m20 = m[6], m21 = m[7], m22 = m[8];

        double trace = m00 + m11 + m22;
        double w, x, y, z;

        if (trace > 0) {
            double s = 0.5 / Math.sqrt(trace + 1.0);
            w = 0.25 / s;
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else if (m00 > m11 && m00 > m22) {
            double s = 2.0 * Math.sqrt(1.0 + m00 - m11 - m22);
            w = (m21 - m12) / s;
            x = 0.25 * s;
            y = (m01 + m10) / s;
            z = (m02 + m20) / s;
        } else if (m11 > m22) {
            double s = 2.0 * Math.sqrt(1.0 + m11 - m00 - m22);
            w = (m02 - m20) / s;
            x = (m01 + m10) / s;
            y = 0.25 * s;
            z = (m12 + m21) / s;
        } else {
            double s = 2.0 * Math.sqrt(1.0 + m22 - m00 - m11);
            w = (m10 - m01) / s;
            x = (m02 + m20) / s;
            y = (m12 + m21) / s;
            z = 0.25 * s;
        }

        // Normalize to ensure unit quaternion
        double norm = Math.sqrt(w * w + x * x + y * y + z * z);
        return new double[]{w / norm, x / norm, y / norm, z / norm};
    }

}
