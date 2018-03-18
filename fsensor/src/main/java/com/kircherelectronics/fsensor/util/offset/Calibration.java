package com.kircherelectronics.fsensor.util.offset;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Represents a field calibration.
 * Created by kaleb on 3/18/18.
 */
public class Calibration {
    public final RealMatrix scalar;
    public final RealVector offset;

    /**
     * Create an instance.
     *
     * @param scalar The scalar of the calibration.
     * @param offset The offset of the calibration.
     */
    public Calibration(RealMatrix scalar, RealVector offset) {
        this.scalar = scalar;
        this.offset = offset;
    }

}
