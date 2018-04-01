package com.kircherelectronics.fsensor.filter.averaging;

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
 * An implementation of the Android Developer low-pass fusedOrientation. The Android
 * Developer LowPassFilter, is an IIR single-pole implementation. The coefficient, a
 * (alpha), can be adjusted based on the sample period of the sensor to produce
 * the desired time constant that the fusedOrientation will act on. It takes a simple form of y[0] = alpha * y[0] + (1
 * - alpha) * x[0]. Alpha is defined as alpha = timeConstant / (timeConstant +
 * dt) where the time constant is the length of signals the fusedOrientation should act on
 * and dt is the sample period (1/frequency) of the sensor.
 *
 * http://developer.android.com/reference/android/hardware/SensorEvent.html
 * @author Kaleb
 */
public class LowPassFilter extends AveragingFilter
{
    private static final String tag = LowPassFilter.class.getSimpleName();

    // Gravity and linear accelerations components for the
    // Wikipedia low-pass fusedOrientation
    private float[] output;

    public LowPassFilter() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public LowPassFilter(float timeConstant) {
        this.timeConstant = timeConstant;
        reset();
    }

    /**
     * Add a sample.
     *
     * @param values
     *            The acceleration data. A 1x3 matrix containing the data from the X, Y and Z axis of the sensor
     *            noting that order is arbitrary.
     * @return Returns the output of the fusedOrientation.
     */
    public float[] filter(float[] values)
    {
        // Initialize the start time.
        if (startTime == 0)
        {
            startTime = System.nanoTime();
        }

        timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.
        float dt = 1 / (count++ / ((timestamp - startTime) / 1000000000.0f));

        float alpha = timeConstant / (timeConstant + dt);

        output[0] = alpha * output[0] + (1 - alpha) * values[0];
        output[1] = alpha * output[1] + (1 - alpha) * values[1];
        output[2] = alpha * output[2] + (1 - alpha) * values[2];

        return output;
    }

    @Override
    public float[] getOutput() {
        return output;
    }

    public void setTimeConstant(float timeConstant)
    {
        this.timeConstant = timeConstant;
    }

    public void reset()
    {
        super.reset();
        this.output = new float[]
                { 0, 0, 0 };

    }
}
