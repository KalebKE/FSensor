package com.tracqi.fsensor.rotation.fusion.complementary;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.tracqi.fsensor.math.gravity.Gravity;
import com.tracqi.fsensor.rotation.fusion.FusedRotation;
import com.tracqi.fsensor.math.angle.Angles;
import com.tracqi.fsensor.math.rotation.Rotation;

import org.apache.commons.math3.complex.Quaternion;

/*
 * Copyright 2024, Tracqi Technology, LLC
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
 */
public class ComplimentaryRotation extends FusedRotation {

    private static final String TAG = ComplimentaryRotation.class.getSimpleName();

    private static final float DEFAULT_TIME_CONSTANT = 0.18f;

    private final SensorManager sensorManager;
    private final SensorEventListener sensorEventListener = new SensorListener();

    private final float[] acceleration = new float[3];
    private final float[] magnetic = new float[3];
    private final float[] rotation = new float[3];

    private final float[] output = new float[3];

    private long rotationTimestamp;

    private long timestamp;

    // The coefficient for the fusedOrientation... 0.5 = means it is averaging the two
    // transfer functions (rotations from the gyroscope and
    // acceleration/magnetic, respectively).
    public float timeConstant = DEFAULT_TIME_CONSTANT;

    /**
     * Initialize a singleton instance.
     */
    public ComplimentaryRotation(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public ComplimentaryRotation(SensorManager sensorManager, float timeConstant) {
        this.sensorManager = sensorManager;
        this.timeConstant = timeConstant;
    }

    @Override
    public void start(int sensorDelay) {
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorDelay);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorDelay);
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public float[] getOrientation() {
        return this.output;
    }

    private void copyAcceleration(float[] rawAcceleration) {
        System.arraycopy(rawAcceleration, 0, this.acceleration, 0, this.acceleration.length);
    }

    private void copyMagnetic(float[] magnetic) {
        System.arraycopy(magnetic, 0, this.magnetic, 0, this.magnetic.length);
    }

    private void copyRotation(float[] rotation) {
        System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.length);
    }


    /**
     * Calculate the fused orientation of the device.
     * <p>
     * Rotation is positive in the counterclockwise direction (right-hand rule). That is, an observer looking from some positive location on the x, y, or z axis at
     * a device positioned on the origin would report positive rotation if the device appeared to be rotating counter clockwise. Note that this is the
     * standard mathematical definition of positive rotation and does not agree with the aerospace definition of roll.
     * <p>
     * See: https://source.android.com/devices/sensors/sensor-types#rotation_vector
     * <p>
     * Returns a vector of size 3 ordered as:
     * [0]X points east and is tangential to the ground.
     * [1]Y points north and is tangential to the ground.
     * [2]Z points towards the sky and is perpendicular to the ground.
     *
     * @param gyroscope the gyroscope measurements.
     * @param timestamp the gyroscope timestamp
     * @return An orientation vector -> @link SensorManager#getOrientation(float[], float[])}
     */
    private void calculateFusedOrientation(float[] gyroscope, long timestamp, float[] acceleration, float[] magnetic) {
        if (isBaseOrientationSet()) {
            if (this.timestamp != 0) {
                final float dT = (timestamp - this.timestamp) * NS2S;

                float alpha = timeConstant / (timeConstant + dT);
                float oneMinusAlpha = (1.0f - alpha);

                // Get last known orientation
                float[] orientation = Angles.getAngles(rotationVector.getQ0(), rotationVector.getQ1(), rotationVector.getQ2(), rotationVector.getQ3());

                // Calculate the gravity vector from the orientation
                float[] gravity = Gravity.getGravityFromOrientation(orientation);

                for(int i = 0; i < gravity.length; i++) {
                    // Apply acceleration sensor
                    // output[0] = alpha * output[0] + (1 - alpha) * input[0];
                    gravity[i] = alpha * gravity[i] + oneMinusAlpha * acceleration[i];
                }

                // Get orientation from acceleration and magnetic
                Quaternion rotationVectorAccelerationMagnetic = Rotation.getOrientationVector(gravity, magnetic);

                if (rotationVectorAccelerationMagnetic != null) {

                    rotationVector = Rotation.integrateGyroscopeRotation(rotationVector, gyroscope, dT, EPSILON);

                    // Apply the complementary fusedOrientation. // We multiply each rotation by their
                    // coefficients (scalar matrices)...
                    Quaternion scaledRotationVectorAccelerationMagnetic = rotationVectorAccelerationMagnetic.multiply(oneMinusAlpha);

                    // Scale our quaternion for the gyroscope
                    Quaternion scaledRotationVectorGyroscope = rotationVector.multiply(alpha);

                    //...and then add the two quaternions together.
                    // output[0] = alpha * output[0] + (1 - alpha) * input[0];
                    Quaternion result = scaledRotationVectorGyroscope.add(scaledRotationVectorAccelerationMagnetic);

                    float[] angles = Angles.getAngles(result.getQ0(), result.getQ1(), result.getQ2(), result.getQ3());
                    System.arraycopy(angles, 0, this.output, 0, angles.length);
                }
            }

            this.timestamp = timestamp;
        } else {
            throw new IllegalStateException("You must call setBaseOrientation() before calling calculateFusedOrientation()!");
        }
    }

    private class SensorListener implements SensorEventListener {
        private boolean hasAcceleration = false;
        private boolean hasRotation = false;
        private boolean hasMagnetic = false;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                copyAcceleration(event.values);
                hasAcceleration = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                copyMagnetic(event.values);
                hasMagnetic = true;
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                copyRotation(event.values);
                rotationTimestamp = event.timestamp;
                hasRotation = true;
            }

            if (hasAcceleration && hasRotation && hasMagnetic) {
                hasAcceleration = false;
                hasRotation = false;
                hasMagnetic = false;

                if (!isBaseOrientationSet()) {
                    setBaseOrientation(Rotation.getOrientationVector(acceleration, magnetic));
                } else {
                    calculateFusedOrientation(rotation, rotationTimestamp, acceleration, magnetic);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
