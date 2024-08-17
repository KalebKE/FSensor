package com.tracqi.fsensor.math.magnetic.tilt;

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
 * Providers helpers to compensate for the tilt of a magnetic sensor.
 * Created by kaleb on 3/18/18.
 */
public class TiltCompensation {

    /**
     * Get the rotation vector based on the tilt of the acceleration sensor.
     * @param acceleration the acceleration vector (tilt) from the device.
     * @return a rotation vector [roll, pitch, azimuth (always 0)]
     */
    public static float[] getRotationFromAcceleration(float[] acceleration) {
        float gpx = acceleration[0];
        float gpy = acceleration[1];
        float gpz = acceleration[2];

        // Calculate the rotation around the x-axis (the roll) of the device
        float phi = (float) Math.atan2(gpy, gpz);

        // calculate current pitch angle Theta
        float the = (float) Math.atan2(-gpx, gpz);

        return new float[]{phi, the, 0};
    }

    /**
     * Compensate for the tilt of the magnetic sensor with a rotation vector.
     * @param magnetic the magnetic sensor vector.
     * @param rotation the rotation vector [roll, pitch, azimuth] from the gyroscope, acceleration sensor or both (note that azimuth is not used).
     * @return the compensated magnetic vector.
     */
    public static float[] compensateTilt(float[] magnetic, float[] rotation)
    {
        float bpx = magnetic[0];
        float bpy = magnetic[1];
        float bpz = magnetic[2];

        // Calculate the sin and cos of Phi (the roll)
        float sin = (float) Math.sin(rotation[0]);
        float cos = (float) Math.cos(rotation[0]);

        // De-rotate the magnetic y-axis by Phi (the roll)
        float bfy = ((bpy * cos) - (bpz * sin));
        // De-rotate the magnetic z-axis by Phi (the roll)
        bpz = (bpy * sin + bpz * cos);

        // Calculate the sin and cos of Theta (the pitch)
        sin = (float) Math.sin(rotation[1]);
        cos = (float) Math.cos(rotation[1]);

        // De-rotate the x-axis by pitch angle Theta
        float bfx = (bpx * cos) + (bpz * sin);
        // De-rotate the z-axis by pitch angle Theta
        float bfz = (-bpx * sin) + (bpz * cos);

        return new float[]{bfx, bfy, bfz};
    }
}
