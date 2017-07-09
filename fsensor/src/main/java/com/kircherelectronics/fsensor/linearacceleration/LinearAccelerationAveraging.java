package com.kircherelectronics.fsensor.linearacceleration;

import com.kircherelectronics.fsensor.filter.averaging.AveragingFilter;

/**
 * Created by kaleb on 7/6/17.
 */

public class LinearAccelerationAveraging extends LinearAcceleration {

    public LinearAccelerationAveraging(AveragingFilter averagingFilter) {
        super(averagingFilter);
    }

    @Override
    public float[] getGravity(float[] values) {
        return filter.filter(values);
    }
}
