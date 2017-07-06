package com.kircherelectronics.fsensor.linearacceleration;

import com.kircherelectronics.fsensor.filter.BaseFilter;

/*
 * Acceleration Explorer
 * Copyright (C) 2013-2015, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A base implementation of a linear acceleration filter. Linear acceleration is defined as
 * linearAcceleration = (acceleration - gravity). An acceleration sensor by itself is not capable of determining the
 * difference between gravity/tilt and true linear acceleration. There are standalone-sensor weighted averaging methods
 * as well as multi-sensor fusion methods available to estimate linear acceleration.
 *
 * @author Kaleb
 */
public class LinearAcceleration {

    private float[] output = new float[]
            {0, 0, 0};

    private BaseFilter filter;

    public LinearAcceleration(BaseFilter filter) {
        this.filter = filter;
    }

    public float[] filter(float[] values) {

        float[] gravity = getGravity(values);

        // Determine the linear acceleration
        output[0] = values[0] - gravity[0];
        output[1] = values[1] - gravity[1];
        output[2] = values[2] - gravity[2];

        return output;
    }

    public void setTimeConstant(float timeConstant) {
        filter.setTimeConstant(timeConstant);
    }

    private float[] getGravity(float[] values) {
        return this.filter.filter(values);
    }

    public void reset() {
        filter.reset();
    }
}
