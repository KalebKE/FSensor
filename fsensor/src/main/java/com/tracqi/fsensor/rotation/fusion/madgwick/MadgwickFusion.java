package com.tracqi.fsensor.rotation.fusion.madgwick;

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
 * Pure-math implementation of the Madgwick AHRS algorithm.
 * No Android dependencies — suitable for unit testing and KMP migration.
 * <p>
 * Implements the 9DOF MARG (Magnetic, Angular Rate, Gravity) version
 * with gradient descent optimization.
 */
public class MadgwickFusion {

    private final float beta;

    private double q0 = 1.0, q1 = 0.0, q2 = 0.0, q3 = 0.0;

    public MadgwickFusion(float beta) {
        this.beta = beta;
    }

    /**
     * Update the orientation estimate with new sensor data.
     *
     * @param accel     accelerometer [x, y, z] in m/s² (does not need to be normalized)
     * @param mag       magnetometer [x, y, z] in µT (may be null for 6DOF IMU mode)
     * @param gyro      gyroscope [x, y, z] in rad/s
     * @param dt        time step in seconds
     */
    public void update(float[] accel, float[] mag, float[] gyro, float dt) {
        double gx = gyro[0], gy = gyro[1], gz = gyro[2];

        // Normalize accelerometer
        double ax = accel[0], ay = accel[1], az = accel[2];
        double normA = Math.sqrt(ax * ax + ay * ay + az * az);
        if (normA < 1e-10) return;
        ax /= normA; ay /= normA; az /= normA;

        if (mag != null) {
            updateMARG(ax, ay, az, mag, gx, gy, gz, dt);
        } else {
            updateIMU(ax, ay, az, gx, gy, gz, dt);
        }
    }

