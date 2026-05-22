package com.tracqi.fsensor.rotation.fusion.mahony;

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
 * Pure-math implementation of the Mahony AHRS algorithm.
 * <p>
 * Uses a proportional-integral (PI) complementary filter on SO(3).
 * The integral term compensates for gyroscope bias drift over time.
 * <p>
 * Reference: R. Mahony, T. Hamel, J-M. Pflimlin, "Nonlinear Complementary
 * Filters on the Special Orthogonal Group" (2008).
 */
public class MahonyFusion {

    private final float kp;
    private final float ki;

    private double q0 = 1.0, q1 = 0.0, q2 = 0.0, q3 = 0.0;
    private double integralFBx = 0.0, integralFBy = 0.0, integralFBz = 0.0;

    /**
     * @param kp Proportional gain (typical: 0.5-10.0, higher = faster convergence, more noise)
     * @param ki Integral gain (typical: 0.0-0.5, compensates gyro bias)
     */
    public MahonyFusion(float kp, float ki) {
        this.kp = kp;
        this.ki = ki;
    }

    /**
     * Update the orientation estimate.
     *
     * @param accel accelerometer [x, y, z] in m/s²
     * @param mag   magnetometer [x, y, z] in µT (may be null for 6DOF mode)
     * @param gyro  gyroscope [x, y, z] in rad/s
     * @param dt    time step in seconds
     */
    public void update(float[] accel, float[] mag, float[] gyro, float dt) {
        double gx = gyro[0], gy = gyro[1], gz = gyro[2];

        // Normalize accelerometer
        double ax = accel[0], ay = accel[1], az = accel[2];
        double normA = Math.sqrt(ax * ax + ay * ay + az * az);
        if (normA < 1e-10) return;
        ax /= normA; ay /= normA; az /= normA;

        // Estimated direction of gravity from quaternion
        double vx = 2.0 * (q1 * q3 - q0 * q2);
        double vy = 2.0 * (q0 * q1 + q2 * q3);
        double vz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3;

        // Error is cross product between estimated and measured gravity
        double ex = (ay * vz - az * vy);
        double ey = (az * vx - ax * vz);
        double ez = (ax * vy - ay * vx);

        // Add magnetometer correction if available
        if (mag != null) {
            double mx = mag[0], my = mag[1], mz = mag[2];
            double normM = Math.sqrt(mx * mx + my * my + mz * mz);
            if (normM > 1e-10) {
                mx /= normM; my /= normM; mz /= normM;

                // Reference direction of Earth's magnetic field (rotate mag to Earth frame)
                double hx = 2.0 * (mx * (0.5 - q2 * q2 - q3 * q3) + my * (q1 * q2 - q0 * q3) + mz * (q1 * q3 + q0 * q2));
                double hy = 2.0 * (mx * (q1 * q2 + q0 * q3) + my * (0.5 - q1 * q1 - q3 * q3) + mz * (q2 * q3 - q0 * q1));
                double bx = Math.sqrt(hx * hx + hy * hy);
                double bz = 2.0 * (mx * (q1 * q3 - q0 * q2) + my * (q2 * q3 + q0 * q1) + mz * (0.5 - q1 * q1 - q2 * q2));

                // Estimated direction of magnetic field
                double wx = bx * (0.5 - q2 * q2 - q3 * q3) + bz * (q1 * q3 - q0 * q2);
                double wy = bx * (q1 * q2 - q0 * q3) + bz * (q0 * q1 + q2 * q3);
                double wz = bx * (q0 * q2 + q1 * q3) + bz * (0.5 - q1 * q1 - q2 * q2);

                // Error is cross product between estimated and measured magnetic field
                ex += (my * wz - mz * wy);
                ey += (mz * wx - mx * wz);
                ez += (mx * wy - my * wx);
            }
        }

        // Apply integral feedback (accumulates gyro bias estimate)
        if (ki > 0.0f) {
            integralFBx += ki * ex * dt;
            integralFBy += ki * ey * dt;
            integralFBz += ki * ez * dt;
            gx += integralFBx;
            gy += integralFBy;
            gz += integralFBz;
        }

        // Apply proportional feedback
        gx += kp * ex;
        gy += kp * ey;
        gz += kp * ez;

        // Integrate quaternion rate of change
        double qDot0 = 0.5 * (-q1 * gx - q2 * gy - q3 * gz);
        double qDot1 = 0.5 * (q0 * gx + q2 * gz - q3 * gy);
        double qDot2 = 0.5 * (q0 * gy - q1 * gz + q3 * gx);
        double qDot3 = 0.5 * (q0 * gz + q1 * gy - q2 * gx);

        q0 += qDot0 * dt;
        q1 += qDot1 * dt;
        q2 += qDot2 * dt;
        q3 += qDot3 * dt;

        // Normalize quaternion
        double norm = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 /= norm; q1 /= norm; q2 /= norm; q3 /= norm;
    }

    public double[] getQuaternion() {
        return new double[]{q0, q1, q2, q3};
    }

    public void reset() {
        q0 = 1.0; q1 = 0.0; q2 = 0.0; q3 = 0.0;
        integralFBx = 0.0; integralFBy = 0.0; integralFBz = 0.0;
    }
}
