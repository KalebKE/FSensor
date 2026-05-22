package com.tracqi.fsensor.math.gravity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class GravityTest {

    private static final float EPSILON = 0.01f;
    private static final float G = 9.80665f; // SensorManager.GRAVITY_EARTH

    @Test
    public void getOrientationFromGravity_deviceFlatFaceUp_zeroPitchAndRoll() {
        float[] gravity = {0, 0, G};

        float[] orientation = Gravity.getOrientationFromGravity(gravity);

        assertEquals(0f, orientation[0], EPSILON); // azimuth always 0
        assertEquals(0f, orientation[1], EPSILON); // pitch
        assertEquals(0f, orientation[2], EPSILON); // roll
    }

    @Test
    public void getOrientationFromGravity_deviceOnLeftEdge_rollNearPiOver2() {
        // Gravity pointing in +X direction
        float[] gravity = {G, 0, 0};

        float[] orientation = Gravity.getOrientationFromGravity(gravity);

        assertEquals(0f, orientation[0], EPSILON); // azimuth
        // pitch = atan2(0, 0) = 0 (or undefined, but atan2 returns 0)
        assertEquals((float)(Math.PI / 2), orientation[2], EPSILON); // roll = asin(1) = pi/2
    }

    @Test
    public void getOrientationFromGravity_deviceTiltedForward_positivePitch() {
        // Gravity pointing in -Y direction (device tilted forward 90 degrees)
        float[] gravity = {0, -G, 0};

        float[] orientation = Gravity.getOrientationFromGravity(gravity);

        assertEquals(0f, orientation[0], EPSILON); // azimuth
        // pitch = atan2(G, 0) = pi/2
        assertEquals((float)(Math.PI / 2), orientation[1], 0.1f); // pitch positive
        assertEquals(0f, orientation[2], EPSILON); // roll
    }

    @Test
    public void getOrientationFromGravity_gravityZComponentZero_noException() {
        // This was the division-by-zero bug
        float[] gravity = {0, G, 0};

        float[] orientation = Gravity.getOrientationFromGravity(gravity);

        assertFalse(Float.isNaN(orientation[1])); // pitch should not be NaN
        assertFalse(Float.isInfinite(orientation[1]));
    }

    @Test
    public void getOrientationFromGravity_deviceUpsideDown_noNaN() {
        float[] gravity = {0, 0, -G};

        float[] orientation = Gravity.getOrientationFromGravity(gravity);

        assertFalse(Float.isNaN(orientation[1]));
        assertFalse(Float.isNaN(orientation[2]));
    }

    @Test
    public void getOrientationFromGravity_excessiveAcceleration_clampedNoNaN() {
        // Acceleration exceeds gravity (e.g., during impact) - would cause asin > 1
        float[] gravity = {G * 1.5f, 0, 0};

        float[] orientation = Gravity.getOrientationFromGravity(gravity);

        assertFalse(Float.isNaN(orientation[2])); // roll should be clamped, not NaN
    }

    @Test
    public void getGravityFromOrientation_zeroPitchRoll_gravityAlongZ() {
        float[] orientation = {0, 0, 0}; // pitch=0, roll=0

        float[] gravity = Gravity.getGravityFromOrientation(orientation);

        assertEquals(0f, gravity[0], EPSILON); // x
        assertEquals(0f, gravity[1], EPSILON); // y
        assertEquals(G, gravity[2], EPSILON);  // z = g*cos(0)*cos(0) = g
    }

    @Test
    public void getGravityFromOrientation_pitchHalfPi_gravityAlongNegY() {
        float[] orientation = {0, (float)(Math.PI / 2), 0}; // pitch=90deg

        float[] gravity = Gravity.getGravityFromOrientation(orientation);

        assertEquals(0f, gravity[0], EPSILON);
        assertEquals(-G, gravity[1], EPSILON); // y = g*-sin(pi/2) = -g
        assertEquals(0f, gravity[2], EPSILON); // z = g*cos(pi/2)*cos(0) ≈ 0
    }

    @Test
    public void roundTrip_gravityFromOrientation_thenOrientationFromGravity() {
        float pitch = 0.3f;
        float roll = 0.5f;
        float[] orientation = {0, pitch, roll};

        float[] gravity = Gravity.getGravityFromOrientation(orientation);
        float[] recoveredOrientation = Gravity.getOrientationFromGravity(gravity);

        // Wider tolerance due to asin(g/G) approximation when acceleration magnitude != G exactly
        assertEquals(pitch, recoveredOrientation[1], 0.05f);
        assertEquals(roll, recoveredOrientation[2], 0.05f);
    }
}
