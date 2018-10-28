package com.kircherelectronics.fsensor.util.rotation;

import android.hardware.SensorManager;

import org.apache.commons.math3.complex.Quaternion;

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
     * @param acceleration the acceleration measurement.
     * @param magnetic the magnetic measurement.
     * @return {@link SensorManager#getOrientation(float[], float[])}
     */
    public static float[] getOrientationVectorFromAccelerationMagnetic(float[] acceleration, float[] magnetic) {
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magnetic)) {
            float[] baseOrientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, baseOrientation);

            return baseOrientation;
        }

        return null;
    }

    /**
     * Calculates orientation vector from accelerometer and magnetometer output.
     * @param acceleration the acceleration measurement.
     * @param magnetic the magnetic measurement.
     * @return A Quaternion representation of the vector returned by {@link SensorManager#getOrientation(float[], float[])}
     */
    public static Quaternion getOrientationQuaternionFromAccelerationMagnetic(float[] acceleration, float[] magnetic) {
        return vectorToQuaternion(getOrientationVectorFromAccelerationMagnetic(acceleration, magnetic));
    }

    /**
     * Create an quaternion vector, in this case a unit quaternion, from the
     * provided Euler angle's (presumably from SensorManager.getFusedOrientation()).
     * <p>
     *
     * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/">Equation</a>
     * @param vector The vector to convert to a Quaternion
     * @return A Quaternion representation of the vector.
     */
    public static Quaternion vectorToQuaternion(float[] vector) {
        if (vector != null) {
            // Assuming the angles are in radians.

            // getFusedOrientation() values:
            // values[0]: azimuth, rotation around the Z axis.
            // values[1]: pitch, rotation around the X axis.
            // values[2]: roll, rotation around the Y axis.

            // Heading, AzimuthUtil, Yaw
            double c1 = Math.cos(vector[0] / 2);
            double s1 = Math.sin(vector[0] / 2);

            // Pitch, Attitude
            // The equation assumes the pitch is pointed in the opposite direction
            // of the orientation vector provided by Android, so we invert it.
            double c2 = Math.cos(vector[1] / 2);
            double s2 = Math.sin(vector[1] / 2);

            // Roll, Bank
            double c3 = Math.cos(vector[2] / 2);
            double s3 = Math.sin(vector[2] / 2);

            double c1c2 = c1 * c2;
            double s1s2 = s1 * s2;

            double w = c1c2 * c3 - s1s2 * s3;
            double x = c1c2 * s3 + s1s2 * c3;
            double y = s1 * c2 * c3 + c1 * s2 * s3;
            double z = c1 * s2 * c3 - s1 * c2 * s3;

            // The quaternion in the equation does not share the same coordinate
            // system as the Android gyroscope quaternion we are using. We reorder
            // it here.

            // Android X (pitch) = Equation Z (pitch)
            // Android Y (roll) = Equation X (roll)
            // Android Z (azimuth) = Equation Y (azimuth)

            // Note that the gyroscope sensor reports the rotation as positive in the counter-clockwise direction so we invert.
            return new Quaternion(w, -z, -x, -y);
        }

        return null;
    }
}
