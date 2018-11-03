package com.kircherelectronics.fsensor.util.gravity;

import android.hardware.SensorManager;
import android.util.Log;

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
 * Helpful utility methods.
 * Created by kaleb on 7/6/17.
 */
public class GravityUtil {

    private static final String tag = GravityUtil.class.getSimpleName();

    private static float[] gravity = new float[]{SensorManager.GRAVITY_EARTH, SensorManager.GRAVITY_EARTH, SensorManager.GRAVITY_EARTH};

    /**
     * Assumes a positive, counter-clockwise, right-handed rotation
     * orientation[0] = pitch, rotation around the X axis.
     * orientation[1] = roll, rotation around the Y axis
     * orientation[2] = azimuth, rotation around the Z axis
     * @param orientation The orientation.
     * @return The gravity components of the orientation.
     */
    public static float[] getGravityFromOrientation(float[] orientation) {

        float[] components = new float[3];

        // Find the gravity component of the X-axis
        // = g*-cos(pitch)*sin(roll);
        components[0] = (float) -(gravity[0]
                * -Math.cos(orientation[0]) * Math
                .sin(orientation[1]));

        // Find the gravity component of the Y-axis
        // = g*-sin(pitch);
        components[1] = (float) (gravity[1] * -Math
                .sin(orientation[0]));

        // Find the gravity component of the Z-axis
        // = g*cos(pitch)*cos(roll);
        components[2] = (float) (gravity[2]
                * Math.cos(orientation[0]) * Math.cos(orientation[1]));

        return components;
    }

    /**
     * Set the gravity as measured by the sensor. Defaults to SensorManager.GRAVITY_EARTH
     * @param g the gravity of earth in units of m/s^2
     */
    public static void setGravity(float[] g) {
        gravity = g;
    }
}
