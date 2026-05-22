package com.tracqi.fsensor.math.angle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class AnglesTest {

    private static final float EPSILON = 0.01f;

    @Test
    public void getAngles_identityQuaternion_returnsZeroAngles() {
        // Identity quaternion: w=1, x=0, y=0, z=0
        float[] angles = Angles.getAngles(1, 0, 0, 0);

        assertEquals(0f, angles[0], EPSILON); // heading/azimuth
        assertEquals(0f, angles[1], EPSILON); // pitch
        assertEquals(0f, angles[2], EPSILON); // roll
    }

    @Test
    public void getAngles_90degAroundZ_heading90() {
        // 90 deg around Z: w=cos(45)=0.7071, z=sin(45)=0.7071
        // getAngles params match Apache Commons Quaternion: (q0=scalar, q1=i, q2=j, q3=k)
        double cos45 = Math.sqrt(2.0) / 2.0;
        // For rotation around Z: q0=cos45, q1=0, q2=0, q3=sin45
        float[] angles = Angles.getAngles(cos45, 0, 0, cos45);

        // Should produce heading = ±pi/2
        assertEquals((float)(Math.PI / 2), Math.abs(angles[0]), 0.1f);
    }

    @Test
    public void getAngles_noNanForAnyQuadrant() {
        // Test multiple quaternions across all quadrants
        double[][] quaternions = {
                {1, 0, 0, 0},       // identity
                {0.7071, 0, 0, 0.7071}, // 90 around Z
                {0.7071, 0.7071, 0, 0}, // 90 around X
                {0.7071, 0, 0.7071, 0}, // 90 around Y
                {0, 1, 0, 0},       // 180 around X
                {0, 0, 1, 0},       // 180 around Y
                {0, 0, 0, 1},       // 180 around Z
        };

        for (double[] q : quaternions) {
            float[] angles = Angles.getAngles(q[0], q[1], q[2], q[3]);
            assertFalse("NaN in heading for q=" + java.util.Arrays.toString(q), Float.isNaN(angles[0]));
            assertFalse("NaN in pitch for q=" + java.util.Arrays.toString(q), Float.isNaN(angles[1]));
            assertFalse("NaN in roll for q=" + java.util.Arrays.toString(q), Float.isNaN(angles[2]));
        }
    }

    @Test
    public void getAngles_northPoleSingularity_handledGracefully() {
        // Singularity when test = x*y + z*w > 0.499
        // Construct a quaternion at the singularity
        // For test = x*y + z*w = 0.5 (north pole): e.g., x=0.5, y=0.5, z=0.5, w=0.5
        float[] angles = Angles.getAngles(0.5, 0.5, 0.5, 0.5);

        assertFalse(Float.isNaN(angles[0]));
        assertFalse(Float.isNaN(angles[1]));
        assertFalse(Float.isNaN(angles[2]));
        assertEquals(-(float)(Math.PI / 2), angles[1], 0.1f); // pitch at -pi/2
    }

    @Test
    public void getAngles_southPoleSingularity_handledGracefully() {
        // In getAngles(w, z, x, y): test = x*y + z*w
        // For south pole: test < -0.499
        // With w=0.5, z=-0.5, x=0.5, y=0.5: test = 0.5*0.5 + (-0.5)*0.5 = 0.25 - 0.25 = 0 (not south pole)
        // Need test < -0.499: e.g., w=0.5, z=0.5, x=-0.5, y=0.5: test = -0.5*0.5 + 0.5*0.5 = 0 (also not)
        // w=0.5, z=-0.5, x=-0.5, y=0.5: test = -0.5*0.5 + (-0.5)*0.5 = -0.5
        float[] angles = Angles.getAngles(0.5, -0.5, -0.5, 0.5);

        assertFalse(Float.isNaN(angles[0]));
        assertFalse(Float.isNaN(angles[1]));
        assertFalse(Float.isNaN(angles[2]));
        assertEquals((float)(Math.PI / 2), angles[1], 0.1f); // pitch at pi/2
    }

    @Test
    public void getAngles_outputRanges_withinExpectedBounds() {
        // Random quaternions should produce angles within expected ranges
        double[] ws = {0.1, 0.5, 0.9, -0.3};
        double[] xs = {0.3, -0.2, 0.1, 0.8};
        double[] ys = {0.2, 0.4, -0.6, 0.1};
        double[] zs = {0.9, 0.7, 0.1, 0.5};

        for (int i = 0; i < ws.length; i++) {
            // Normalize
            double norm = Math.sqrt(ws[i]*ws[i] + xs[i]*xs[i] + ys[i]*ys[i] + zs[i]*zs[i]);
            float[] angles = Angles.getAngles(ws[i]/norm, xs[i]/norm, xs[i]/norm, ys[i]/norm);

            assertTrue("Heading out of range", angles[0] >= -Math.PI && angles[0] <= Math.PI);
            assertTrue("Pitch out of range", angles[1] >= -Math.PI && angles[1] <= Math.PI);
            assertTrue("Roll out of range", angles[2] >= -Math.PI && angles[2] <= Math.PI);
        }
    }
}
