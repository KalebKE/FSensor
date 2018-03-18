package com.kircherelectronics.fsensor.util.offset;

import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Transforms the ellipsoid into a sphere with the offset vector = [0,0,0] and
 * the radii vector = [1,1,1].
 *
 * @author Kaleb
 */
public class CalibrationUtil {
    private static final String TAG = CalibrationUtil.class.getSimpleName();


    /**
     * Transforms the ellipsoid into a sphere with the offset vector = [0,0,0]
     * and the radii vector = [1,1,1].
     *
     * @param fitPoints the representation of the calibration ellipsoid
     */
    public static Calibration getCalibration(FitPoints fitPoints) {
        // The scalar values to transform the radii vector into [1,1,1]
        RealMatrix scalar = new Array2DRowRealMatrix(3, 3);

        // RIV determines the magnitude of the radii. We have to know the
        // magnitudes because the eigenvalues, and thus the radii, are returned
        // in ascending order. Without knowing the magnitudes, we wouldn't know
        // what radii to apply to what axis.

        // Find the max and minimum magnitudes.
        double max = fitPoints.riv.getEntry(0);
        double min = fitPoints.riv.getEntry(0);

        // The indexes of the maximum, median, and minimum radii.
        // Note that these are the opposite of the max and min
        // because a smaller riv value means a greater magnitude.
        int maxi = 0, midi = 0, mini = 0;

        // Find max and min radii
        for (int i = 0; i < fitPoints.riv.getDimension(); i++) {
            if (fitPoints.riv.getEntry(i) > max) {
                max = fitPoints.riv.getEntry(i);
                mini = i;
            }
            if (fitPoints.riv.getEntry(i) < min) {
                min = fitPoints.riv.getEntry(i);
                maxi = i;
            }
        }

        // Find median radii
        for (int i = 0; i < fitPoints.riv.getDimension(); i++) {
            if (fitPoints.riv.getEntry(i) < max && fitPoints.riv.getEntry(i) > min) {
                midi = i;
            }
        }

        // Create the scalar vector in the correct orientation.
        scalar.setEntry(0, 0, 1 / fitPoints.radii.getEntry(mini));
        scalar.setEntry(1, 1, 1 / fitPoints.radii.getEntry(midi));
        scalar.setEntry(2, 2, 1 / fitPoints.radii.getEntry(maxi));

        return new Calibration(scalar, fitPoints.center);
    }

    /**
     * Compensate for hard and soft iron distortions (magnetic) or sensor skew and offsets (acceleration) depending on the sensor type.
     *
     * @param vector      the vector to be compensated.
     * @param calibration the calibration to be applied
     * @return the compensated vector
     */
    public static float[] calibrate(float[] vector, Calibration calibration) {
        RealVector point = new ArrayRealVector(3);

        point.setEntry(0, vector[0]);
        point.setEntry(1, vector[1]);
        point.setEntry(2, vector[2]);

        point = calibration.scalar.operate((point.subtract(calibration.offset)));

        vector[0] = (float) point.getEntry(0);
        vector[1] = (float) point.getEntry(1);
        vector[2] = (float) point.getEntry(2);

        return vector;
    }

    private void log(double maxi, double midi, double mini) {
        Log.d(TAG, "max :" + maxi);
        Log.d(TAG, "mid :" + midi);
        Log.d(TAG, "min :" + mini);
    }
}
