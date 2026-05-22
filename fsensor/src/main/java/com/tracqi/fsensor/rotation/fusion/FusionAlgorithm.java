package com.tracqi.fsensor.rotation.fusion;

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
 * Common interface for all sensor fusion algorithms.
 * Implementations are pure math with no Android dependencies, enabling
 * unit testing and eventual KMP portability.
 */
public interface FusionAlgorithm {

    /**
     * Update the fusion state with new sensor measurements.
     *
     * @param acceleration accelerometer reading [x, y, z] in m/s²
     * @param magnetic     magnetometer reading [x, y, z] in µT (may be null for 6DOF mode)
     * @param gyroscope    gyroscope reading [x, y, z] in rad/s
     * @param dt           time delta since last update in seconds
     */
    void update(float[] acceleration, float[] magnetic, float[] gyroscope, float dt);

    /**
     * @return current orientation as Euler angles [azimuth, pitch, roll] in radians
     */
    float[] getOrientation();

    /**
     * @return current orientation as quaternion [w, x, y, z]
     */
    double[] getQuaternion();

    /**
     * Reset the filter state to initial conditions.
     */
    void reset();
}
