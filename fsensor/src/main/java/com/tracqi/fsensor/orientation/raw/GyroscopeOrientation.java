package com.tracqi.fsensor.orientation.raw;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.tracqi.fsensor.orientation.Orientation;
import com.tracqi.fsensor.util.angle.AngleUtils;
import com.tracqi.fsensor.util.rotation.RotationUtil;

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
 * @author Kaleb
 */
public class GyroscopeOrientation implements Orientation {
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 0.000000001f;
    private Quaternion rotationQuaternion = Quaternion.IDENTITY;
    private final float[] rotation = new float[3];
    private final SensorEventListener sensorEventListener = new SensorListener();
    private long timestamp = 0;
    private final SensorManager sensorManager;

    /**
     * Initialize a singleton instance.
     */
    public GyroscopeOrientation(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    @Override
    public void start(int sensorDelay) {
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensorDelay);
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public float[] getOrientation() {
        return rotation;
    }

    /**
     * Set the base orientation (frame of reference) to which all subsequent rotations will be applied.
     * <p>
     * To initialize to an arbitrary local frame of reference pass in the Identity Quaternion. This will initialize the base orientation as the orientation the device is
     * currently in and all subsequent rotations will be relative to this orientation.
     * <p>
     * To initialize to an absolute frame of reference (like Earth frame) the devices orientation must be determine from other sensors (such as the acceleration and magnetic
     * sensors).
     *
     * @param baseOrientation The base orientation to which all subsequent rotations will be applied.
     */
    public void setBaseOrientation(Quaternion baseOrientation) {
        rotationQuaternion = baseOrientation;
    }

    /**
     * Calculate the fused orientation of the device.
     * <p>
     * Rotation is positive in the counterclockwise direction (right-hand rule). That is, an observer looking from some positive location on the x, y, or z axis at
     * a device positioned on the origin would report positive rotation if the device appeared to be rotating counter clockwise. Note that this is the
     * standard mathematical definition of positive rotation and does not agree with the aerospace definition of roll.
     * <p>
     * Returns a vector of size 3 ordered as:
     * [0]X points east and is tangential to the ground.
     * [1]Y points north and is tangential to the ground.
     * [2]Z points towards the sky and is perpendicular to the ground.
     *
     * @param gyroscope the gyroscope measurements.
     * @param timestamp the gyroscope timestamp
     */
    private void calculateOrientation(float[] gyroscope, long timestamp) {
        float[] angles;
        if (this.timestamp != 0) {
            final float dT = (timestamp - this.timestamp) * NS2S;
            rotationQuaternion = RotationUtil.integrateGyroscopeRotation(rotationQuaternion, gyroscope, dT, EPSILON);
            angles = AngleUtils.getAngles(rotationQuaternion.getQ0(), rotationQuaternion.getQ1(), rotationQuaternion.getQ2(), rotationQuaternion.getQ3());

            rotation[0] = angles[0];
            rotation[1] = angles[1];
            rotation[2] = angles[2];
        }

        this.timestamp = timestamp;
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                calculateOrientation(event.values, event.timestamp);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
