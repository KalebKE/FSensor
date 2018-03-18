package com.kircherelectronics.fsensor.util.offset;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by kaleb on 3/18/18.
 */

public class OffsetUtil {

    /**
     * Compensate for hard and soft iron distortions (magnetic) or sensor skew and offsets (acceleration) depending on the sensor type.
     * @param magnetic
     * @param scalar
     * @param center
     * @return
     */
    public static float[] compensate(float[] magnetic, RealMatrix scalar, RealVector center) {
        RealVector point = new ArrayRealVector(3);

        point.setEntry(0, magnetic[0]);
        point.setEntry(1, magnetic[1]);
        point.setEntry(2, magnetic[2]);

        point = scalar.operate((point.subtract(center)));

        magnetic[0] = (float) point.getEntry(0);
        magnetic[1] = (float) point.getEntry(1);
        magnetic[2] = (float) point.getEntry(2);

        return magnetic;
    }
}
