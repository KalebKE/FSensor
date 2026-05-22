package com.tracqi.fsensor.rotation.fusion.ekf;

import org.junit.Test;

import static org.junit.Assert.*;

public class EkfFusionTest {

    private static final float G = 9.81f;

    @Test
    public void predict_zeroGyro_statePersists() {
        EkfFusion ekf = new EkfFusion();

        float[] zeroGyro = {0, 0, 0};
        ekf.predict(zeroGyro, 0.01f);

        double[] q = ekf.getQuaternion();
        assertEquals(1.0, q[0], 1e-6);
        assertEquals(0.0, q[1], 1e-6);
        assertEquals(0.0, q[2], 1e-6);
        assertEquals(0.0, q[3], 1e-6);
    }

    @Test
    public void predict_constantZRotation_rotatesState() {
        EkfFusion ekf = new EkfFusion();

        float[] gyroZ = {0, 0, 1.0f}; // 1 rad/s around Z
        float dt = 0.01f;

        for (int i = 0; i < 100; i++) {
            ekf.predict(gyroZ, dt);
        }

        double[] q = ekf.getQuaternion();
        // After 1 second at 1 rad/s: q0 ≈ cos(0.5), q3 ≈ sin(0.5)
        assertTrue("q0 should decrease from 1", q[0] < 0.95);
        assertTrue("q3 should be positive (Z rotation)", q[3] > 0.1);

        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, 1e-6);
    }

    @Test
    public void correctAccelerometer_pullsTowardGravity() {
        EkfFusion ekf = new EkfFusion();

        // First rotate state away from identity
        float[] gyroX = {2.0f, 0, 0};
        ekf.predict(gyroX, 0.1f); // rotates significantly

        double[] qBefore = ekf.getQuaternion().clone();

        // Correct with gravity along Z (device flat)
        float[] accel = {0, 0, G};
        ekf.correctAccelerometer(accel);

        double[] qAfter = ekf.getQuaternion();
        // State should move toward identity (flat device)
        double distBefore = Math.sqrt((1-qBefore[0])*(1-qBefore[0]) + qBefore[1]*qBefore[1]);
        double distAfter = Math.sqrt((1-qAfter[0])*(1-qAfter[0]) + qAfter[1]*qAfter[1]);

        assertTrue("Correction should pull state toward gravity alignment",
                distAfter < distBefore);
    }

    @Test
    public void fullCycle_stationaryDevice_converges() {
        EkfFusion ekf = new EkfFusion(0.001, 0.05, 0.5);

        float[] accel = {0, 0, G};
        float[] mag = {20, 0, 40};
        float[] gyro = {0, 0, 0};
        float dt = 0.01f;

        for (int i = 0; i < 500; i++) {
            ekf.predict(gyro, dt);
            ekf.correctAccelerometer(accel);
            ekf.correctMagnetometer(mag);
        }

        double[] q = ekf.getQuaternion();
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, 1e-6);

        // Should converge near identity for flat stationary device
        assertTrue("Should converge near identity, q0=" + q[0], Math.abs(q[0]) > 0.85);
    }

    @Test
    public void fullCycle_dynamicRotation_tracksGyro() {
        EkfFusion ekf = new EkfFusion();

        float[] accel = {0, 0, G};
        float[] mag = {20, 0, 40};
        float[] gyroZ = {0, 0, 0.5f}; // slow Z rotation
        float dt = 0.01f;

        for (int i = 0; i < 200; i++) {
            ekf.predict(gyroZ, dt);
            ekf.correctAccelerometer(accel);
            ekf.correctMagnetometer(mag);
        }

        double[] q = ekf.getQuaternion();
        // Should have some Z rotation component
        assertTrue("Should track Z rotation", Math.abs(q[3]) > 0.05);
    }

    @Test
    public void reset_returnsToIdentity() {
        EkfFusion ekf = new EkfFusion();
        ekf.predict(new float[]{1, 1, 1}, 0.1f);

        ekf.reset();
        double[] q = ekf.getQuaternion();
        assertEquals(1.0, q[0], 1e-10);
        assertEquals(0.0, q[1], 1e-10);
        assertEquals(0.0, q[2], 1e-10);
        assertEquals(0.0, q[3], 1e-10);
    }

    @Test
    public void quaternionNorm_maintainedAcrossManyUpdates() {
        EkfFusion ekf = new EkfFusion();

        float[] accel = {1, 2, G};
        float[] mag = {15, 5, 35};
        float[] gyro = {0.3f, -0.2f, 0.5f};
        float dt = 0.01f;

        for (int i = 0; i < 5000; i++) {
            ekf.predict(gyro, dt);
            if (i % 2 == 0) ekf.correctAccelerometer(accel);
            if (i % 5 == 0) ekf.correctMagnetometer(mag);
        }

        double[] q = ekf.getQuaternion();
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, 1e-5);
    }
}
