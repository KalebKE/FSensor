package com.kircherelectronics.fsensor.util.magnetic.tilt;

/**
 * Providers helpers to compensate for the tilt of a magnetic sensor.
 * Created by kaleb on 3/18/18.
 */
public class TiltCompensationUtil {

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
