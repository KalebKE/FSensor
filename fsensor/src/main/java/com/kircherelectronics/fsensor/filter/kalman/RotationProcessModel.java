package com.kircherelectronics.fsensor.filter.kalman;

import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/*
 * Acceleration Explorer
 * Copyright (C) 2013-2015, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class RotationProcessModel implements ProcessModel
{
	/**
	 * The state transition matrix, used to advance the internal state
	 * estimation each time-step.
	 */
	private RealMatrix stateTransitionMatrix;

	/** The process noise covariance matrix. */
	private RealMatrix processNoiseCovMatrix;

	/** The initial state estimation of the observed process. */
	private RealVector initialStateEstimateVector;

	/** The initial error covariance matrix of the observed process. */
	private RealMatrix initialErrorCovMatrix;

	/** The initial error covariance matrix of the observed process. */
	private RealMatrix controlMatrix;

	public RotationProcessModel()
	{
		super();

		// A = stateTransitionMatrix
		stateTransitionMatrix = new Array2DRowRealMatrix(new double[][]
		{
		{ 1, 0, 0, 0 },
		{ 0, 1, 0, 0 },
		{ 0, 0, 1, 0 },
		{ 0, 0, 0, 1 } });

		// B = stateTransitionMatrix
		controlMatrix = new Array2DRowRealMatrix(new double[][]
		{
		{ 1, 0, 0, 0 },
		{ 0, 1, 0, 0 },
		{ 0, 0, 1, 0 },
		{ 0, 0, 0, 1 } });

		// Q = processNoiseCovMatrix
		processNoiseCovMatrix = new Array2DRowRealMatrix(new double[][]
		{
		{ 1, 0, 0, 0 },
		{ 0, 1, 0, 0 },
		{ 0, 0, 1, 0 },
		{ 0, 0, 0, 1 } });

		// xP = initialStateEstimateVector
		initialStateEstimateVector = new ArrayRealVector(new double[]
		{ 0, 0, 0, 0 });

		// P0 = initialErrorCovMatrix;
		initialErrorCovMatrix = new Array2DRowRealMatrix(new double[][]
		{
		{ 0.1, 0, 0, 0 },
		{ 0, 0.1, 0, 0 },
		{ 0, 0, 0.1, 0 },
		{ 0, 0, 0, 0.1 } });
	}

	/** {@inheritDoc} */
	public RealMatrix getStateTransitionMatrix()
	{
		stateTransitionMatrix = new Array2DRowRealMatrix(new double[][]
		{
		{ 1, 0, 0, 0 },
		{ 0, 1, 0, 0 },
		{ 0, 0, 1, 0 },
		{ 0, 0, 0, 1 } });

		return stateTransitionMatrix;
	}

	/** {@inheritDoc} */
	public RealMatrix getControlMatrix()
	{
		return controlMatrix;
	}

	/** {@inheritDoc} */
	public RealMatrix getProcessNoise()
	{
		return processNoiseCovMatrix;
	}

	/** {@inheritDoc} */
	public RealVector getInitialStateEstimate()
	{
		return initialStateEstimateVector;
	}

	/** {@inheritDoc} */
	public RealMatrix getInitialErrorCovariance()
	{
		return initialErrorCovMatrix;
	}
}
