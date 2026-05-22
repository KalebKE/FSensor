package com.tracqi.fsensor.math.rotation;

import org.apache.commons.math3.complex.Quaternion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class RotationTest {

    private static final float EPSILON = 1e-5f;
    private static final double DEPSILON = 1e-6;

    @Test
    public void getQuaternion_identityMatrix_returnsIdentityQuaternion() {
        float[] identity = {
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
        };

        double[] q = Rotation.getQuaternion(identity);

        assertEquals(1.0, q[0], DEPSILON); // w
        assertEquals(0.0, q[1], DEPSILON); // x
        assertEquals(0.0, q[2], DEPSILON); // y
        assertEquals(0.0, q[3], DEPSILON); // z
    }

    @Test
    public void getQuaternion_90degAroundZ_returnsCorrectQuaternion() {
        // Rotation of 90 degrees around Z: cos(45)=sin(45)=sqrt(2)/2
        float[] rotZ90 = {
                0, -1, 0,
                1,  0, 0,
                0,  0, 1
        };

        double[] q = Rotation.getQuaternion(rotZ90);

        double expected = Math.sqrt(2.0) / 2.0;
        assertEquals(expected, Math.abs(q[0]), DEPSILON); // w = cos(45)
        assertEquals(0.0, q[1], DEPSILON); // x
        assertEquals(0.0, q[2], DEPSILON); // y
        assertEquals(expected, Math.abs(q[3]), DEPSILON); // z = sin(45)
    }

    @Test
    public void getQuaternion_90degAroundX_returnsCorrectQuaternion() {
        float[] rotX90 = {
                1, 0,  0,
                0, 0, -1,
                0, 1,  0
        };

        double[] q = Rotation.getQuaternion(rotX90);

        double expected = Math.sqrt(2.0) / 2.0;
        assertEquals(expected, Math.abs(q[0]), DEPSILON); // w
        assertEquals(expected, Math.abs(q[1]), DEPSILON); // x
        assertEquals(0.0, q[2], DEPSILON); // y
        assertEquals(0.0, q[3], DEPSILON); // z
    }

    @Test
    public void getQuaternion_180degAroundZ_handlesTraceNearZero() {
        // 180 degrees around Z axis: trace = -1+(-1)+1 = -1, so trace+1 = 0
        float[] rotZ180 = {
                -1,  0, 0,
                 0, -1, 0,
                 0,  0, 1
        };

        double[] q = Rotation.getQuaternion(rotZ180);

        // Should produce w≈0, z≈±1 without NaN
        assertFalse(Double.isNaN(q[0]));
        assertFalse(Double.isNaN(q[1]));
        assertFalse(Double.isNaN(q[2]));
        assertFalse(Double.isNaN(q[3]));

        // Verify unit quaternion
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, DEPSILON);

        // For 180 around Z: w≈0, x≈0, y≈0, z≈±1
        assertEquals(0.0, q[0], 0.01); // w near 0
        assertEquals(0.0, q[1], 0.01); // x near 0
        assertEquals(0.0, q[2], 0.01); // y near 0
        assertEquals(1.0, Math.abs(q[3]), 0.01); // z near ±1
    }

    @Test
    public void getQuaternion_180degAroundX_handlesMaxDiagonalX() {
        // 180 degrees around X: m00=1, m11=-1, m22=-1
        float[] rotX180 = {
                1,  0,  0,
                0, -1,  0,
                0,  0, -1
        };

        double[] q = Rotation.getQuaternion(rotX180);

        assertFalse(Double.isNaN(q[0]));
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, DEPSILON);

        // w≈0, x≈±1, y≈0, z≈0
        assertEquals(0.0, q[0], 0.01);
        assertEquals(1.0, Math.abs(q[1]), 0.01);
        assertEquals(0.0, q[2], 0.01);
        assertEquals(0.0, q[3], 0.01);
    }

    @Test
    public void getQuaternion_180degAroundY_handlesMaxDiagonalY() {
        // 180 degrees around Y: m00=-1, m11=1, m22=-1
        float[] rotY180 = {
                -1, 0,  0,
                 0, 1,  0,
                 0, 0, -1
        };

        double[] q = Rotation.getQuaternion(rotY180);

        assertFalse(Double.isNaN(q[0]));
        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, DEPSILON);

        // w≈0, x≈0, y≈±1, z≈0
        assertEquals(0.0, q[0], 0.01);
        assertEquals(0.0, q[1], 0.01);
        assertEquals(1.0, Math.abs(q[2]), 0.01);
        assertEquals(0.0, q[3], 0.01);
    }

    @Test
    public void getQuaternion_alwaysReturnsUnitQuaternion() {
        // Arbitrary rotation matrix (30 deg around axis [1,1,1]/sqrt(3))
        double angle = Math.toRadians(30);
        double c = Math.cos(angle), s = Math.sin(angle), t = 1 - c;
        double k = 1.0 / Math.sqrt(3.0);
        float[] m = {
                (float)(t*k*k + c),     (float)(t*k*k - k*s),  (float)(t*k*k + k*s),
                (float)(t*k*k + k*s),   (float)(t*k*k + c),    (float)(t*k*k - k*s),
                (float)(t*k*k - k*s),   (float)(t*k*k + k*s),  (float)(t*k*k + c)
        };

        double[] q = Rotation.getQuaternion(m);

        double norm = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        assertEquals(1.0, norm, DEPSILON);
    }

    @Test
    public void integrateGyroscopeRotation_zeroRate_returnsUnchanged() {
        Quaternion identity = Quaternion.IDENTITY;
        float[] zeroRate = {0, 0, 0};

        Quaternion result = Rotation.integrateGyroscopeRotation(identity, zeroRate, 0.01f, 1e-9f);

        assertEquals(1.0, result.getQ0(), DEPSILON);
        assertEquals(0.0, result.getQ1(), DEPSILON);
        assertEquals(0.0, result.getQ2(), DEPSILON);
        assertEquals(0.0, result.getQ3(), DEPSILON);
    }

    @Test
    public void integrateGyroscopeRotation_constantZRotation_accumulatesCorrectly() {
        Quaternion q = Quaternion.IDENTITY;
        float[] rateZ = {0, 0, 1.0f}; // 1 rad/s around Z
        float dt = 0.01f; // 10ms

        // Integrate for 100 steps = 1 second = 1 radian total
        for (int i = 0; i < 100; i++) {
            q = Rotation.integrateGyroscopeRotation(q, rateZ, dt, 1e-9f);
        }

        // After 1 radian around Z: w=cos(0.5), z=sin(0.5)
        double expectedW = Math.cos(0.5);
        double expectedZ = Math.sin(0.5);

        assertEquals(expectedW, q.getQ0(), 0.01);
        assertEquals(0.0, q.getQ1(), 0.01);
        assertEquals(0.0, q.getQ2(), 0.01);
        assertEquals(expectedZ, q.getQ3(), 0.01);
    }

    @Test
    public void integrateGyroscopeRotation_doesNotMutateInput() {
        float[] rate = {1.0f, 2.0f, 3.0f};
        float[] rateCopy = {1.0f, 2.0f, 3.0f};

        Rotation.integrateGyroscopeRotation(Quaternion.IDENTITY, rate, 0.01f, 1e-9f);

        assertArrayEquals(rateCopy, rate, 0f);
    }

    @Test
    public void integrateGyroscopeRotation_maintainsUnitNormOver10000Steps() {
        Quaternion q = Quaternion.IDENTITY;
        float[] rate = {0.5f, 0.3f, 0.7f}; // arbitrary rotation
        float dt = 0.01f;

        for (int i = 0; i < 10000; i++) {
            q = Rotation.integrateGyroscopeRotation(q, rate, dt, 1e-9f);
        }

        double norm = q.getNorm();
        assertEquals(1.0, norm, 1e-4);
    }
}
