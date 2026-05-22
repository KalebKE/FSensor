package com.tracqi.fsensor.rotation.fusion.madgwick;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.tracqi.fsensor.math.angle.Angles;
import com.tracqi.fsensor.rotation.Rotation;
import com.tracqi.fsensor.rotation.fusion.FusionAlgorithm;

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
 * Madgwick's AHRS (Attitude and Heading Reference System) algorithm.
 * <p>
 * Uses gradient descent to compute the orientation quaternion from
 * accelerometer, magnetometer, and gyroscope data. Computationally
 * efficient (no matrix inversions) and well-suited for embedded systems.
 * <p>
 * Reference: S.O.H. Madgwick, "An efficient orientation filter for
 * inertial and inertial/magnetic sensor arrays" (2010).
 */
public class MadgwickRotation implements Rotation, FusionAlgorithm {

    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float DEFAULT_BETA = 0.033f;

    private final SensorManager sensorManager;
    private final SensorEventListener sensorEventListener = new SensorListener();
    private final MadgwickFusion fusion;

    private final float[] acceleration = new float[3];
    private final float[] magnetic = new float[3];
    private final float[] gyroscope = new float[3];
    private final float[] output = new float[3];

    private long timestamp;

    public MadgwickRotation(SensorManager sensorManager) {
        this(sensorManager, DEFAULT_BETA);
    }

    public MadgwickRotation(SensorManager sensorManager, float beta) {
        this.sensorManager = sensorManager;
        this.fusion = new MadgwickFusion(beta);
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
        return output;
    }

    @Override
    public void update(float[] acceleration, float[] magnetic, float[] gyroscope, float dt) {
        fusion.update(acceleration, magnetic, gyroscope, dt);
        double[] q = fusion.getQuaternion();
        float[] angles = Angles.getAngles(q[0], q[1], q[2], q[3]);
        System.arraycopy(angles, 0, output, 0, 3);
    }

    @Override
    public double[] getQuaternion() {
        return fusion.getQuaternion();
    }

    @Override
    public void reset() {
        fusion.reset();
    }

    private class SensorListener implements SensorEventListener {
        private boolean hasAcceleration = false;
        private boolean hasRotation = false;
        private boolean hasMagnetic = false;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, acceleration, 0, 3);
                hasAcceleration = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetic, 0, 3);
                hasMagnetic = true;
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                System.arraycopy(event.values, 0, gyroscope, 0, 3);
                hasRotation = true;
            }

            if (hasAcceleration && hasRotation && hasMagnetic) {
                hasAcceleration = false;
                hasRotation = false;
                hasMagnetic = false;

                if (timestamp != 0) {
                    float dt = (event.timestamp - timestamp) * NS2S;
                    update(acceleration, magnetic, gyroscope, dt);
                }
                timestamp = event.timestamp;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
