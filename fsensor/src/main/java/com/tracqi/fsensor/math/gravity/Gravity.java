package com.tracqi.fsensor.math.gravity;

import android.hardware.SensorManager;

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

/**
 * Created by kaleb on 7/6/17.
 */
public class Gravity {
    /**
     * Assumes a positive, counter-clockwise, right-handed rotation
     * orientation[0] = pitch, rotation around the X axis.
     * orientation[1] = roll, rotation around the Y axis
     * orientation[2] = azimuth, rotation around the Z axis
     * @param orientation The orientation.
     * @return The gravity components of the orientation.
     */
    public static float[] getGravityFromOrientation(float[] orientation) {
       // Log.d("GravityUtil", "Orientation: " + Arrays.toString(orientation));

        float[] components = new float[3];

        float pitch = orientation[1];
        float roll = orientation[2];

        // Find the gravity component of the X-axis
        // = g*-cos(pitch)*sin(roll);
        components[0] = (float) -(SensorManager.GRAVITY_EARTH * -Math.cos(pitch) * Math.sin(roll));

        // Find the gravity component of the Y-axis
        // = g*-sin(pitch);
        components[1] = (float) (SensorManager.GRAVITY_EARTH * -Math.sin(pitch));

        // Find the gravity component of the Z-axis
        // = g*cos(pitch)*cos(roll);
        components[2] = (float) (SensorManager.GRAVITY_EARTH * Math.cos(pitch) * Math.cos(roll));

        return components;
    }

    /**
     * Assumes a positive, counter-clockwise, right-handed rotation
     * @ param gravity The gravity components.
     * @return  orientation[0] = pitch, rotation around the X axis.
     *          orientation[1] = roll, rotation around the Y axis
     *          orientation[2] = azimuth, rotation around the Z axis
     */
    public static float[] getOrientationFromGravity(float[] gravity) {
        float pitch = (float) Math.atan(-gravity[1] / gravity[2]);
        float roll = (float) Math.asin(gravity[0] / SensorManager.GRAVITY_EARTH);
        return new float[] {0, pitch, roll};
    }
}
