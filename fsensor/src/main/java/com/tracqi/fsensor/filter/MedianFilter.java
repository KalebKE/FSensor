package com.tracqi.fsensor.filter;

import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayDeque;
import java.util.Arrays;

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
 * Implements a median filter designed to smooth the data points based on a time constant.
 */
public class MedianFilter extends SensorFilter {

    private static final String tag = MedianFilter.class
            .getSimpleName();

    private final ArrayDeque<float[]> values = new ArrayDeque<>();;

    /**
     * Initialize a new MeanFilter object.
     */
    public MedianFilter() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public MedianFilter(float timeConstant) {
        this.timeConstant = timeConstant;
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

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.
        float hz = (count++ / ((System.nanoTime() - startTime) / 1000000000.0f));

        int filterWindow = (int) Math.ceil(hz * timeConstant);

        values.addLast(Arrays.copyOf(data, data.length));

        while (values.size() > filterWindow) {
            values.removeFirst();
        }

        if(!values.isEmpty()) {
            float[] median = getMedian(values);
            System.arraycopy(median, 0, output, 0, output.length);
        } else {
            System.arraycopy(data, 0, output, 0, data.length);
        }

        return output;
    }

    /**
     * Get the mean of the data set.
     *
     * @param data the data set.
     * @return the mean of the data set.
     */
    private float[] getMedian(ArrayDeque<float[]> data) {
        float[] mean = new float[data.getFirst().length];

        double[][] values = new double[data.getFirst().length][data.size()];
        int index = 0;

        for (float[] axis : data) {
            for (int i = 0; i < axis.length; i++) {
                values[i][index] = axis[i];
            }
            index++;
        }

        for (int i = 0; i < mean.length; i++) {
            mean[i] = (float) StatUtils.percentile(values[i], 50);
        }

        return mean;
    }
}