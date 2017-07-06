package com.kircherelectronics.fsensor.filter.averaging;

import com.kircherelectronics.fsensor.filter.BaseFilter;

import java.util.ArrayDeque;

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
 * Implements a mean filter designed to smooth the data points based on a mean. The mean filter will take the mean
 * of the samples that occur over a period defined by the time constant... the number
 * of samples that are considered is known as the filter window. The approach
 * allows the filter window to be defined over a period of time, instead of a
 * fixed number of samples. This is important on devices that are
 * equipped with different hardware sensors that output samples at different
 * frequencies and also allow the developer to generally specify the output
 * frequency. Defining the filter window in terms of the time constant allows
 * the mean filter to applied to all sensor outputs with the same relative
 * filter window, regardless of sensor frequency.
 *
 * @author Kaleb
 */
public class MeanFilter  implements BaseFilter {

    public static float DEFAULT_TIME_CONSTANT = 0.18f;

    private static final String tag = MeanFilter.class.getSimpleName();

    // The size of the mean filters rolling window.
    private int filterWindow;

    private ArrayDeque<float[]> values;

    private long startTime;
    private long timestamp;

    private int count;

    private float timeConstant;

    /**
     * Initialize a new MeanFilter object.
     */
    public MeanFilter() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public MeanFilter(float timeConstant) {
        this.timeConstant = timeConstant;
        this.values = new ArrayDeque<>();

        reset();
    }

    /**
     * Filter the data.
     *
     * @param data contains input the data.
     * @return the filtered output data.
     */
    public float[] filter(float[] data) {

        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.
        float hz = (count++ / ((timestamp - startTime) / 1000000000.0f));

        this.filterWindow = (int) (hz * timeConstant);

        values.addLast(data);

        while (values.size() > filterWindow) {
            values.removeFirst();
        }

        return getMean(values);
    }

    /**
     * Get the mean of the data set.
     *
     * @param data the data set.
     * @return the mean of the data set.
     */
    private float[] getMean(ArrayDeque<float[]> data) {
        float[] mean = new float[3];

        for (float[] axis : data) {
            for (int i = 0; i < axis.length; i++) {
                mean[i] += axis[i];
            }
        }

        for (int i = 0; i < mean.length; i++) {
            mean[i] = mean[i] / data.size();
        }

        return mean;
    }

    public void setTimeConstant(float timeConstant) {
        this.timeConstant = timeConstant;
    }

    public void reset()
    {
        startTime = 0;
        timestamp = 0;
        count = 0;

        this.values.clear();
    }
}
