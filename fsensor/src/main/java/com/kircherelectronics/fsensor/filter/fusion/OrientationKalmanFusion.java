package com.kircherelectronics.fsensor.filter.fusion;

import android.hardware.SensorManager;
import android.util.Log;

import com.kircherelectronics.fsensor.filter.kalman.RotationKalmanFilter;
import com.kircherelectronics.fsensor.filter.kalman.RotationMeasurementModel;
import com.kircherelectronics.fsensor.filter.kalman.RotationProcessModel;

import org.apache.commons.math3.complex.Quaternion;

import java.util.Arrays;

/**
 * Created by kaleb on 7/6/17.
 */

public class OrientationKalmanFusion extends OrientationFusion {

    private static final String tag = OrientationComplimentaryFusion.class.getSimpleName();

    private RotationKalmanFilter kalmanFilter;
    private RotationProcessModel pm;
    private RotationMeasurementModel mm;

    public OrientationKalmanFusion(float timeConstant) {
        super(timeConstant);

        pm = new RotationProcessModel();
        mm = new RotationMeasurementModel();

        kalmanFilter = new RotationKalmanFilter(pm, mm);
    }

    /**
     * Calculate the fused orientation.
     */
    protected float[] calculateFusedOrientation(float[] gyroscope, float dt, float[] acceleration, float[] magnetic) {

        float[] baseOrientation = getBaseOrientation(acceleration, magnetic);

        if (baseOrientation != null) {

            Quaternion rotationVectorAccelerationMagnetic = getAccelerationMagneticRotationVector(baseOrientation);
            initializeRotationVectorGyroscopeIfRequired(rotationVectorAccelerationMagnetic);

            rotationVectorGyroscope = getGyroscopeRotationVector(rotationVectorGyroscope, gyroscope, dt);

            double[] vectorGyroscope = new double[4];

            vectorGyroscope[0] = (float) rotationVectorGyroscope.getVectorPart()[0];
            vectorGyroscope[1] = (float) rotationVectorGyroscope.getVectorPart()[1];
            vectorGyroscope[2] = (float) rotationVectorGyroscope.getVectorPart()[2];
            vectorGyroscope[3] = (float) rotationVectorGyroscope.getScalarPart();

            double[] vectorAccelerationMagnetic = new double[4];

            vectorAccelerationMagnetic[0] = (float) rotationVectorAccelerationMagnetic.getVectorPart()[0];
            vectorAccelerationMagnetic[1] = (float) rotationVectorAccelerationMagnetic.getVectorPart()[1];
            vectorAccelerationMagnetic[2] = (float) rotationVectorAccelerationMagnetic.getVectorPart()[2];
            vectorAccelerationMagnetic[3] = (float) rotationVectorAccelerationMagnetic.getScalarPart();

            // Apply the Kalman filter... Note that the prediction and correction
            // inputs could be swapped, but the filter is much more stable in this
            // configuration.
            kalmanFilter.predict(vectorGyroscope);
            kalmanFilter.correct(vectorAccelerationMagnetic);

            // Apply the new gyroscope delta rotation to the new Kalman filter
            // rotation estimation.
            rotationVectorGyroscope = new Quaternion(kalmanFilter.getStateEstimation()[3],
                    Arrays.copyOfRange(kalmanFilter.getStateEstimation(), 0, 3));

            // Now we get a structure we can pass to get a rotation matrix, and then
            // an orientation vector from Android.

            float[] fusedVector = new float[4];

            // Now we get a structure we can pass to get a rotation matrix, and then
            // an orientation vector from Android.
            fusedVector[0] = (float) rotationVectorGyroscope.getVectorPart()[0];
            fusedVector[1] = (float) rotationVectorGyroscope.getVectorPart()[1];
            fusedVector[2] = (float) rotationVectorGyroscope.getVectorPart()[2];
            fusedVector[3] = (float) rotationVectorGyroscope.getScalarPart();

            // rotation matrix from gyro data
            float[] fusedMatrix = new float[9];

            // We need a rotation matrix so we can get the orientation vector...
            // Getting Euler
            // angles from a quaternion is not trivial, so this is the easiest way,
            // but perhaps
            // not the fastest way of doing this.
            SensorManager.getRotationMatrixFromVector(fusedMatrix, fusedVector);

            float[] fusedOrientation = new float[3];

            // Get the fused orienatation
            SensorManager.getOrientation(fusedMatrix, fusedOrientation);

            return fusedOrientation;
        }

        // The device had a problem determining the base orientation from the acceleration and magnetic sensors,
        // possible because of bad inputs or possibly because the device determined the orientation could not be
        // calculated, e.g the device is in free-fall
        Log.w(tag, "Base Device Orientation could not be computed!");

        return null;
    }
}
