package com.kircherelectronics.fsensor.util.rotation;

import android.hardware.SensorManager;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;

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
 * Created by kaleb on 4/1/18.
 */

public class RotationUtil {

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
        // Calculate the angular speed of the sample
        float magnitude = (float) Math.sqrt(Math.pow(rateOfRotation[0], 2)
                + Math.pow(rateOfRotation[1], 2) + Math.pow(rateOfRotation[2], 2));

        // Normalize the rotation vector if it's big enough to get the axis
        if (magnitude > epsilon) {
            rateOfRotation[0] /= magnitude;
            rateOfRotation[1] /= magnitude;
            rateOfRotation[2] /= magnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = magnitude * dt / 2.0f;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

        double[] deltaVector = new double[4];

        deltaVector[0] = sinThetaOverTwo * rateOfRotation[0];
        deltaVector[1] = sinThetaOverTwo * rateOfRotation[1];
        deltaVector[2] = sinThetaOverTwo * rateOfRotation[2];
        deltaVector[3] = cosThetaOverTwo;

        // Since it is a unit quaternion, we can just multiply the old rotation
        // by the new rotation delta to integrate the rotation.
        return previousRotationVector.multiply(new Quaternion(deltaVector[3], Arrays.copyOfRange(
                deltaVector, 0, 3)));
    }

    /**
     * Calculates orientation vector from accelerometer and magnetometer output.
     *
     * @param acceleration the acceleration measurement.
     * @param magnetic     the magnetic measurement.
     * @return
     */
    public static Quaternion getOrientationVectorFromAccelerationMagnetic(float[] acceleration, float[] magnetic) {
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magnetic)) {
            float[] rv = new float[3];
            SensorManager.getOrientation(rotationMatrix,rv);
            // SensorManager.getOrientation() returns an orientation in Earth frame and that needs to be rotated into device frame so the reported angles
            // are indexed with the orientation of the sensors
            Rotation rotation = new Rotation(RotationOrder.XYZ, RotationConvention.VECTOR_OPERATOR, rv[1], -rv[2], rv[0]);
            return new Quaternion(rotation.getQ0(), rotation.getQ1(),rotation.getQ2(),rotation.getQ3());
        }

        return null;
    }

    private static double[][] convertTo2DArray(float[] rotation) {
        if (rotation.length != 9) {
            throw new IllegalStateException("Length must be of 9! Length: " + rotation.length);
        }

        double[][] rm = new double[3][3];

        rm[0][0] = rotation[0];
        rm[0][1] = rotation[1];
        rm[0][2] = rotation[2];
        rm[1][0] = rotation[3];
        rm[1][1] = rotation[4];
        rm[1][2] = rotation[5];
        rm[2][0] = rotation[6];
        rm[2][1] = rotation[7];
        rm[2][2] = rotation[8];

        return rm;
    }
}
