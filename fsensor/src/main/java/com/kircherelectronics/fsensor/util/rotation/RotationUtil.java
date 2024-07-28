package com.kircherelectronics.fsensor.util.rotation;

import android.hardware.SensorManager;
import android.renderscript.Matrix3f;

import org.apache.commons.math3.complex.Quaternion;

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
    public static Quaternion getOrientationVector(float[] acceleration, float[] magnetic) {
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magnetic)) {
            double[] rotation = getQuaternion(new Matrix3f(rotationMatrix));
            return new Quaternion(rotation[0], rotation[1], rotation[2], rotation[3]);
        }

        return null;
    }

    private static double[] getQuaternion(Matrix3f m1) {
        double w = Math.sqrt(1.0 + m1.get(0,0) + m1.get(1,1) + m1.get(2,2)) / 2.0;
        double w4 = (4.0 * w);
        double x = (m1.get(2,1) - m1.get(1,2)) / w4 ;
        double y = (m1.get(0,2) - m1.get(2,0)) / w4 ;
        double z = (m1.get(1,0) - m1.get(0,1)) / w4 ;

        return new double[]{w,x,y,z};
    }

}
