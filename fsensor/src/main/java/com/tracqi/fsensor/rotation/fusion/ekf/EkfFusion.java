package com.tracqi.fsensor.rotation.fusion.ekf;

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
 * Extended Kalman Filter for quaternion-based orientation estimation.
 * <p>
 * Unlike the linear Kalman filter, the EKF properly handles the non-linear
 * quaternion kinematics by linearizing the dynamics at each time step.
 * <p>
 * State: x = [q0, q1, q2, q3] (unit quaternion)
 * Process model: quaternion kinematics q_dot = 0.5 * Omega(omega) * q
 * Measurement model: expected gravity and magnetic field in body frame
 */
public class EkfFusion {

    private final double processNoiseVariance;
    private final double accelNoiseVariance;
    private final double magNoiseVariance;

    // State: quaternion [q0, q1, q2, q3]
    private double[] q = {1.0, 0.0, 0.0, 0.0};

    // Error covariance 4x4
    private double[][] P = {
            {0.1, 0, 0, 0},
            {0, 0.1, 0, 0},
            {0, 0, 0.1, 0},
            {0, 0, 0, 0.1}
    };

    /**
     * @param processNoiseVariance gyroscope noise variance (typical: 0.001)
     * @param accelNoiseVariance   accelerometer noise variance (typical: 0.1)
     * @param magNoiseVariance     magnetometer noise variance (typical: 0.5)
     */
    public EkfFusion(double processNoiseVariance, double accelNoiseVariance, double magNoiseVariance) {
        this.processNoiseVariance = processNoiseVariance;
        this.accelNoiseVariance = accelNoiseVariance;
        this.magNoiseVariance = magNoiseVariance;
    }

    public EkfFusion() {
        this(0.001, 0.1, 0.5);
    }

