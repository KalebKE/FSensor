package com.kircherelectronics.fsensor.util.offset;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * A helper for compensating for field distortions.
 * Created by kaleb on 3/18/18.
 */
public class OffsetUtil {

    /**
     * Compensate for hard and soft iron distortions (magnetic) or sensor skew and offsets (acceleration) depending on the sensor type.
     * @param vector the vector to be compensated.
     * @param scalar the scalar to be applied to the radii
     * @param center the offset to apply the transform
     * @return a vector that is relative to the center (0,0,0) and unit radii (1,1,1).
     */
    public static float[] compensate(float[] vector, RealMatrix scalar, RealVector center) {
        RealVector point = new ArrayRealVector(3);

        point.setEntry(0, vector[0]);
        point.setEntry(1, vector[1]);
        point.setEntry(2, vector[2]);

        point = scalar.operate((point.subtract(center)));

        vector[0] = (float) point.getEntry(0);
        vector[1] = (float) point.getEntry(1);
        vector[2] = (float) point.getEntry(2);

        return vector;
    }
}
