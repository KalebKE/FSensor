package com.tracqi.fsensor.rotation.fusion.mahony;

import org.junit.Test;

import static org.junit.Assert.*;

public class MahonyFusionTest {

    private static final float G = 9.81f;

    @Test
    public void update_stationary_convergesFromIdentity() {
        MahonyFusion fusion = new MahonyFusion(2.0f, 0.1f);

        float[] accel = {0, 0, G};
        float[] mag = {20, 0, 40};
        float[] gyro = {0, 0, 0};
        float dt = 0.01f;

        for (int i = 0; i < 500; i++) {
            fusion.update(accel, mag, gyro, dt);
        }

        double[] q = fusion.getQuaternion();
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, 1e-6);
        assertTrue("Should converge near identity for flat device", Math.abs(q[0]) > 0.9);
    }

    @Test
    public void update_integralTerm_compensatesGyroBias() {
        MahonyFusion fusionWithKi = new MahonyFusion(1.0f, 0.5f);
        MahonyFusion fusionNoKi = new MahonyFusion(1.0f, 0.0f);

        float[] accel = {0, 0, G};
        float[] mag = {20, 0, 40};
        // Simulated gyro bias
        float[] biasedGyro = {0.1f, 0.05f, 0.0f};
        float dt = 0.01f;

        for (int i = 0; i < 2000; i++) {
            fusionWithKi.update(accel, mag, biasedGyro, dt);
            fusionNoKi.update(accel, mag, biasedGyro, dt);
        }

        double[] qWithKi = fusionWithKi.getQuaternion();
        double[] qNoKi = fusionNoKi.getQuaternion();

        // With Ki, the integral term should compensate the bias better
        // The filter with Ki should be closer to identity (since device is stationary)
        double distWithKi = Math.sqrt((1-qWithKi[0])*(1-qWithKi[0]) + qWithKi[1]*qWithKi[1] + qWithKi[2]*qWithKi[2] + qWithKi[3]*qWithKi[3]);
        double distNoKi = Math.sqrt((1-qNoKi[0])*(1-qNoKi[0]) + qNoKi[1]*qNoKi[1] + qNoKi[2]*qNoKi[2] + qNoKi[3]*qNoKi[3]);

        assertTrue("Ki should help compensate gyro bias, distWithKi=" + distWithKi + " distNoKi=" + distNoKi,
                distWithKi < distNoKi);
    }

    @Test
    public void update_higherKp_fasterResponse() {
        MahonyFusion slowFusion = new MahonyFusion(0.1f, 0.0f);
        MahonyFusion fastFusion = new MahonyFusion(5.0f, 0.0f);

        float[] accel = {0, G, 0}; // device tilted 90 degrees
        float[] gyro = {0, 0, 0};
        float dt = 0.01f;

        for (int i = 0; i < 50; i++) {
            slowFusion.update(accel, null, gyro, dt);
            fastFusion.update(accel, null, gyro, dt);
        }

        double[] qSlow = slowFusion.getQuaternion();
        double[] qFast = fastFusion.getQuaternion();

        double distSlow = Math.abs(1.0 - qSlow[0]);
        double distFast = Math.abs(1.0 - qFast[0]);
        assertTrue("Higher Kp should respond faster", distFast > distSlow);
    }

    @Test
    public void update_6dofMode_worksWithoutMag() {
        MahonyFusion fusion = new MahonyFusion(2.0f, 0.0f);

        float[] accel = {0, 0, G};
        float[] gyro = {0, 0, 0};
        float dt = 0.01f;

        for (int i = 0; i < 100; i++) {
            fusion.update(accel, null, gyro, dt);
        }

        double[] q = fusion.getQuaternion();
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, 1e-6);
    }

    @Test
    public void reset_clearsState() {
        MahonyFusion fusion = new MahonyFusion(2.0f, 0.5f);

        float[] accel = {G, 0, 0};
        float[] gyro = {1, 1, 1};
        fusion.update(accel, null, gyro, 0.1f);

        fusion.reset();
        double[] q = fusion.getQuaternion();
        assertEquals(1.0, q[0], 1e-10);
        assertEquals(0.0, q[1], 1e-10);
        assertEquals(0.0, q[2], 1e-10);
        assertEquals(0.0, q[3], 1e-10);
    }
}
