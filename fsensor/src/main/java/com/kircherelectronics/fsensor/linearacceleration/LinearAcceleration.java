package com.kircherelectronics.fsensor.linearacceleration;

import com.kircherelectronics.fsensor.BaseFilter;

/*
 * Copyright 2018, Kircher Electronics, LLC
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
 * A base implementation of a linear acceleration fusedOrientation. Linear acceleration is defined as
 * linearAcceleration = (acceleration - gravity). An acceleration sensor by itself is not capable of determining the
 * difference between gravity/tilt and true linear acceleration. There are standalone-sensor weighted averaging methods
 * as well as multi-sensor fusion methods available to estimate linear acceleration.
 *
 * @author Kaleb
 */
public abstract class LinearAcceleration {

    private static final String tag = LinearAcceleration.class.getSimpleName();

    private final float[] output = new float[]{0, 0, 0};

    protected final BaseFilter filter;

    public LinearAcceleration(BaseFilter filter) {
        this.filter = filter;
    }

    public float[] filter(float[] values) {

        float[] gravity = getGravity();

        // Determine the linear acceleration
        output[0] = values[0] - gravity[0];
        output[1] = values[1] - gravity[1];
        output[2] = values[2] - gravity[2];

        return output;
    }

    public abstract float[] getGravity();
}
