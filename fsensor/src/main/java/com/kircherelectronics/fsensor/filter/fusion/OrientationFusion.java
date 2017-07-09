package com.kircherelectronics.fsensor.filter.fusion;

import android.hardware.SensorManager;

import com.kircherelectronics.fsensor.filter.BaseFilter;

import org.apache.commons.math3.complex.Quaternion;

import java.util.Arrays;

/*
 * Copyright 2017, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A base implementation for an orientation IMU sensor fusion.
 * Created by kaleb on 7/6/17.
 */

public abstract class OrientationFusion implements BaseFilter {

    private static final float EPSILON = 0.000000001f;
    // Nano-second to second conversion
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final String tag = OrientationFusion.class.getSimpleName();
    public static float DEFAULT_TIME_CONSTANT = 0.18f;
    // The coefficient for the filter... 0.5 = means it is averaging the two
    // transfer functions (rotations from the gyroscope and
    // acceleration/magnetic, respectively).
    public float timeConstant;
    protected Quaternion rotationVectorGyroscope;

    // accelerometer vector
    private float[] acceleration;
    private boolean accelerationUpdated;
    // magnetic field vector
    private float[] magnetic;
    private boolean magneticUpdated;
    private long timestamp;

    /**
     * Initialize a singleton instance.
     */
    public OrientationFusion() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public OrientationFusion(float timeConstant) {
        this.timeConstant = timeConstant;

        reset();
    }

    /**
     * Set the rate of rotation from the gyroscope.
     *
     * @param values
     */
    public float[] filter(float[] values) {
        return getFusedOrientation(values);
    }

    public void reset() {
        accelerationUpdated = false;
        magneticUpdated = false;
        timestamp = 0;
        magnetic = new float[3];
        acceleration = new float[3];
        rotationVectorGyroscope = null;
    }

    public void setAcceleration(float[] acceleration) {
        // Get a local copy of the raw magnetic values from the device sensor.
        this.acceleration = Arrays.copyOf(acceleration, acceleration.length);;
        this.accelerationUpdated = true;
    }

    public void setMagneticField(float[] magnetic) {
        this.magnetic = magnetic;
        this.magneticUpdated = true;
    }

    /**
     * The complementary filter coefficient, a floating point value between 0-1,
     * exclusive of 0, inclusive of 1.
     *
     * @param timeConstant
     */
    public void setTimeConstant(float timeConstant) {
        this.timeConstant = timeConstant;
    }

    public abstract void startFusion();
    public abstract void stopFusion();
    /**
     * Calculate the fused orientation.
     */
    protected abstract float[] calculateFusedOrientation(float[] gyroscope, float dt, float[] acceleration, float[]
            magnetic);

    /**
     * Create an angle-axis vector, in this case a unit quaternion, from the
     * provided Euler angle's (presumably from SensorManager.getFusedOrientation()).
     * <p>
     * Equation from
     * http://www.euclideanspace.com/maths/geometry/rotations/conversions
     * /eulerToQuaternion/
     *
     * @param orientation
     */
    protected Quaternion getAccelerationMagneticRotationVector(float[] orientation) {
        // Assuming the angles are in radians.

        // getFusedOrientation() values:
        // values[0]: azimuth, rotation around the Z axis.
        // values[1]: pitch, rotation around the X axis.
        // values[2]: roll, rotation around the Y axis.

        // Heading, Azimuth, Yaw
        double c1 = Math.cos(orientation[0] / 2);
        double s1 = Math.sin(orientation[0] / 2);

        // Pitch, Attitude
        // The equation assumes the pitch is pointed in the opposite direction
        // of the orientation vector provided by Android, so we invert it.
        double c2 = Math.cos(-orientation[1] / 2);
        double s2 = Math.sin(-orientation[1] / 2);

        // Roll, Bank
        double c3 = Math.cos(orientation[2] / 2);
        double s3 = Math.sin(orientation[2] / 2);

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

        return new Quaternion(w, z, x, y);
    }

    /**
     * Calculates orientation angles from accelerometer and magnetometer output.
     */
    protected float[] getBaseOrientation(float[] acceleration, float[] magnetic) {
        // To get the orientation vector from the acceleration and magnetic
        // sensors, we let Android do the heavy lifting. This call will
        // automatically compensate for the tilt of the compass and fail if the
        // magnitude of the acceleration is not close to 9.82m/sec^2. You could
        // perform these steps yourself, but in my opinion, this is the best way
        // to do it.
        float[] rotationMatrix = new float[9];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, acceleration, magnetic)) {
            float[] baseOrientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, baseOrientation);
            return baseOrientation;
        }

        return null;
    }

    /**
     * Calculates a rotation vector from the gyroscope angular speed values.
     * <p>
     * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
     *
     * @param dt
     */
    protected Quaternion getGyroscopeRotationVector(Quaternion previousRotationVector, float[] rateOfRotation,
                                                    float dt) {
        // Calculate the angular speed of the sample
        float magnitude = (float) Math.sqrt(Math.pow(rateOfRotation[0], 2)
                + Math.pow(rateOfRotation[1], 2) + Math.pow(rateOfRotation[2], 2));

        // Normalize the rotation vector if it's big enough to get the axis
        if (magnitude > EPSILON) {
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

    protected void initializeRotationVectorGyroscopeIfRequired(Quaternion rotationVectorAccelerationMagnetic) {
        rotationVectorGyroscope = new Quaternion(rotationVectorAccelerationMagnetic.getScalarPart(),
                    rotationVectorAccelerationMagnetic.getVectorPart());
    }

    private float[] getFusedOrientation(float[] gyroscope) {
        long timestamp = System.nanoTime();
        if (accelerationUpdated && magneticUpdated) {
            if(this.timestamp == 0) {
                this.timestamp = System.nanoTime();
            }
            float dt = (timestamp - this.timestamp) * NS2S;
            this.timestamp = timestamp;
            return calculateFusedOrientation(gyroscope, dt, acceleration, magnetic);
        }

        return new float[3];
    }
}
