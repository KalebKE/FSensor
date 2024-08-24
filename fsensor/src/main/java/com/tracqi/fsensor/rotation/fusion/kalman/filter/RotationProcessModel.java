package com.tracqi.fsensor.rotation.fusion.kalman.filter;

import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/*
 * Copyright 2018, Kircher Electronics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class RotationProcessModel implements ProcessModel {
    /**
     * The state transition matrix, used to advance the internal state
     * estimation each time-step.
     */
    private final RealMatrix stateTransitionMatrix;

    /**
     * The process noise covariance matrix.
     */
    private final RealMatrix processNoiseCovMatrix;

    /**
     * The initial state estimation of the observed process.
     */
    private final RealVector initialStateEstimateVector;

    /**
     * The initial error covariance matrix of the observed process.
     */
    private final RealMatrix initialErrorCovMatrix;

    /**
     * The initial error covariance matrix of the observed process.
     */
    private final RealMatrix controlMatrix;

    public RotationProcessModel() {
        super();

        // A = stateTransitionMatrix
        stateTransitionMatrix = new Array2DRowRealMatrix(new double[][]
                {
                        {1, 0, 0, 0},
                        {0, 1, 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1}});

        // B = stateTransitionMatrix
        controlMatrix = new Array2DRowRealMatrix(new double[][]
                {
                        {1, 0, 0, 0},
                        {0, 1, 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1}});

        // Q = processNoiseCovMatrix
        processNoiseCovMatrix = new Array2DRowRealMatrix(new double[][]
                {
                        {0.01, 0, 0, 0},
                        {0, 0.01, 0, 0},
                        {0, 0, 0.01, 0},
                        {0, 0, 0, 0.01}});

        // xP = initialStateEstimateVector
        initialStateEstimateVector = new ArrayRealVector(new double[]
                {0, 0, 0, 0});

        // P0 = initialErrorCovMatrix;
        initialErrorCovMatrix = new Array2DRowRealMatrix(new double[][]
                {
                        {0.1, 0, 0, 0},
                        {0, 0.1, 0, 0},
                        {0, 0, 0.1, 0},
                        {0, 0, 0, 0.1}});
    }

    /**
     * {@inheritDoc}
     */
    public RealMatrix getStateTransitionMatrix() {


        return stateTransitionMatrix;
    }

    /**
     * {@inheritDoc}
     */
    public RealMatrix getControlMatrix() {
        return controlMatrix;
    }

    /**
     * {@inheritDoc}
     */
    public RealMatrix getProcessNoise() {
        return processNoiseCovMatrix;
    }

    /**
     * {@inheritDoc}
     */
    public RealVector getInitialStateEstimate() {
        return initialStateEstimateVector;
    }

    /**
     * {@inheritDoc}
     */
    public RealMatrix getInitialErrorCovariance() {
        return initialErrorCovMatrix;
    }
}
