package com.tracqi.fsensor.filter;

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
 * Implements a low pass filter designed to smooth the data points based on a time constant.
 */
public class LowPassFilter extends SensorFilter {
    private static final String tag = LowPassFilter.class.getSimpleName();

    public LowPassFilter() {}

    public LowPassFilter(float timeConstant) {
        this.timeConstant = timeConstant;
    }

    /**
     * Add a sample.
     *
     * @param values The acceleration data. A 1x3 matrix containing the data from the X, Y and Z axis of the sensor
     *               noting that order is arbitrary.
     * @return Returns the output of the fusedOrientation.
     */
    public float[] filter(float[] values) {
        // Initialize the start time.
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.
        float dt = 1 / (count++ / ((System.nanoTime() - startTime) / 1000000000.0f));

        float alpha = timeConstant / (timeConstant + dt);
        float oneMinusAlpha = 1 - alpha;

        output[0] = alpha * output[0] + oneMinusAlpha * values[0];
        output[1] = alpha * output[1] + oneMinusAlpha * values[1];
        output[2] = alpha * output[2] + oneMinusAlpha * values[2];

        return output;
    }
}
