package com.kircherelectronics.fsensor.util;

import android.hardware.SensorManager;

/**
 * Created by kaleb on 7/6/17.
 */

public class Util {

    public static float[] getGravityFromOrientation(float[] orientation) {
        // components[0]: azimuth, rotation around the Z axis.
        // components[1]: pitch, rotation around the X axis.
        // components[2]: roll, rotation around the Y axis.
        float[] components = new float[3];

        // Find the gravity component of the X-axis
        // = g*-cos(pitch)*sin(roll);
        components[0] = (float) (SensorManager.GRAVITY_EARTH
                * -Math.cos(orientation[1]) * Math
                .sin(orientation[2]));

        // Find the gravity component of the Y-axis
        // = g*-sin(pitch);
        components[1] = (float) (SensorManager.GRAVITY_EARTH * -Math
                .sin(orientation[1]));

        // Find the gravity component of the Z-axis
        // = g*cos(pitch)*cos(roll);
        components[2] = (float) (SensorManager.GRAVITY_EARTH
                * Math.cos(orientation[1]) * Math.cos(orientation[2]));

        return components;
    }
}