    private void updateMARG(double ax, double ay, double az, float[] mag,
                            double gx, double gy, double gz, float dt) {
        double mx = mag[0], my = mag[1], mz = mag[2];
        double normM = Math.sqrt(mx * mx + my * my + mz * mz);
        if (normM < 1e-10) {
            updateIMU(ax, ay, az, gx, gy, gz, dt);
            return;
        }
        mx /= normM; my /= normM; mz /= normM;

        // Auxiliary variables to avoid repeated arithmetic
        double _2q0 = 2.0 * q0, _2q1 = 2.0 * q1, _2q2 = 2.0 * q2, _2q3 = 2.0 * q3;
        double _2q0q2 = 2.0 * q0 * q2, _2q2q3 = 2.0 * q2 * q3;
        double q0q0 = q0 * q0, q0q1 = q0 * q1, q0q2 = q0 * q2, q0q3 = q0 * q3;
        double q1q1 = q1 * q1, q1q2 = q1 * q2, q1q3 = q1 * q3;
        double q2q2 = q2 * q2, q2q3 = q2 * q3, q3q3 = q3 * q3;

        // Reference direction of Earth's magnetic field
        double hx = mx * (q0q0 + q1q1 - q2q2 - q3q3) + 2.0 * my * (q1q2 - q0q3) + 2.0 * mz * (q1q3 + q0q2);
        double hy = 2.0 * mx * (q1q2 + q0q3) + my * (q0q0 - q1q1 + q2q2 - q3q3) + 2.0 * mz * (q2q3 - q0q1);
        double _2bx = Math.sqrt(hx * hx + hy * hy);
        double _2bz = 2.0 * mx * (q1q3 - q0q2) + 2.0 * my * (q2q3 + q0q1) + mz * (q0q0 - q1q1 - q2q2 + q3q3);

        // Gradient descent corrective step
        double s0 = -_2q2 * (2.0 * q1q3 - _2q0q2 - ax)
                + _2q1 * (2.0 * q0q1 + _2q2q3 - ay)
                - _2bz * q2 * (_2bx * (0.5 - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx)
                + (-_2bx * q3 + _2bz * q1) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my)
                + _2bx * q2 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5 - q1q1 - q2q2) - mz);

        double s1 = _2q3 * (2.0 * q1q3 - _2q0q2 - ax)
                + _2q0 * (2.0 * q0q1 + _2q2q3 - ay)
                - 4.0 * q1 * (1.0 - 2.0 * q1q1 - 2.0 * q2q2 - az)
                + _2bz * q3 * (_2bx * (0.5 - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx)
                + (_2bx * q2 + _2bz * q0) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my)
                + (_2bx * q3 - 2.0 * _2bz * q1) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5 - q1q1 - q2q2) - mz);

        double s2 = -_2q0 * (2.0 * q1q3 - _2q0q2 - ax)
                + _2q3 * (2.0 * q0q1 + _2q2q3 - ay)
                - 4.0 * q2 * (1.0 - 2.0 * q1q1 - 2.0 * q2q2 - az)
                + (-2.0 * _2bx * q2 - _2bz * q0) * (_2bx * (0.5 - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx)
                + (_2bx * q1 + _2bz * q3) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my)
                + (_2bx * q0 - 2.0 * _2bz * q2) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5 - q1q1 - q2q2) - mz);

        double s3 = _2q1 * (2.0 * q1q3 - _2q0q2 - ax)
                + _2q2 * (2.0 * q0q1 + _2q2q3 - ay)
                + (-2.0 * _2bx * q3 + _2bz * q1) * (_2bx * (0.5 - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx)
                + (-_2bx * q0 + _2bz * q2) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my)
                + _2bx * q1 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5 - q1q1 - q2q2) - mz);

        // Normalize gradient step
        double normS = Math.sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3);
        if (normS > 1e-10) {
            s0 /= normS; s1 /= normS; s2 /= normS; s3 /= normS;
        }

        // Compute rate of change of quaternion from gyroscope
        double qDot0 = 0.5 * (-q1 * gx - q2 * gy - q3 * gz) - beta * s0;
        double qDot1 = 0.5 * (q0 * gx + q2 * gz - q3 * gy) - beta * s1;
        double qDot2 = 0.5 * (q0 * gy - q1 * gz + q3 * gx) - beta * s2;
        double qDot3 = 0.5 * (q0 * gz + q1 * gy - q2 * gx) - beta * s3;

        // Integrate to yield quaternion
        q0 += qDot0 * dt;
        q1 += qDot1 * dt;
        q2 += qDot2 * dt;
        q3 += qDot3 * dt;

        // Normalize quaternion
        double norm = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 /= norm; q1 /= norm; q2 /= norm; q3 /= norm;
    }

    private void updateIMU(double ax, double ay, double az,
                           double gx, double gy, double gz, float dt) {
        // Gradient descent for gravity-only (6DOF, no magnetometer)
        double _2q0 = 2.0 * q0, _2q1 = 2.0 * q1, _2q2 = 2.0 * q2, _2q3 = 2.0 * q3;
        double _4q0 = 4.0 * q0, _4q1 = 4.0 * q1, _4q2 = 4.0 * q2;
        double _8q1 = 8.0 * q1, _8q2 = 8.0 * q2;
        double q0q0 = q0 * q0, q1q1 = q1 * q1, q2q2 = q2 * q2, q3q3 = q3 * q3;

        double s0 = _4q0 * q2q2 + _2q2 * ax + _4q0 * q1q1 - _2q1 * ay;
        double s1 = _4q1 * q3q3 - _2q3 * ax + 4.0 * q0q0 * q1 - _2q0 * ay - _4q1 + _8q1 * q1q1 + _8q1 * q2q2 + _4q1 * az;
        double s2 = 4.0 * q0q0 * q2 + _2q0 * ax + _4q2 * q3q3 - _2q3 * ay - _4q2 + _8q2 * q1q1 + _8q2 * q2q2 + _4q2 * az;
        double s3 = 4.0 * q1q1 * q3 - _2q1 * ax + 4.0 * q2q2 * q3 - _2q2 * ay;

        double normS = Math.sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3);
        if (normS > 1e-10) {
            s0 /= normS; s1 /= normS; s2 /= normS; s3 /= normS;
        }

        double qDot0 = 0.5 * (-q1 * gx - q2 * gy - q3 * gz) - beta * s0;
        double qDot1 = 0.5 * (q0 * gx + q2 * gz - q3 * gy) - beta * s1;
        double qDot2 = 0.5 * (q0 * gy - q1 * gz + q3 * gx) - beta * s2;
        double qDot3 = 0.5 * (q0 * gz + q1 * gy - q2 * gx) - beta * s3;

        q0 += qDot0 * dt;
        q1 += qDot1 * dt;
        q2 += qDot2 * dt;
        q3 += qDot3 * dt;

        double norm = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 /= norm; q1 /= norm; q2 /= norm; q3 /= norm;
    }

    public double[] getQuaternion() {
        return new double[]{q0, q1, q2, q3};
    }

    public void reset() {
        q0 = 1.0; q1 = 0.0; q2 = 0.0; q3 = 0.0;
    }
}
