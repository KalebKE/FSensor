package com.kircherelectronics.fsensor.filter.gyroscope.fusion.complementary;

import com.kircherelectronics.fsensor.filter.gyroscope.fusion.OrientationFused;
import com.kircherelectronics.fsensor.util.angle.AngleUtils;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;

import org.apache.commons.math3.complex.Quaternion;

/*
 * Copyright 2018, Kircher Electronics, LLC
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
 * OrientationComplimentaryFilter estimates the orientation of the devices based on a sensor fusion of a
 * gyroscope, accelerometer and magnetometer. The fusedOrientation is backed by a quaternion based complimentary fusedOrientation.
 * <p>
 * The complementary fusedOrientation is a frequency domain fusedOrientation. In its strictest
 * sense, the definition of a complementary fusedOrientation refers to the use of two or
 * more transfer functions, which are mathematical complements of one another.
 * Thus, if the data from one sensor is operated on by G(s), then the data from
 * the other sensor is operated on by I-G(s), and the sum of the transfer
 * functions is I, the identity matrix.
 * <p>
 * OrientationComplimentaryFilter attempts to fuse magnetometer, gravity and gyroscope
 * sensors together to produce an accurate measurement of the rotation of the
 * device.
 * <p>
 * The magnetometer and acceleration sensors are used to determine one of the
 * two orientation estimations of the device. This measurement is subject to the
 * constraint that the device must not be accelerating and hard and soft-iron
 * distortions are not present in the local magnetic field..
 * <p>
 * The gyroscope is used to determine the second of two orientation estimations
 * of the device. The gyroscope can have a shorter response time and is not
 * effected by linear acceleration or magnetic field distortions, however it
 * experiences drift and has to be compensated periodically by the
 * acceleration/magnetic sensors to remain accurate.
 * <p>
 * Quaternions are used to integrate the measurements of the gyroscope and apply
 * the rotations to each sensors measurements via complementary fusedOrientation. This the
 * ideal method because quaternions are not subject to many of the singularties
 * of rotation matrices, such as gimbal lock.
 * <p>
 * The quaternion for the magnetic/acceleration sensor is only needed to apply
 * the weighted quaternion to the gyroscopes weighted quaternion via
 * complementary fusedOrientation to produce the fused rotation. No integrations are
 * required.
 * <p>
 * The gyroscope provides the angular rotation speeds for all three axes. To
 * find the orientation of the device, the rotation speeds must be integrated
 * over time. This can be accomplished by multiplying the angular speeds by the
 * time intervals between sensor updates. The calculation produces the rotation
 * increment. Integrating these values again produces the absolute orientation
 * of the device. Small errors are produced at each iteration causing the gyro
 * to drift away from the true orientation.
 * <p>
 * To eliminate both the drift and noise from the orientation, the gyroscope
 * measurements are applied only for orientation changes in short time
 * intervals. The magnetometer/acceleration fusion is used for long time
 * intervals. This is equivalent to low-pass filtering of the accelerometer and
 * magnetic field sensor signals and high-pass filtering of the gyroscope
 * signals.
 *
 * @author Kaleb
 *         http://developer.android.com/reference/android/hardware/SensorEvent.html#values
 */
public class OrientationFusedComplementary extends OrientationFused {

    private static final String TAG = OrientationFusedComplementary.class.getSimpleName();

    /**
     * Initialize a singleton instance.
     */
    public OrientationFusedComplementary() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public OrientationFusedComplementary(float timeConstant) {
        super(timeConstant);
    }

    /**
     * Calculate the fused orientation of the device.
     *
     * Rotation is positive in the counterclockwise direction (right-hand rule). That is, an observer looking from some positive location on the x, y, or z axis at
     * a device positioned on the origin would report positive rotation if the device appeared to be rotating counter clockwise. Note that this is the
     * standard mathematical definition of positive rotation and does not agree with the aerospace definition of roll.
     *
     * See: https://source.android.com/devices/sensors/sensor-types#rotation_vector
     *
     * Returns a vector of size 3 ordered as:
     * [0]X points east and is tangential to the ground.
     * [1]Y points north and is tangential to the ground.
     * [2]Z points towards the sky and is perpendicular to the ground.
     *
     * @param gyroscope the gyroscope measurements.
     * @param timestamp the gyroscope timestamp
     * @return An orientation vector -> @link SensorManager#getOrientation(float[], float[])}
     */
    public float[] calculateFusedOrientation(float[] gyroscope, long timestamp, float[] acceleration, float[] magnetic) {
        if (isBaseOrientationSet()) {
            if (this.timestamp != 0) {
                final float dT = (timestamp - this.timestamp) * NS2S;

                float alpha = timeConstant / (timeConstant + dT);
                float oneMinusAlpha = (1.0f - alpha);

                Quaternion rotationVectorAccelerationMagnetic = RotationUtil.getOrientationVectorFromAccelerationMagnetic(acceleration, magnetic);

                if(rotationVectorAccelerationMagnetic != null) {

                    rotationVector  = RotationUtil.integrateGyroscopeRotation(rotationVector, gyroscope, dT, EPSILON);

                    // Apply the complementary fusedOrientation. // We multiply each rotation by their
                    // coefficients (scalar matrices)...
                    Quaternion scaledRotationVectorAccelerationMagnetic = rotationVectorAccelerationMagnetic.multiply(oneMinusAlpha);

                    // Scale our quaternion for the gyroscope
                    Quaternion scaledRotationVectorGyroscope = rotationVector.multiply(alpha);

                    //...and then add the two quaternions together.
                    // output[0] = alpha * output[0] + (1 - alpha) * input[0];
                    Quaternion result = scaledRotationVectorGyroscope.add(scaledRotationVectorAccelerationMagnetic);

                    output = AngleUtils.getAngles(result.getQ0(), result.getQ1(), result.getQ2(), result.getQ3());
                }
            }

            this.timestamp = timestamp;

            return output;
        } else {
            throw new IllegalStateException("You must call setBaseOrientation() before calling calculateFusedOrientation()!");
        }
    }
}
