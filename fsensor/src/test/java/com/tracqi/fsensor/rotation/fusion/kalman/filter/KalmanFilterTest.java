package com.tracqi.fsensor.rotation.fusion.kalman.filter;

import org.junit.Test;

import static org.junit.Assert.*;

public class KalmanFilterTest {

    private static final double EPSILON = 1e-4;

    @Test
    public void constructor_initializesCorrectDimensions() {
        RotationProcessModel process = new RotationProcessModel();
        RotationMeasurementModel measurement = new RotationMeasurementModel();
        KalmanFilter kf = new KalmanFilter(process, measurement);

        assertEquals(4, kf.getStateDimension());
        assertEquals(4, kf.getMeasurementDimension());
    }

    @Test
    public void predict_appliesTransitionAndControl() {
        RotationProcessModel process = new RotationProcessModel();
        RotationMeasurementModel measurement = new RotationMeasurementModel();
        KalmanFilter kf = new KalmanFilter(process, measurement);

        // Initial state is [0, 0, 0, 0]
        double[] initial = kf.getStateEstimation();
        for (double v : initial) {
            assertEquals(0.0, v, EPSILON);
        }

        // With A=I and B=I: x_new = I*[0,0,0,0] + I*u = u
        double[] u = {0.0, 0.0, 0.383, 0.924};
        kf.predict(u);

        double[] state = kf.getStateEstimation();
        assertEquals(0.0, state[0], EPSILON);
        assertEquals(0.0, state[1], EPSILON);
        assertEquals(0.383, state[2], EPSILON);
        assertEquals(0.924, state[3], EPSILON);
    }

    @Test
    public void predict_increasesErrorCovariance() {
        RotationProcessModel process = new RotationProcessModel();
        RotationMeasurementModel measurement = new RotationMeasurementModel();
        KalmanFilter kf = new KalmanFilter(process, measurement);

        double[][] covBefore = kf.getErrorCovariance();
        double traceBefore = trace(covBefore);

        kf.predict(new double[]{0, 0, 0, 1});

        double[][] covAfter = kf.getErrorCovariance();
        double traceAfter = trace(covAfter);

        // P_new = A*P*A' + Q, so trace increases by trace(Q)
        assertTrue("Error covariance should increase after predict", traceAfter > traceBefore);
    }

    @Test
    public void correct_decreasesErrorCovariance() {
        RotationProcessModel process = new RotationProcessModel();
        RotationMeasurementModel measurement = new RotationMeasurementModel();
        KalmanFilter kf = new KalmanFilter(process, measurement);

        kf.predict(new double[]{0, 0, 0, 1});

        double[][] covAfterPredict = kf.getErrorCovariance();
        double traceAfterPredict = trace(covAfterPredict);

        kf.correct(new double[]{0, 0, 0, 1});

        double[][] covAfterCorrect = kf.getErrorCovariance();
        double traceAfterCorrect = trace(covAfterCorrect);

        assertTrue("Error covariance trace should decrease after correction",
                traceAfterCorrect < traceAfterPredict);
    }

    @Test
    public void correct_pullsStateTowardMeasurement() {
        RotationProcessModel process = new RotationProcessModel();
        RotationMeasurementModel measurement = new RotationMeasurementModel();
        KalmanFilter kf = new KalmanFilter(process, measurement);

        // Predict to state [0,0,0,1]
        kf.predict(new double[]{0, 0, 0, 1});

        // Correct with measurement that differs
        kf.correct(new double[]{0.1, 0, 0, 0.995});

        double[] state = kf.getStateEstimation();
        // State should move toward measurement (x component should become positive)
        assertTrue("State should be pulled toward measurement", state[0] > 0);
        // But not all the way (since R > Q, we trust prediction more)
        assertTrue("State should not fully match measurement", state[0] < 0.1);
    }

    @Test
    public void steadyState_processNoiseSmall_favorsPrediction() {
        RotationProcessModel process = new RotationProcessModel();
        RotationMeasurementModel measurement = new RotationMeasurementModel();
        KalmanFilter kf = new KalmanFilter(process, measurement);

        // Run many predict/correct cycles to reach steady state
        for (int i = 0; i < 100; i++) {
            kf.predict(new double[]{0, 0, 0, 1});
            kf.correct(new double[]{0, 0, 0, 1});
        }

        // At steady state, now predict with different values
        kf.predict(new double[]{0, 0, 0, 1}); // prediction says identity
        kf.correct(new double[]{0.5, 0, 0, 0.866}); // measurement says 60 deg around X

        double[] state = kf.getStateEstimation();
        // With Q=0.001 and R=0.1 at steady state, Kalman gain is small
        // so state stays closer to prediction than measurement
        double distToPred = Math.abs(state[0] - 0) + Math.abs(state[3] - 1);
        double distToMeas = Math.abs(state[0] - 0.5) + Math.abs(state[3] - 0.866);

        assertTrue("At steady state with low Q, high R, state should favor prediction. " +
                        "distToPred=" + distToPred + " distToMeas=" + distToMeas,
                distToPred < distToMeas);
    }

    @Test
    public void multipleCorrections_convergeToConsistentMeasurement() {
        RotationProcessModel process = new RotationProcessModel();
        RotationMeasurementModel measurement = new RotationMeasurementModel();
        KalmanFilter kf = new KalmanFilter(process, measurement);

        double[] target = {0.0, 0.0, 0.383, 0.924};
        // Use zero control input (no rotation delta) with constant measurement
        double[] noChange = {0, 0, 0, 0};

        // Initial predict sets state to target
        kf.predict(target);
        kf.correct(target);

        // Subsequent cycles: predict with zero delta, correct with same measurement
        for (int i = 0; i < 20; i++) {
            kf.predict(noChange);
            kf.correct(target);
        }

        double[] state = kf.getStateEstimation();
        assertEquals(target[0], state[0], 0.05);
        assertEquals(target[1], state[1], 0.05);
        assertEquals(target[2], state[2], 0.05);
        assertEquals(target[3], state[3], 0.05);
    }

    private double trace(double[][] m) {
        double t = 0;
        for (int i = 0; i < m.length; i++) {
            t += m[i][i];
        }
        return t;
    }
}
