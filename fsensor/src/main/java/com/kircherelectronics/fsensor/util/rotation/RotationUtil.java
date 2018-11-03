package com.kircherelectronics.fsensor.util.rotation;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import java.util.Arrays;

/**
 * Created by kaleb on 4/1/18.
 */

public class RotationUtil {

    /**
     * Calculates a rotation vector from the gyroscope angular speed values.
     * <p>
     * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
     *
     * @param previousRotationVector the last known orientation to which the new rotation will be applied.
     * @param rateOfRotation         the rotation measurement
     * @param dt                     the period of time over which the rotation measurement took place in units of seconds
     * @param epsilon                minimum rotation vector magnitude required to get the axis for normalization
     * @return A Quaternion representing the orientation.
     */
    public static Quaternion integrateGyroscopeRotation(Quaternion previousRotationVector, float[] rateOfRotation, float dt, float epsilon) {
        // Calculate the angular speed of the sample
        float magnitude = (float) Math.sqrt(Math.pow(rateOfRotation[0], 2)
                + Math.pow(rateOfRotation[1], 2) + Math.pow(rateOfRotation[2], 2));

        // Normalize the rotation vector if it's big enough to get the axis
        if (magnitude > epsilon) {
            rateOfRotation[0] /= magnitude;
            rateOfRotation[1] /= magnitude;
            rateOfRotation[2] /= magnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = magnitude * dt / 2.0f;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

        double[] deltaVector = new double[4];

        deltaVector[0] = sinThetaOverTwo * rateOfRotation[0];
        deltaVector[1] = sinThetaOverTwo * rateOfRotation[1];
        deltaVector[2] = sinThetaOverTwo * rateOfRotation[2];
        deltaVector[3] = cosThetaOverTwo;

        // Since it is a unit quaternion, we can just multiply the old rotation
        // by the new rotation delta to integrate the rotation.
        return previousRotationVector.multiply(new Quaternion(deltaVector[3], Arrays.copyOfRange(
                deltaVector, 0, 3)));
    }

    /**
     * Calculates orientation vector from accelerometer and magnetometer output.
     *
     * @param acceleration the acceleration measurement.
     * @param magnetic     the magnetic measurement.
     * @return
     */
    public static Quaternion getOrientationVectorFromAccelerationMagnetic(float[] acceleration, float[] magnetic) {
        float[] rotationMatrix = new float[9];
        if (getRotationMatrix(rotationMatrix, null, acceleration, magnetic)) {
            Rotation rotation = new Rotation(convertTo2DArray(rotationMatrix), 0.1);
            return new Quaternion(rotation.getQ0(), rotation.getQ1(), rotation.getQ2(), rotation.getQ3());
        }

        return null;
    }

    private static double[][] convertTo2DArray(float[] rotation) {
        if (rotation.length != 9) {
            throw new IllegalStateException("Length must be of 9! Length: " + rotation.length);
        }

        double[][] rm = new double[3][3];

        rm[0][0] = rotation[0];
        rm[0][1] = rotation[1];
        rm[0][2] = rotation[2];
        rm[1][0] = rotation[3];
        rm[1][1] = rotation[4];
        rm[1][2] = rotation[5];
        rm[2][0] = rotation[6];
        rm[2][1] = rotation[7];
        rm[2][2] = rotation[8];

        return rm;
    }


    private static boolean getRotationMatrix(float[] R, float[] I, float[] gravity, float[] geomagnetic) {
        float Ax = gravity[0];
        float Ay = gravity[1];
        float Az = gravity[2];
        final float normsqA = (Ax * Ax + Ay * Ay + Az * Az);
        final float g = 9.81f;
        final float freeFallGravitySquared = 0.01f * g * g;
        if (normsqA < freeFallGravitySquared) {
            // gravity less than 10% of normal value
            return false;
        }
        final float Ex = geomagnetic[0];
        final float Ey = geomagnetic[1];
        final float Ez = geomagnetic[2];
        float Hx = Ey * Az - Ez * Ay;
        float Hy = Ez * Ax - Ex * Az;
        float Hz = Ex * Ay - Ey * Ax;
        final float normH = (float) Math.sqrt(Hx * Hx + Hy * Hy + Hz * Hz);
        if (normH < 0.1f) {
            // device is close to free fall (or in space?), or close to
            // magnetic north pole. Typical values are  > 100.
            return false;
        }
        final float invH = 1.0f / normH;
        Hx *= invH;
        Hy *= invH;
        Hz *= invH;
        final float invA = 1.0f / (float) Math.sqrt(Ax * Ax + Ay * Ay + Az * Az);
        Ax *= invA;
        Ay *= invA;
        Az *= invA;
        final float Mx = Ay * Hz - Az * Hy;
        final float My = Az * Hx - Ax * Hz;
        final float Mz = Ax * Hy - Ay * Hx;
        if (R != null) {
            if (R.length == 9) {
                R[0] = Hx;
                R[1] = Hy;
                R[2] = Hz;
                R[3] = Mx;
                R[4] = My;
                R[5] = Mz;
                R[6] = Ax;
                R[7] = Ay;
                R[8] = Az;
            } else if (R.length == 16) {
                R[0] = Hx;
                R[1] = Hy;
                R[2] = Hz;
                R[3] = 0;
                R[4] = Mx;
                R[5] = My;
                R[6] = Mz;
                R[7] = 0;
                R[8] = Ax;
                R[9] = Ay;
                R[10] = Az;
                R[11] = 0;
                R[12] = 0;
                R[13] = 0;
                R[14] = 0;
                R[15] = 1;
            }
        }
        if (I != null) {
            // compute the inclination matrix by projecting the geomagnetic
            // vector onto the Z (gravity) and X (horizontal component
            // of geomagnetic vector) axes.
            final float invE = 1.0f / (float) Math.sqrt(Ex * Ex + Ey * Ey + Ez * Ez);
            final float c = (Ex * Mx + Ey * My + Ez * Mz) * invE;
            final float s = (Ex * Ax + Ey * Ay + Ez * Az) * invE;
            if (I.length == 9) {
                I[0] = 1;
                I[1] = 0;
                I[2] = 0;
                I[3] = 0;
                I[4] = c;
                I[5] = s;
                I[6] = 0;
                I[7] = -s;
                I[8] = c;
            } else if (I.length == 16) {
                I[0] = 1;
                I[1] = 0;
                I[2] = 0;
                I[4] = 0;
                I[5] = c;
                I[6] = s;
                I[8] = 0;
                I[9] = -s;
                I[10] = c;
                I[3] = I[7] = I[11] = I[12] = I[13] = I[14] = 0;
                I[15] = 1;
            }
        }
        return true;
    }
}