    /**
     * Predict step using gyroscope data.
     */
    public void predict(float[] gyro, float dt) {
        double wx = gyro[0], wy = gyro[1], wz = gyro[2];

        // State transition matrix: F = I + 0.5 * Omega(omega) * dt
        // Omega(omega) for quaternion kinematics:
        // [  0  -wx  -wy  -wz ]
        // [ wx   0    wz  -wy ]
        // [ wy  -wz   0    wx ]
        // [ wz   wy  -wx   0  ]
        double halfDt = 0.5 * dt;
        double[][] F = {
                {1.0, -halfDt * wx, -halfDt * wy, -halfDt * wz},
                {halfDt * wx, 1.0, halfDt * wz, -halfDt * wy},
                {halfDt * wy, -halfDt * wz, 1.0, halfDt * wx},
                {halfDt * wz, halfDt * wy, -halfDt * wx, 1.0}
        };

        // Propagate state: q_new = F * q
        double[] qNew = matVecMul4(F, q);

        // Normalize
        double norm = Math.sqrt(qNew[0] * qNew[0] + qNew[1] * qNew[1] + qNew[2] * qNew[2] + qNew[3] * qNew[3]);
        q[0] = qNew[0] / norm;
        q[1] = qNew[1] / norm;
        q[2] = qNew[2] / norm;
        q[3] = qNew[3] / norm;

        // Propagate covariance: P = F * P * F' + Q
        double[][] Ft = transpose4(F);
        double[][] FP = matMul4(F, P);
        double[][] FPFt = matMul4(FP, Ft);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                P[i][j] = FPFt[i][j] + (i == j ? processNoiseVariance * dt : 0);
            }
        }
    }

    /**
     * Correct step using accelerometer (gravity reference).
     */
    public void correctAccelerometer(float[] accel) {
        double ax = accel[0], ay = accel[1], az = accel[2];
        double normA = Math.sqrt(ax * ax + ay * ay + az * az);
        if (normA < 1e-10) return;
        ax /= normA; ay /= normA; az /= normA;

        // Expected gravity in body frame from current quaternion: R(q)^T * [0, 0, 1]
        double q0q0 = q[0] * q[0], q1q1 = q[1] * q[1], q2q2 = q[2] * q[2], q3q3 = q[3] * q[3];
        double gx_pred = 2.0 * (q[1] * q[3] - q[0] * q[2]);
        double gy_pred = 2.0 * (q[0] * q[1] + q[2] * q[3]);
        double gz_pred = q0q0 - q1q1 - q2q2 + q3q3;

        // Innovation (measurement - prediction)
        double[] innovation = {ax - gx_pred, ay - gy_pred, az - gz_pred};

        // Jacobian H (3x4): dh/dq for gravity measurement
        double[][] H = {
                {-2.0 * q[2], 2.0 * q[3], -2.0 * q[0], 2.0 * q[1]},
                {2.0 * q[1], 2.0 * q[0], 2.0 * q[3], 2.0 * q[2]},
                {2.0 * q[0], -2.0 * q[1], -2.0 * q[2], 2.0 * q[3]}
        };

        // R (3x3 measurement noise)
        double[][] R = {
                {accelNoiseVariance, 0, 0},
                {0, accelNoiseVariance, 0},
                {0, 0, accelNoiseVariance}
        };

        applyCorrection(H, R, innovation, 3);
    }

    /**
     * Correct step using magnetometer (heading reference).
     */
    public void correctMagnetometer(float[] mag) {
        double mx = mag[0], my = mag[1], mz = mag[2];
        double normM = Math.sqrt(mx * mx + my * my + mz * mz);
        if (normM < 1e-10) return;
        mx /= normM; my /= normM; mz /= normM;

        // Rotate mag to Earth frame to find reference
        double q0q0 = q[0] * q[0], q1q1 = q[1] * q[1], q2q2 = q[2] * q[2], q3q3 = q[3] * q[3];
        double hx = mx * (q0q0 + q1q1 - q2q2 - q3q3) + 2.0 * my * (q[1] * q[2] - q[0] * q[3]) + 2.0 * mz * (q[1] * q[3] + q[0] * q[2]);
        double hy = 2.0 * mx * (q[1] * q[2] + q[0] * q[3]) + my * (q0q0 - q1q1 + q2q2 - q3q3) + 2.0 * mz * (q[2] * q[3] - q[0] * q[1]);
        double bx = Math.sqrt(hx * hx + hy * hy);
        double bz = 2.0 * mx * (q[1] * q[3] - q[0] * q[2]) + 2.0 * my * (q[2] * q[3] + q[0] * q[1]) + mz * (q0q0 - q1q1 - q2q2 + q3q3);

        // Expected magnetic field in body frame
        double mx_pred = bx * (q0q0 + q1q1 - q2q2 - q3q3) + bz * 2.0 * (q[1] * q[3] - q[0] * q[2]);
        double my_pred = bx * 2.0 * (q[1] * q[2] - q[0] * q[3]) + bz * 2.0 * (q[0] * q[1] + q[2] * q[3]);
        double mz_pred = bx * 2.0 * (q[0] * q[2] + q[1] * q[3]) + bz * (q0q0 - q1q1 - q2q2 + q3q3);

        double[] innovation = {mx - mx_pred, my - my_pred, mz - mz_pred};

        // Simplified Jacobian (numerical differentiation could be used for exact)
        // Use approximate H based on gravity-like structure for heading
        double[][] H = {
                {-2.0 * q[2] * bz, 2.0 * q[3] * bz, -2.0 * q[0] * bz + 2.0 * q[2] * bx, 2.0 * q[1] * bz + 2.0 * q[3] * bx},
                {2.0 * q[1] * bz - 2.0 * q[3] * bx, 2.0 * q[0] * bz + 2.0 * q[2] * bx, 2.0 * q[3] * bz + 2.0 * q[1] * bx, -2.0 * q[0] * bx + 2.0 * q[2] * bz},
                {2.0 * q[0] * bx + 2.0 * q[2] * bz, -2.0 * q[1] * bx + 2.0 * q[3] * bz, 2.0 * q[0] * bz - 2.0 * q[2] * bx, 2.0 * q[1] * bz + 2.0 * q[3] * bx}
        };

        double[][] R = {
                {magNoiseVariance, 0, 0},
                {0, magNoiseVariance, 0},
                {0, 0, magNoiseVariance}
        };

        applyCorrection(H, R, innovation, 3);
    }

    private void applyCorrection(double[][] H, double[][] R, double[] innovation, int mDim) {
        // S = H * P * H' + R (mDim x mDim)
        double[][] Ht = new double[4][mDim];
        for (int i = 0; i < mDim; i++)
            for (int j = 0; j < 4; j++)
                Ht[j][i] = H[i][j];

        double[][] PHt = matMul(P, Ht, 4, 4, mDim);
        double[][] S = matMul(H, PHt, mDim, 4, mDim);
        for (int i = 0; i < mDim; i++)
            S[i][i] += R[i][i];

        // K = P * H' * S^-1 (4 x mDim)
        double[][] Sinv = invertSymmetric(S, mDim);
        double[][] K = matMul(PHt, Sinv, 4, mDim, mDim);

        // State update: x = x + K * innovation
        for (int i = 0; i < 4; i++) {
            double correction = 0;
            for (int j = 0; j < mDim; j++) {
                correction += K[i][j] * innovation[j];
            }
            q[i] += correction;
        }

        // Normalize quaternion
        double norm = Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        q[0] /= norm; q[1] /= norm; q[2] /= norm; q[3] /= norm;

        // Covariance update: P = (I - K*H) * P (Joseph form for stability)
        double[][] KH = matMul(K, H, 4, mDim, 4);
        double[][] IminusKH = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                IminusKH[i][j] = (i == j ? 1.0 : 0.0) - KH[i][j];
            }
        }
        P = matMul4(IminusKH, P);
    }

    public double[] getQuaternion() {
        return new double[]{q[0], q[1], q[2], q[3]};
    }

    public void reset() {
        q = new double[]{1.0, 0.0, 0.0, 0.0};
        P = new double[][]{
                {0.1, 0, 0, 0},
                {0, 0.1, 0, 0},
                {0, 0, 0.1, 0},
                {0, 0, 0, 0.1}
        };
        }

    // --- Matrix utilities for small matrices ---

    private static double[] matVecMul4(double[][] A, double[] x) {
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) {
            result[i] = A[i][0] * x[0] + A[i][1] * x[1] + A[i][2] * x[2] + A[i][3] * x[3];
        }
        return result;
    }

    private static double[][] matMul4(double[][] A, double[][] B) {
        double[][] C = new double[4][4];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++)
                    C[i][j] += A[i][k] * B[k][j];
        return C;
    }

    private static double[][] matMul(double[][] A, double[][] B, int m, int n, int p) {
        double[][] C = new double[m][p];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < p; j++)
                for (int k = 0; k < n; k++)
                    C[i][j] += A[i][k] * B[k][j];
        return C;
    }

    private static double[][] transpose4(double[][] A) {
        double[][] T = new double[4][4];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                T[i][j] = A[j][i];
        return T;
    }

    private static double[][] invertSymmetric(double[][] A, int n) {
        // Simple Gauss-Jordan inversion for small symmetric matrices (3x3 or 4x4)
        double[][] aug = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, aug[i], 0, n);
            aug[i][n + i] = 1.0;
        }

        for (int col = 0; col < n; col++) {
            // Partial pivoting
            int maxRow = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(aug[row][col]) > Math.abs(aug[maxRow][col])) {
                    maxRow = row;
                }
            }
            double[] temp = aug[col];
            aug[col] = aug[maxRow];
            aug[maxRow] = temp;

            double pivot = aug[col][col];
            if (Math.abs(pivot) < 1e-12) {
                pivot = 1e-12; // Regularize
            }
            for (int j = 0; j < 2 * n; j++) {
                aug[col][j] /= pivot;
            }
            for (int row = 0; row < n; row++) {
                if (row != col) {
                    double factor = aug[row][col];
                    for (int j = 0; j < 2 * n; j++) {
                        aug[row][j] -= factor * aug[col][j];
                    }
                }
            }
        }

        double[][] inv = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(aug[i], n, inv[i], 0, n);
        }
        return inv;
    }
}
