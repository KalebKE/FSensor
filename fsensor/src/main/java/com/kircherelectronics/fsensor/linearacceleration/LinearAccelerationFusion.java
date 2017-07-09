package com.kircherelectronics.fsensor.linearacceleration;

import com.kircherelectronics.fsensor.filter.fusion.OrientationFusion;
import com.kircherelectronics.fsensor.util.Util;

/**
 * Created by kaleb on 7/6/17.
 */

public class LinearAccelerationFusion extends LinearAcceleration {

    public LinearAccelerationFusion(OrientationFusion orientationFusion) {
        super(orientationFusion);
    }

    @Override
    public float[] getGravity(float[] values) {
        return Util.getGravityFromOrientation(filter.filter(values));
    }
}
