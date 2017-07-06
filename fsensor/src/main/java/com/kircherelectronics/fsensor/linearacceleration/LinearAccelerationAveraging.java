package com.kircherelectronics.fsensor.linearacceleration;

import com.kircherelectronics.fsensor.filter.averaging.AveragingFilter;
import com.kircherelectronics.fsensor.filter.fusion.OrientationFusion;
import com.kircherelectronics.fsensor.util.Util;

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
