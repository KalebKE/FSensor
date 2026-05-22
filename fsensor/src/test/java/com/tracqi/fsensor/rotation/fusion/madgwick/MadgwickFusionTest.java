package com.tracqi.fsensor.rotation.fusion.madgwick;

import org.junit.Test;

import static org.junit.Assert.*;

public class MadgwickFusionTest {

    private static final float G = 9.81f;
    private static final double EPSILON = 0.1;

    @Test
    public void update_stationary_convergesFromIdentity() {
        MadgwickFusion fusion = new MadgwickFusion(0.1f);

        // Gravity pointing down Z, no rotation, no magnetic
        float[] accel = {0, 0, G};
        float[] mag = {20, 0, 40}; // typical indoor magnetic field
        float[] gyro = {0, 0, 0};
        float dt = 0.01f;

        for (int i = 0; i < 500; i++) {
            fusion.update(accel, mag, gyro, dt);
        }

        double[] q = fusion.getQuaternion();
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, 1e-6);

        // With gravity along Z and device flat, should converge near identity
        assertTrue("q0 should be close to 1 (near identity)", Math.abs(q[0]) > 0.9);
    }

    @Test
    public void update_constantRotationZ_tracksGyro() {
        MadgwickFusion fusion = new MadgwickFusion(0.033f);

        float[] accel = {0, 0, G};
        float[] mag = {20, 0, 40};
        float[] gyro = {0, 0, 1.0f}; // 1 rad/s around Z
        float dt = 0.01f;

        for (int i = 0; i < 100; i++) {
            fusion.update(accel, mag, gyro, dt);
        }

        double[] q = fusion.getQuaternion();
        // After 1 second at 1 rad/s around Z, expect rotation of ~1 radian
        // q3 (z component) should be significantly non-zero
        assertTrue("Z rotation should be tracked", Math.abs(q[3]) > 0.1);
    }

    @Test
    public void update_noisyAccel_stillConverges() {
        MadgwickFusion fusion = new MadgwickFusion(0.033f);

        float[] mag = {20, 0, 40};
        float[] gyro = {0, 0, 0};
        float dt = 0.01f;
        java.util.Random rng = new java.util.Random(42);

        for (int i = 0; i < 1000; i++) {
            float[] noisyAccel = {
                    (float)(rng.nextGaussian() * 0.5),
                    (float)(rng.nextGaussian() * 0.5),
                    G + (float)(rng.nextGaussian() * 0.5)
            };
            fusion.update(noisyAccel, mag, gyro, dt);
        }

        double[] q = fusion.getQuaternion();
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, 1e-6);
        // Should still be near identity despite noise
        assertTrue("Should converge despite noisy accel", Math.abs(q[0]) > 0.8);
    }

    @Test
    public void update_6dofMode_worksWithoutMagnetometer() {
        MadgwickFusion fusion = new MadgwickFusion(0.1f);

        float[] accel = {0, 0, G};
        float[] gyro = {0, 0, 0};
        float dt = 0.01f;

        // Should not throw
        for (int i = 0; i < 100; i++) {
            fusion.update(accel, null, gyro, dt);
        }

        double[] q = fusion.getQuaternion();
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, 1e-6);
    }

    @Test
    public void reset_returnsToIdentity() {
        MadgwickFusion fusion = new MadgwickFusion(0.1f);

        float[] accel = {G, 0, 0}; // tilted
        float[] gyro = {1, 1, 1};
        fusion.update(accel, null, gyro, 0.1f);

        fusion.reset();
        double[] q = fusion.getQuaternion();
        assertEquals(1.0, q[0], 1e-10);
        assertEquals(0.0, q[1], 1e-10);
        assertEquals(0.0, q[2], 1e-10);
        assertEquals(0.0, q[3], 1e-10);
    }

    @Test
    public void update_higherBeta_fasterConvergence() {
        MadgwickFusion slowFusion = new MadgwickFusion(0.01f);
        MadgwickFusion fastFusion = new MadgwickFusion(0.5f);

        // Start with tilted gravity
        float[] accel = {0, G, 0}; // gravity along Y (device tilted 90 deg)
        float[] mag = {20, 0, 40};
        float[] gyro = {0, 0, 0};
        float dt = 0.01f;

        for (int i = 0; i < 50; i++) {
            slowFusion.update(accel, mag, gyro, dt);
            fastFusion.update(accel, mag, gyro, dt);
        }

        double[] qSlow = slowFusion.getQuaternion();
        double[] qFast = fastFusion.getQuaternion();

        // Fast filter should have moved further from identity
        double distSlow = Math.abs(1.0 - qSlow[0]);
        double distFast = Math.abs(1.0 - qFast[0]);
        assertTrue("Higher beta should converge faster", distFast > distSlow);
    }
}
