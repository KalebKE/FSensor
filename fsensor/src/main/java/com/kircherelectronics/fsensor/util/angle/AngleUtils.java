package com.kircherelectronics.fsensor.util.angle;

import android.util.Log;

public class AngleUtils {
    private static final String TAG = AngleUtils.class.getSimpleName();

    /**
     * https://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm
     * https://developer.android.com/reference/android/hardware/SensorManager#getOrientation(float%5B%5D,%20float%5B%5D)
     *
     * The return format is the same as SensorManager.getOrientation()
     *
     * +X to the right
     * +Y straight up
     * +Z axis toward viewer
     *
     * Heading = rotation about y axis
     * Attitude = rotation about z axis
     * Bank = rotation about x axis
     *
     * Heading applied first
     * Attitude applied second
     * Bank applied last
     *
     * When it returns, the array values are as follows:
     *
     * values[0]: Azimuth, angle of rotation about the -z axis. This value represents the angle between the device's y axis and the magnetic north pole. When facing north, this
     * angle is 0, when facing south, this angle is π. Likewise, when facing east, this angle is π/2, and when facing west, this angle is -π/2. The range of values is -π to π.
     * values[1]: Pitch, angle of rotation about the x axis. This value represents the angle between a plane parallel to the device's screen and a plane parallel to the ground.
     * Assuming that the bottom edge of the device faces the user and that the screen is face-up, tilting the top edge of the device toward the ground creates a positive pitch
     * angle. The range of values is -π to π.
     * values[2]: Roll, angle of rotation about the y axis. This value represents the angle between a plane perpendicular to the device's screen and a plane perpendicular to the
     * ground. Assuming that the bottom edge of the device faces the user and that the screen is face-up, tilting the left edge of the device toward the ground creates a
     * positive roll angle. The range of values is -π/2 to π/2.
     *
     * @param w
     * @param z
     * @param x
     * @param y
     * @return
     */
    public static float[] getAngles(double w, double z, double x, double y) {
        double heading;
        double pitch;
        double roll;

        double test = x*y + z*w;
        if (test > 0.499) { // singularity at north pole
            heading = 2 * Math.atan2(x,w);
            pitch = -Math.PI/2;
            roll = 0;
            Log.e(TAG, "singularity at north pole");
            return new float[]{(float)heading, (float)pitch, (float)roll};
        }
        if (test < -0.499) { // singularity at south pole
            heading = -2 * Math.atan2(x,w);
            pitch = Math.PI/2;
            roll = 0;
            Log.e(TAG, "singularity at south pole");
            return new float[]{(float)heading, (float)pitch, (float)roll};
        }
        double sqx = x*x;
        double sqy = y*y;
        double sqz = z*z;
        heading = -Math.atan2(2*y*w-2*x*z , 1 - 2*sqy - 2*sqz);
        pitch = -Math.asin(2*test);
        roll = -Math.atan2(2*x*w-2*y*z , 1 - 2*sqx - 2*sqz);

        return new float[]{(float) heading, (float) pitch, (float) roll};
    }
}
