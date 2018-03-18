package com.kircherelectronics.fsensor.util.magnetic;

/**
 * A helper class for
 * Created by kaleb on 3/18/18.
 */
public class AzimuthUtil {

    /**
     * Get the azimuth from a magnetic sensor.
     * @param magnetic The magnetic measurements.
     * @return The azimuth in units of degrees with range: 0 < range <= 360.
     */
    public static float getAzimuth(float[] magnetic) {
        float azimuth = (int) Math.toDegrees(Math.atan2(magnetic[0], magnetic[1]));

        // Adjust the range: 0 < range <= 360 (from: -180 < range <=
        // 180)
        return (azimuth + 360) % 360;
    }
}
