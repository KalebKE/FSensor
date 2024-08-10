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
 * A base implementation of a sensor filter.
 *
 * @author Kaleb Kircher
 */

public abstract class SensorFilter {
    public static float DEFAULT_TIME_CONSTANT = 0.18f;

    protected SensorFilter filter;

    protected float timeConstant = DEFAULT_TIME_CONSTANT;
    protected long startTime;

    protected int count;
    protected final float[] output = new float[3];

    public SensorFilter() {}

    public SensorFilter(SensorFilter filter) {
        this.filter = filter;
    }

    public SensorFilter(float timeConstant) {
        this.timeConstant = timeConstant;
    }

    public SensorFilter(SensorFilter filter, float timeConstant) {
        this.timeConstant = timeConstant;
        this.filter = filter;
    }

    public void reset() {
        startTime = 0;
        count = 0;
    }

    public void setTimeConstant(float timeConstant) {
        this.timeConstant = timeConstant;
    }

    public abstract float[] filter(float[] data);
}
