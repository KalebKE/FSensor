package com.kircherelectronics.fsensor.filter.averaging;

/*
 * Copyright 2017, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.kircherelectronics.fsensor.BaseFilter;

/**
 * A base implementation of an averaging fusedOrientation.
 *
 * Created by kaleb on 7/6/17.
 */

public abstract class AveragingFilter extends BaseFilter {
    public static float DEFAULT_TIME_CONSTANT = 0.18f;

    protected float timeConstant;
    protected long startTime;
    protected long timestamp;
    protected int count;

    public AveragingFilter() {
        this(DEFAULT_TIME_CONSTANT);
    }

    public AveragingFilter(float timeConstant) {
        this.timeConstant = timeConstant;
        reset();
    }

    public void reset() {
        startTime = 0;
        timestamp = 0;
        count = 0;
    }

    public abstract float[] filter(float[] data);
}
